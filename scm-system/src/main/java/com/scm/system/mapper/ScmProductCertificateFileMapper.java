package com.scm.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.scm.system.domain.ScmFile;
import com.scm.system.domain.ScmProductCertificateFile;

/**
 * 产品证件文件关联 数据层
 */
public interface ScmProductCertificateFileMapper
{
    List<ScmFile> selectFilesByCertificateId(@Param("certificateId") Long certificateId);

    int softDeleteByCertificateId(@Param("certificateId") Long certificateId);

    int insertScmProductCertificateFile(ScmProductCertificateFile row);
}
