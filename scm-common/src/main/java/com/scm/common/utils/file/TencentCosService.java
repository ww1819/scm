package com.scm.common.utils.file;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.endpoint.UserSpecifiedEndpointBuilder;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.model.GeneratePresignedUrlRequest;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ResponseHeaderOverrides;
import com.qcloud.cos.region.Region;
import com.scm.common.config.CosProperties;
import com.scm.common.exception.ServiceException;
import com.scm.common.utils.DateUtils;
import com.scm.common.utils.StringUtils;
import com.scm.common.utils.uuid.Seq;

/**
 * 腾讯云 COS 上传/下载服务（下载走官方 GetObject 流式接口）
 */
@Service
public class TencentCosService
{
    private static final Logger log = LoggerFactory.getLogger(TencentCosService.class);

    private static final String COS_SERVICE_ENDPOINT = "service.cos.myqcloud.com";

    @Autowired
    private CosProperties cosProperties;

    /** SDK 走官方 API 域名（上传/GetObject） */
    private volatile COSClient cosClient;

    /**
     * COS 上传结果
     */
    public static class CosUploadResult
    {
        private String bucketName;
        private String region;
        private String objectKey;
        private String url;
        private String etag;
        private String originalFilename;
        private String contentType;
        private long size;

        public String getBucketName()
        {
            return bucketName;
        }

        public void setBucketName(String bucketName)
        {
            this.bucketName = bucketName;
        }

        public String getRegion()
        {
            return region;
        }

        public void setRegion(String region)
        {
            this.region = region;
        }

        public String getObjectKey()
        {
            return objectKey;
        }

        public void setObjectKey(String objectKey)
        {
            this.objectKey = objectKey;
        }

        public String getUrl()
        {
            return url;
        }

        public void setUrl(String url)
        {
            this.url = url;
        }

        public String getEtag()
        {
            return etag;
        }

        public void setEtag(String etag)
        {
            this.etag = etag;
        }

        public String getOriginalFilename()
        {
            return originalFilename;
        }

        public void setOriginalFilename(String originalFilename)
        {
            this.originalFilename = originalFilename;
        }

        public String getContentType()
        {
            return contentType;
        }

        public void setContentType(String contentType)
        {
            this.contentType = contentType;
        }

        public long getSize()
        {
            return size;
        }

        public void setSize(long size)
        {
            this.size = size;
        }
    }

    public java.util.Map<String, Object> getConfigStatus()
    {
        java.util.Map<String, Object> status = new java.util.HashMap<>();
        status.put("enabled", cosProperties.isEnabled());
        status.put("configured", cosProperties.isConfigured());
        status.put("region", StringUtils.defaultString(cosProperties.getRegion(), ""));
        status.put("bucketName", StringUtils.defaultString(cosProperties.getBucketName(), ""));
        status.put("basePath", StringUtils.defaultString(cosProperties.getBasePath(), ""));
        status.put("domain", StringUtils.defaultString(cosProperties.getDomain(), ""));
        status.put("publicRead", cosProperties.isPublicRead());
        status.put("secretIdMask", maskSecret(cosProperties.getSecretId()));
        return status;
    }

    public CosUploadResult upload(MultipartFile file) throws Exception
    {
        validateReady();
        if (file == null || file.isEmpty())
        {
            throw new ServiceException("请选择要上传的文件");
        }

        FileUploadUtils.assertAllowed(file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);

        String objectKey = buildObjectKey(file);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        if (StringUtils.isNotEmpty(file.getContentType()))
        {
            metadata.setContentType(file.getContentType());
        }

        try (InputStream inputStream = file.getInputStream())
        {
            PutObjectRequest request = new PutObjectRequest(
                    cosProperties.getBucketName(), objectKey, inputStream, metadata);
            PutObjectResult result = getClient().putObject(request);
            log.info("COS 上传成功，key={}, etag={}", objectKey, result.getETag());

            CosUploadResult uploadResult = new CosUploadResult();
            uploadResult.setBucketName(cosProperties.getBucketName());
            uploadResult.setRegion(cosProperties.getRegion());
            uploadResult.setObjectKey(objectKey);
            uploadResult.setUrl(buildFileUrl(objectKey));
            uploadResult.setEtag(result.getETag());
            uploadResult.setOriginalFilename(file.getOriginalFilename());
            uploadResult.setContentType(file.getContentType());
            uploadResult.setSize(file.getSize());
            return uploadResult;
        }
    }

    /**
     * 官方推荐：GetObject 流式下载，由服务端设置 Content-Disposition 后输出给浏览器
     */
    public void download(String objectKey, String originalFilename, HttpServletResponse response) throws IOException
    {
        validateReady();
        if (StringUtils.isEmpty(objectKey))
        {
            throw new ServiceException("文件 objectKey 为空");
        }

        response.reset();
        response.setBufferSize(8192);

        GetObjectRequest getObjectRequest = new GetObjectRequest(cosProperties.getBucketName(), objectKey);
        COSObject cosObject = null;
        try
        {
            cosObject = getClient().getObject(getObjectRequest);
            ObjectMetadata metadata = cosObject.getObjectMetadata();
            String downloadName = StringUtils.defaultIfBlank(originalFilename, FilenameUtils.getName(objectKey));
            long contentLength = metadata != null ? metadata.getContentLength() : -1L;

            log.info("COS GetObject 开始下载, key={}, size={}", objectKey, contentLength);

            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            if (contentLength > 0)
            {
                response.setContentLengthLong(contentLength);
            }
            FileUtils.setAttachmentResponseHeader(response, downloadName);

            try (COSObjectInputStream inputStream = cosObject.getObjectContent())
            {
                long copied = IOUtils.copyLarge(inputStream, response.getOutputStream());
                response.flushBuffer();
                log.info("COS GetObject 下载完成, key={}, copied={}", objectKey, copied);
            }
        }
        catch (CosServiceException e)
        {
            log.error("COS GetObject 失败, key={}, code={}, msg={}", objectKey, e.getErrorCode(), e.getErrorMessage());
            throw new ServiceException("COS 下载失败：" + e.getErrorMessage());
        }
        catch (CosClientException e)
        {
            log.error("COS 客户端异常, key={}", objectKey, e);
            throw new ServiceException("COS 下载失败：" + e.getMessage());
        }
        finally
        {
            if (cosObject != null)
            {
                cosObject.close();
            }
        }
    }

    /**
     * 预签名下载 URL（自定义域名桶需单独 Client，见腾讯云文档「使用自定义域名生成预签名 URL」）
     */
    public String generatePresignedDownloadUrl(String objectKey, String originalFilename)
    {
        validateReady();
        if (StringUtils.isEmpty(objectKey))
        {
            throw new ServiceException("文件 objectKey 为空");
        }

        String downloadName = StringUtils.defaultIfBlank(originalFilename, FilenameUtils.getName(objectKey));
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(
                cosProperties.getBucketName(), objectKey, HttpMethodName.GET);
        request.setExpiration(new Date(System.currentTimeMillis() + 3600_000L));

        ResponseHeaderOverrides overrides = new ResponseHeaderOverrides();
        overrides.setContentDisposition("attachment; filename=\"" + downloadName.replace("\"", "") + "\"");
        request.setResponseHeaders(overrides);

        COSClient presignClient = null;
        try
        {
            presignClient = createPresignClient();
            URL url = presignClient.generatePresignedUrl(request);
            log.info("COS 预签名下载 URL 已生成, key={}", objectKey);
            return url.toString();
        }
        finally
        {
            if (presignClient != null)
            {
                presignClient.shutdown();
            }
        }
    }

    /** 规范化公有读 fileUrl（路径段编码，避免中文/空格导致 404） */
    public String normalizePublicFileUrl(String fileUrl)
    {
        if (StringUtils.isEmpty(fileUrl))
        {
            return fileUrl;
        }
        try
        {
            return openFetchUrl(fileUrl).toString();
        }
        catch (IOException e)
        {
            log.warn("COS fileUrl 规范化失败, url={}", fileUrl, e);
            return fileUrl;
        }
    }

    /** 公有读备用：HTTP GET fileUrl（不加 disposition 等 query 参数） */
    public void downloadFromPublicUrl(String fileUrl, String originalFilename, HttpServletResponse response)
            throws IOException
    {
        if (StringUtils.isEmpty(fileUrl))
        {
            throw new ServiceException("文件访问地址为空");
        }

        response.reset();
        response.setBufferSize(8192);

        HttpURLConnection connection = (HttpURLConnection) openFetchUrl(fileUrl).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(120000);
        connection.setInstanceFollowRedirects(true);
        try
        {
            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK)
            {
                throw new ServiceException("COS 文件读取失败，HTTP " + status);
            }

            String remoteType = StringUtils.defaultString(connection.getContentType(), "");
            if (remoteType.contains("text/html") || remoteType.contains("application/json")
                    || remoteType.contains("text/xml") || remoteType.contains("application/xml"))
            {
                throw new ServiceException("COS 返回异常内容类型：" + remoteType);
            }

            long contentLength = connection.getContentLengthLong();
            String downloadName = StringUtils.defaultIfBlank(originalFilename, "download");
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            if (contentLength > 0)
            {
                response.setContentLengthLong(contentLength);
            }
            FileUtils.setAttachmentResponseHeader(response, downloadName);

            try (InputStream inputStream = connection.getInputStream())
            {
                InputStream validated = rejectCosErrorXml(inputStream);
                IOUtils.copyLarge(validated, response.getOutputStream());
                response.flushBuffer();
            }
        }
        finally
        {
            connection.disconnect();
        }
    }

    private COSClient createPresignClient()
    {
        COSCredentials cred = new BasicCOSCredentials(
                cosProperties.getSecretId(), cosProperties.getSecretKey());
        ClientConfig clientConfig = new ClientConfig(new Region(cosProperties.getRegion()));
        clientConfig.setHttpProtocol(HttpProtocol.https);

        String domain = cosProperties.getDomain();
        if (StringUtils.isNotEmpty(domain))
        {
            String host = domain.replaceFirst("^https?://", "").replaceAll("/+$", "");
            clientConfig.setEndpointBuilder(new UserSpecifiedEndpointBuilder(host, COS_SERVICE_ENDPOINT));
        }
        return new COSClient(cred, clientConfig);
    }

    private COSClient getClient()
    {
        if (cosClient == null)
        {
            synchronized (this)
            {
                if (cosClient == null)
                {
                    COSCredentials cred = new BasicCOSCredentials(
                            cosProperties.getSecretId(), cosProperties.getSecretKey());
                    ClientConfig clientConfig = new ClientConfig(new Region(cosProperties.getRegion()));
                    clientConfig.setHttpProtocol(HttpProtocol.https);
                    cosClient = new COSClient(cred, clientConfig);
                }
            }
        }
        return cosClient;
    }

    private void validateReady()
    {
        if (!cosProperties.isEnabled())
        {
            throw new ServiceException("COS 上传未启用，请在 application.yml 中设置 scm.cos.enabled=true");
        }
        if (!cosProperties.isConfigured())
        {
            throw new ServiceException("COS 配置不完整，请检查 secretId、secretKey、bucketName、region");
        }
    }

    private String buildObjectKey(MultipartFile file)
    {
        String basePath = StringUtils.defaultString(cosProperties.getBasePath(), "");
        if (StringUtils.isNotEmpty(basePath) && !basePath.endsWith("/"))
        {
            basePath = basePath + "/";
        }
        String extension = FileUploadUtils.getExtension(file);
        String filename = StringUtils.format("{}_{}.{}",
                FilenameUtils.getBaseName(file.getOriginalFilename()), Seq.getId(Seq.uploadSeqType), extension);
        return basePath + DateUtils.datePath() + "/" + filename;
    }

    private String buildFileUrl(String objectKey)
    {
        String encodedKey = encodeObjectKeyForUrl(objectKey);
        String domain = cosProperties.getDomain();
        if (StringUtils.isNotEmpty(domain))
        {
            String normalized = domain.endsWith("/") ? domain.substring(0, domain.length() - 1) : domain;
            return normalized + "/" + encodedKey;
        }
        return String.format("https://%s.cos.%s.myqcloud.com/%s",
                cosProperties.getBucketName(), cosProperties.getRegion(), encodedKey);
    }

    private static InputStream rejectCosErrorXml(InputStream inputStream) throws IOException
    {
        PushbackInputStream pushback = new PushbackInputStream(new BufferedInputStream(inputStream), 256);
        byte[] head = new byte[256];
        int read = pushback.read(head);
        if (read > 0)
        {
            pushback.unread(head, 0, read);
            String prefix = new String(head, 0, read, StandardCharsets.UTF_8).trim();
            if (prefix.startsWith("<?xml") || prefix.startsWith("<Error"))
            {
                throw new ServiceException("COS 返回错误：" + prefix.substring(0, Math.min(prefix.length(), 200)));
            }
        }
        return pushback;
    }

    private String encodeObjectKeyForUrl(String objectKey)
    {
        return encodePathSegments(objectKey);
    }

    private URL openFetchUrl(String fileUrl) throws java.net.MalformedURLException
    {
        int schemeEnd = fileUrl.indexOf("://");
        if (schemeEnd <= 0)
        {
            return new URL(fileUrl);
        }
        int pathStart = fileUrl.indexOf('/', schemeEnd + 3);
        if (pathStart < 0)
        {
            return new URL(fileUrl);
        }
        String base = fileUrl.substring(0, pathStart + 1);
        String path = fileUrl.substring(pathStart + 1);
        return new URL(base + encodePathSegments(path));
    }

    /**
     * 路径段编码：已编码段先 decode 再 encode，避免 %E5%... 被二次编码成 %25E5%25...
     */
    private String encodePathSegments(String path)
    {
        if (StringUtils.isEmpty(path))
        {
            return path;
        }
        String[] segments = path.split("/");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < segments.length; i++)
        {
            if (i > 0)
            {
                sb.append('/');
            }
            if (StringUtils.isEmpty(segments[i]))
            {
                continue;
            }
            String segment = segments[i];
            try
            {
                segment = java.net.URLDecoder.decode(segment, "UTF-8");
            }
            catch (IllegalArgumentException | java.io.UnsupportedEncodingException ignored)
            {
                // 含非法 % 序列或编码异常时按原样继续
            }
            try
            {
                sb.append(URLEncoder.encode(segment, "UTF-8").replace("+", "%20"));
            }
            catch (java.io.UnsupportedEncodingException e)
            {
                throw new ServiceException("UTF-8 编码不支持");
            }
        }
        return sb.toString();
    }

    private static String maskSecret(String value)
    {
        if (StringUtils.isEmpty(value))
        {
            return "未配置";
        }
        if (value.length() <= 8)
        {
            return "****";
        }
        return value.substring(0, 4) + "****" + value.substring(value.length() - 4);
    }

    @PreDestroy
    public void destroy()
    {
        if (cosClient != null)
        {
            cosClient.shutdown();
        }
    }
}
