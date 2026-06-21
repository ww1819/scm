package com.scm.system.service;

import java.util.List;
import com.scm.system.domain.ScmFile;

/**
 * 供应商证件文件关联 服务层
 */
public interface IScmSupplierCertificateFileService
{
    List<ScmFile> selectFilesByCertificateId(Long certificateId);

    /**
     * 替换证件关联文件，并同步 scm_supplier_certificate.certificate_file（URL 逗号分隔）
     */
    void replaceCertificateFiles(Long certificateId, String fileIdsCsv, String operBy);

    void deleteByCertificateId(Long certificateId);

    String buildFileUrlsCsv(List<ScmFile> files);

    String buildFileIdsCsv(List<ScmFile> files);
}
