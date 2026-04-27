package com.scm.system.service;

/**
 * 解析当前登录用户关联的供应商上下文
 */
public interface IScmSupplierContextService
{
    /**
     * 从 scm_supplier_user 解析（主账号优先），无关联返回 null
     */
    Long resolveSupplierIdForUser(Long userId);
}
