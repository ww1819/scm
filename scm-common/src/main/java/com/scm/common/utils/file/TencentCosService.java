package com.scm.common.utils.file;



import java.io.InputStream;

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

import com.qcloud.cos.model.COSObject;

import com.qcloud.cos.model.COSObjectInputStream;

import com.qcloud.cos.model.ObjectMetadata;

import com.qcloud.cos.model.PutObjectRequest;

import com.qcloud.cos.model.PutObjectResult;

import com.qcloud.cos.region.Region;

import com.scm.common.config.CosProperties;

import com.scm.common.exception.ServiceException;

import com.scm.common.utils.DateUtils;

import com.scm.common.utils.StringUtils;

import com.scm.common.utils.uuid.Seq;



/**

 * 腾讯云 COS 上传/下载服务

 */

@Service

public class TencentCosService

{

    private static final Logger log = LoggerFactory.getLogger(TencentCosService.class);



    @Autowired

    private CosProperties cosProperties;



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



    /**

     * 返回脱敏后的配置状态，供测试页展示

     */

    public java.util.Map<String, Object> getConfigStatus()

    {

        java.util.Map<String, Object> status = new java.util.HashMap<>();

        status.put("enabled", cosProperties.isEnabled());

        status.put("configured", cosProperties.isConfigured());

        status.put("region", StringUtils.defaultString(cosProperties.getRegion(), ""));

        status.put("bucketName", StringUtils.defaultString(cosProperties.getBucketName(), ""));

        status.put("basePath", StringUtils.defaultString(cosProperties.getBasePath(), ""));

        status.put("domain", StringUtils.defaultString(cosProperties.getDomain(), ""));

        status.put("secretIdMask", maskSecret(cosProperties.getSecretId()));

        return status;

    }



    /**

     * 上传文件到 COS

     */

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

     * 从 COS 下载文件并写入响应流

     */

    public void download(String objectKey, String originalFilename, String contentType,

            HttpServletResponse response) throws Exception

    {

        validateReady();

        if (StringUtils.isEmpty(objectKey))

        {

            throw new ServiceException("文件对象键为空");

        }



        COSObject cosObject = getClient().getObject(cosProperties.getBucketName(), objectKey);

        ObjectMetadata metadata = cosObject.getObjectMetadata();

        String downloadName = StringUtils.isNotEmpty(originalFilename) ? originalFilename : FilenameUtils.getName(objectKey);

        String responseType = StringUtils.isNotEmpty(contentType) ? contentType

                : (metadata != null && StringUtils.isNotEmpty(metadata.getContentType())

                        ? metadata.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE);



        response.setContentType(responseType);

        if (metadata != null && metadata.getContentLength() > 0)

        {

            response.setContentLengthLong(metadata.getContentLength());

        }

        FileUtils.setAttachmentResponseHeader(response, downloadName);



        try (COSObjectInputStream inputStream = cosObject.getObjectContent())

        {

            IOUtils.copy(inputStream, response.getOutputStream());

            response.flushBuffer();

        }

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

        String domain = cosProperties.getDomain();

        if (StringUtils.isNotEmpty(domain))

        {

            String normalized = domain.endsWith("/") ? domain.substring(0, domain.length() - 1) : domain;

            return normalized + "/" + objectKey;

        }

        return String.format("https://%s.cos.%s.myqcloud.com/%s",

                cosProperties.getBucketName(), cosProperties.getRegion(), objectKey);

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

                    cosClient = new COSClient(cred, clientConfig);

                }

            }

        }

        return cosClient;

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

