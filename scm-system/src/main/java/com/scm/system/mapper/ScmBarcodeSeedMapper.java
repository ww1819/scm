package com.scm.system.mapper;

import org.apache.ibatis.annotations.Param;
import com.scm.system.domain.ScmBarcodeSeed;

/**
 * 条码种子序列表
 */
public interface ScmBarcodeSeedMapper
{
    ScmBarcodeSeed selectForUpdate(@Param("counterType") String counterType,
        @Param("tenantId") String tenantId,
        @Param("zsCustomerId") String zsCustomerId,
        @Param("warehouseId") String warehouseId,
        @Param("highLowFlag") String highLowFlag);

    int updateSeedValue(@Param("id") String id, @Param("newValue") long newValue);

    int ensureTenantSeed(@Param("id") String id,
        @Param("tenantId") String tenantId,
        @Param("warehouseId") String warehouseId,
        @Param("highLowFlag") String highLowFlag);

    int ensureZsCustomerSeed(@Param("id") String id,
        @Param("tenantId") String tenantId,
        @Param("zsCustomerId") String zsCustomerId,
        @Param("warehouseId") String warehouseId,
        @Param("highLowFlag") String highLowFlag);
}
