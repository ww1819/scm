package com.scm.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.scm.system.domain.MenuGrantSupplierAuthPair;
import com.scm.system.domain.ScmSupplierMenuAuth;

public interface ScmSupplierMenuAuthMapper
{
    int deleteBySupplierId(@Param("supplierId") Long supplierId);
    int deleteBySupplierAndHospital(@Param("supplierId") Long supplierId, @Param("hospitalId") Long hospitalId);
    int deleteByHospitalAndMenuIds(@Param("hospitalId") Long hospitalId, @Param("menuIds") List<Long> menuIds);

    int batchInsert(@Param("list") List<ScmSupplierMenuAuth> list);

    /** 忽略 uk_supplier_hospital_menu 重复（含 hospital_id 为 NULL 的全局行） */
    int batchInsertIgnore(@Param("list") List<ScmSupplierMenuAuth> list);

    List<Long> selectMenuIdsBySupplierId(@Param("supplierId") Long supplierId);
    List<Long> selectMenuIdsBySupplierAndHospital(@Param("supplierId") Long supplierId, @Param("hospitalId") Long hospitalId);

    /** 活跃供应商全局白名单 (supplier_id, menu_id)，一次查出供批量赋权比对 */
    List<MenuGrantSupplierAuthPair> selectGlobalAuthPairsForActiveSuppliers();
}
