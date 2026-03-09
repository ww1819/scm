package com.scm.system.service;

/**
 * 客户新增后自动创建医院管理员角色与用户
 */
public interface ITenantAdminCreateService
{
    /**
     * 为指定客户创建医院管理员角色（若不存在）及首字母用户（若不存在），并绑定角色
     * @param tenantId 客户ID
     * @param tenantName 客户名称
     * @param pinyinCode 拼音简码（用作用户名/登录名）
     * @param operBy 操作人
     */
    void ensureHospitalAdminRoleAndUser(String tenantId, String tenantName, String pinyinCode, String operBy);
}
