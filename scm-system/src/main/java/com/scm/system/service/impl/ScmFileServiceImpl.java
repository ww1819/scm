package com.scm.system.service.impl;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
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
    @Autowired
    private ScmFileMapper scmFileMapper;

    @Autowired
    private TencentCosService tencentCosService;

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
    public void download(String fileId, HttpServletResponse response) throws Exception
    {
        ScmFile scmFile = scmFileMapper.selectScmFileById(fileId);
        if (scmFile == null)
        {
            throw new ServiceException("文件不存在或已删除");
        }
        if (ScmFileConstants.STORAGE_COS.equals(scmFile.getStorageType()))
        {
            tencentCosService.download(scmFile.getObjectKey(), scmFile.getOriginalName(),
                    scmFile.getContentType(), response);
            return;
        }
        throw new ServiceException("暂不支持的存储类型：" + scmFile.getStorageType());
    }
}
