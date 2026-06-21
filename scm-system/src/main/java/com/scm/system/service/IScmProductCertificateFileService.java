package com.scm.system.service;

import java.util.List;
import com.scm.system.domain.ScmFile;

/**
 * 产品证件文件关联 服务层
 */
public interface IScmProductCertificateFileService
{
    List<ScmFile> selectFilesByCertificateId(Long certificateId);

    void replaceCertificateFiles(Long certificateId, String fileIdsCsv, String operBy);

    void deleteByCertificateId(Long certificateId);

    String buildFileUrlsCsv(List<ScmFile> files);

    String buildFileIdsCsv(List<ScmFile> files);
}
