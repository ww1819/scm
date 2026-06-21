package com.scm.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.scm.system.domain.ScmFile;
import com.scm.system.domain.ScmSupplierCertificateFile;

/**
 * 供应商证件文件关联 数据层
 */
public interface ScmSupplierCertificateFileMapper
{
    List<ScmFile> selectFilesByCertificateId(@Param("certificateId") Long certificateId);

    int softDeleteByCertificateId(@Param("certificateId") Long certificateId);

    int insertScmSupplierCertificateFile(ScmSupplierCertificateFile row);
}
