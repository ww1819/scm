package com.scm.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.scm.system.domain.ScmSupplierMenuAuth;

public interface ScmSupplierMenuAuthMapper
{
    int deleteBySupplierId(@Param("supplierId") Long supplierId);
    int deleteBySupplierAndHospital(@Param("supplierId") Long supplierId, @Param("hospitalId") Long hospitalId);
    int deleteByHospitalAndMenuIds(@Param("hospitalId") Long hospitalId, @Param("menuIds") List<Long> menuIds);

    int batchInsert(@Param("list") List<ScmSupplierMenuAuth> list);

    List<Long> selectMenuIdsBySupplierId(@Param("supplierId") Long supplierId);
    List<Long> selectMenuIdsBySupplierAndHospital(@Param("supplierId") Long supplierId, @Param("hospitalId") Long hospitalId);
}
