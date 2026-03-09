package com.scm.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.scm.system.domain.SupplierUserApply;

/**
 * 供应商业务员关联申请
 */
public interface SupplierUserApplyMapper {

    List<SupplierUserApply> selectBySupplierId(@Param("supplierId") Long supplierId, @Param("status") String status);

    SupplierUserApply selectByApplyId(Long applyId);

    SupplierUserApply selectPendingBySupplierAndUser(@Param("supplierId") Long supplierId, @Param("userId") Long userId);

    int insert(SupplierUserApply apply);

    int updateStatus(@Param("applyId") Long applyId, @Param("status") String status,
                     @Param("auditBy") String auditBy, @Param("auditRemark") String auditRemark);
}
