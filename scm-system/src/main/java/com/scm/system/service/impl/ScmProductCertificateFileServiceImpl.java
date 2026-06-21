package com.scm.system.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.scm.common.utils.StringUtils;
import com.scm.common.utils.uuid.IdUtils;
import com.scm.system.domain.ProductCertificate;
import com.scm.system.domain.ScmFile;
import com.scm.system.domain.ScmProductCertificateFile;
import com.scm.system.mapper.ProductCertificateMapper;
import com.scm.system.mapper.ScmFileMapper;
import com.scm.system.mapper.ScmProductCertificateFileMapper;
import com.scm.system.service.IScmProductCertificateFileService;

/**
 * 产品证件文件关联 服务实现
 */
@Service
public class ScmProductCertificateFileServiceImpl implements IScmProductCertificateFileService
{
    @Autowired
    private ScmProductCertificateFileMapper certificateFileMapper;

    @Autowired
    private ScmFileMapper scmFileMapper;

    @Autowired
    private ProductCertificateMapper productCertificateMapper;

    @Override
    public List<ScmFile> selectFilesByCertificateId(Long certificateId)
    {
        if (certificateId == null)
        {
            return new ArrayList<>();
        }
        return certificateFileMapper.selectFilesByCertificateId(certificateId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceCertificateFiles(Long certificateId, String fileIdsCsv, String operBy)
    {
        if (certificateId == null)
        {
            return;
        }
        certificateFileMapper.softDeleteByCertificateId(certificateId);

        List<String> urls = new ArrayList<>();
        if (StringUtils.isNotEmpty(fileIdsCsv))
        {
            String[] parts = fileIdsCsv.split(",");
            int sort = 0;
            for (String part : parts)
            {
                String fileId = part != null ? part.trim() : "";
                if (fileId.isEmpty())
                {
                    continue;
                }
                ScmFile file = scmFileMapper.selectScmFileById(fileId);
                if (file == null)
                {
                    continue;
                }
                ScmProductCertificateFile rel = new ScmProductCertificateFile();
                rel.setId(IdUtils.dashedUuid7());
                rel.setCertificateId(certificateId);
                rel.setFileId(fileId);
                rel.setSortOrder(sort++);
                rel.setCreateBy(operBy);
                certificateFileMapper.insertScmProductCertificateFile(rel);
                if (StringUtils.isNotEmpty(file.getFileUrl()))
                {
                    urls.add(file.getFileUrl());
                }
            }
        }

        ProductCertificate row = new ProductCertificate();
        row.setCertificateId(certificateId);
        row.setCertificateFile(String.join(",", urls));
        row.setUpdateBy(operBy);
        productCertificateMapper.updateProductCertificateFile(row);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByCertificateId(Long certificateId)
    {
        if (certificateId != null)
        {
            certificateFileMapper.softDeleteByCertificateId(certificateId);
        }
    }

    @Override
    public String buildFileUrlsCsv(List<ScmFile> files)
    {
        if (files == null || files.isEmpty())
        {
            return "";
        }
        List<String> urls = new ArrayList<>();
        for (ScmFile file : files)
        {
            if (file != null && StringUtils.isNotEmpty(file.getFileUrl()))
            {
                urls.add(file.getFileUrl());
            }
        }
        return String.join(",", urls);
    }

    @Override
    public String buildFileIdsCsv(List<ScmFile> files)
    {
        if (files == null || files.isEmpty())
        {
            return "";
        }
        List<String> ids = new ArrayList<>();
        for (ScmFile file : files)
        {
            if (file != null && StringUtils.isNotEmpty(file.getFileId()))
            {
                ids.add(file.getFileId());
            }
        }
        return String.join(",", ids);
    }
}
