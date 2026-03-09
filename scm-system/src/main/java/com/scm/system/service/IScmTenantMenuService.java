package com.scm.system.service;

import java.util.List;

/**
 * 租户功能菜单授权 服务
 */
public interface IScmTenantMenuService
{
    List<Long> selectMenuIdsByTenantId(String tenantId);

    /** 保存客户菜单权限（createBy 可为空） */
    void saveTenantMenus(String tenantId, Long[] menuIds, String createBy);
}
