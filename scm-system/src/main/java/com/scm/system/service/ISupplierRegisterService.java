package com.scm.system.service;

import com.scm.common.core.domain.entity.SysUser;
import com.scm.system.domain.Supplier;

/**
 * 供应商注册与业务员申请
 */
public interface ISupplierRegisterService {

    /**
     * 供应商注册：校验供应商不存在后新增供应商、供应商管理员与供应商业务员角色、管理员用户并关联
     * @return 注册成功返回供应商ID，失败抛异常或返回 null
     */
    Long registerSupplier(Supplier supplier, SysUser adminUser, String operBy);

    /**
     * 业务员注册：选择供应商并填写用户信息，创建用户及待审核申请
     * @return 申请ID，提示需供应商管理员审核
     */
    Long registerSalesperson(Long supplierId, SysUser user, String operBy);

    /**
     * 注册用户提交供应商关联申请：当前用户选择供应商提交，待供应商管理员审核通过后添加供应商业务员角色
     * @param supplierId 供应商ID
     * @param userId 当前用户ID
     * @param operBy 操作人
     * @return 申请ID
     */
    Long submitSupplierAssociate(Long supplierId, Long userId, String operBy);

    /**
     * 供应商管理员审核业务员申请：通过则绑定用户为供应商业务员
     */
    void approveApply(Long applyId, String approved, String auditRemark, String operBy);
}
