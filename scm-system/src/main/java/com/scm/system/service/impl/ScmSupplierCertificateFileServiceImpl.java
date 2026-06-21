package com.scm.system.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.scm.common.utils.StringUtils;
import com.scm.common.utils.uuid.IdUtils;
import com.scm.system.domain.ScmFile;
import com.scm.system.domain.ScmSupplierCertificateFile;
import com.scm.system.domain.SupplierCertificate;
import com.scm.system.mapper.ScmFileMapper;
import com.scm.system.mapper.ScmSupplierCertificateFileMapper;
import com.scm.system.mapper.SupplierCertificateMapper;
import com.scm.system.service.IScmSupplierCertificateFileService;

/**
 * 供应商证件文件关联 服务实现
 */
@Service
public class ScmSupplierCertificateFileServiceImpl implements IScmSupplierCertificateFileService
{
    @Autowired
    private ScmSupplierCertificateFileMapper certificateFileMapper;

    @Autowired
    private ScmFileMapper scmFileMapper;

    @Autowired
    private SupplierCertificateMapper supplierCertificateMapper;

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
                ScmSupplierCertificateFile rel = new ScmSupplierCertificateFile();
                rel.setId(IdUtils.dashedUuid7());
                rel.setCertificateId(certificateId);
                rel.setFileId(fileId);
                rel.setSortOrder(sort++);
                rel.setCreateBy(operBy);
                certificateFileMapper.insertScmSupplierCertificateFile(rel);
                if (StringUtils.isNotEmpty(file.getFileUrl()))
                {
                    urls.add(file.getFileUrl());
                }
            }
        }

        SupplierCertificate row = new SupplierCertificate();
        row.setCertificateId(certificateId);
        row.setCertificateFile(String.join(",", urls));
        row.setUpdateBy(operBy);
        supplierCertificateMapper.updateSupplierCertificateFile(row);
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
