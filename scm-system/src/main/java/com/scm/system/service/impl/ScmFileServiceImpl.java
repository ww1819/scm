package com.scm.system.service.impl;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.scm.common.config.CosProperties;
import com.scm.common.constant.ScmFileConstants;
import com.scm.common.exception.ServiceException;
import com.scm.common.utils.StringUtils;
import com.scm.common.utils.file.TencentCosService;
import com.scm.common.utils.uuid.IdUtils;
import com.scm.system.domain.ScmFile;
import com.scm.system.mapper.ScmFileMapper;
import com.scm.system.service.IScmFileService;

/**
 * 统一文件存储 服务实现
 */
@Service
public class ScmFileServiceImpl implements IScmFileService
{
    private static final Logger log = LoggerFactory.getLogger(ScmFileServiceImpl.class);

    @Autowired
    private ScmFileMapper scmFileMapper;

    @Autowired
    private TencentCosService tencentCosService;

    @Autowired
    private CosProperties cosProperties;

    @Override
    public ScmFile selectScmFileById(String fileId)
    {
        return scmFileMapper.selectScmFileById(fileId);
    }

    @Override
    public List<ScmFile> selectScmFileList(ScmFile query)
    {
        return scmFileMapper.selectScmFileList(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ScmFile uploadToCos(MultipartFile file, String sourceModule, String createBy) throws Exception
    {
        TencentCosService.CosUploadResult uploadResult = tencentCosService.upload(file);

        ScmFile scmFile = new ScmFile();
        scmFile.setFileId(IdUtils.dashedUuid7());
        scmFile.setStorageType(ScmFileConstants.STORAGE_COS);
        scmFile.setBucketName(uploadResult.getBucketName());
        scmFile.setRegion(uploadResult.getRegion());
        scmFile.setObjectKey(uploadResult.getObjectKey());
        scmFile.setOriginalName(uploadResult.getOriginalFilename());
        scmFile.setFileName(FilenameUtils.getName(uploadResult.getObjectKey()));
        scmFile.setFileExt(FilenameUtils.getExtension(uploadResult.getOriginalFilename()));
        scmFile.setContentType(uploadResult.getContentType());
        scmFile.setFileSize(uploadResult.getSize());
        scmFile.setFileUrl(uploadResult.getUrl());
        scmFile.setEtag(uploadResult.getEtag());
        scmFile.setSourceModule(StringUtils.defaultIfBlank(sourceModule, ScmFileConstants.MODULE_COS_TEST));
        scmFile.setCreateBy(createBy);

        if (scmFileMapper.insertScmFile(scmFile) <= 0)
        {
            throw new ServiceException("文件记录保存失败");
        }
        return scmFile;
    }

    @Override
    public String getPresignedDownloadUrl(String fileId) throws Exception
    {
        ScmFile scmFile = requireCosFile(fileId);
        if (cosProperties.isPublicRead() && StringUtils.isNotEmpty(scmFile.getFileUrl()))
        {
            return tencentCosService.normalizePublicFileUrl(scmFile.getFileUrl());
        }
        return tencentCosService.generatePresignedDownloadUrl(scmFile.getObjectKey(), scmFile.getOriginalName());
    }

    @Override
    public void download(String fileId, HttpServletResponse response) throws Exception
    {
        ScmFile scmFile = requireCosFile(fileId);
        if (cosProperties.isPublicRead() && StringUtils.isNotEmpty(scmFile.getFileUrl()))
        {
            log.info("公有读桶下载, fileId={}, url={}", fileId, scmFile.getFileUrl());
            tencentCosService.downloadFromPublicUrl(scmFile.getFileUrl(), scmFile.getOriginalName(), response);
            return;
        }
        try
        {
            tencentCosService.download(scmFile.getObjectKey(), scmFile.getOriginalName(), response);
        }
        catch (Exception sdkEx)
        {
            log.warn("COS SDK GetObject 下载失败, fileId={}, key={}, 尝试公网 URL 拉取",
                    fileId, scmFile.getObjectKey(), sdkEx);
            if (response.isCommitted() || StringUtils.isEmpty(scmFile.getFileUrl()))
            {
                throw sdkEx;
            }
            response.reset();
            tencentCosService.downloadFromPublicUrl(scmFile.getFileUrl(), scmFile.getOriginalName(), response);
        }
    }

    private ScmFile requireCosFile(String fileId)
    {
        ScmFile scmFile = scmFileMapper.selectScmFileById(fileId);
        if (scmFile == null)
        {
            throw new ServiceException("文件不存在或已删除");
        }
        if (!ScmFileConstants.STORAGE_COS.equals(scmFile.getStorageType()))
        {
            throw new ServiceException("暂不支持的存储类型：" + scmFile.getStorageType());
        }
        if (StringUtils.isEmpty(scmFile.getObjectKey()))
        {
            throw new ServiceException("文件 objectKey 为空，无法下载");
        }
        return scmFile;
    }
}
