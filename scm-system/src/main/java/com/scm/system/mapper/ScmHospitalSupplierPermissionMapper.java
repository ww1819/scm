package com.scm.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.scm.system.domain.ScmHospitalSupplierPermission;

public interface ScmHospitalSupplierPermissionMapper
{
    List<ScmHospitalSupplierPermission> selectList(ScmHospitalSupplierPermission query);

    ScmHospitalSupplierPermission selectByHospitalAndSupplier(@Param("hospitalId") Long hospitalId,
        @Param("supplierId") Long supplierId);

    int insert(ScmHospitalSupplierPermission row);

    int update(ScmHospitalSupplierPermission row);

    int logicalDeleteById(@Param("id") String id, @Param("updateBy") String updateBy);

    List<Long> selectForbidSubmitHospitalIds(@Param("supplierId") Long supplierId);
}
