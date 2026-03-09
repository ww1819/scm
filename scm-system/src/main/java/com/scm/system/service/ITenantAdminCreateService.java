package com.scm.system.service;

/**
 * 客户新增后自动创建医院管理员、供应商、供应商业务员角色及医院管理员用户
 */
public interface ITenantAdminCreateService
{
    /**
     * 为指定客户创建医院管理员角色（若不存在）及首字母用户（若不存在），并绑定角色
     * @deprecated 请使用 {@link #ensureTenantRolesAndAdminUser}
     */
    void ensureHospitalAdminRoleAndUser(String tenantId, String tenantName, String pinyinCode, String operBy);

    /**
     * 确保客户下存在：医院管理员、供应商、供应商业务员三个角色；并确保存在医院管理员用户（若不存在则创建）
     * @param tenantId 客户ID
     * @param tenantName 客户名称
     * @param pinyinCode 拼音简码（用于管理员登录名）
     * @param operBy 操作人
     */
    void ensureTenantRolesAndAdminUser(String tenantId, String tenantName, String pinyinCode, String operBy);
}
