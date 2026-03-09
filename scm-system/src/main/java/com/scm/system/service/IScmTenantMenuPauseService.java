package com.scm.system.service;

import java.util.List;
import com.scm.system.domain.ScmTenantMenuPause;
import com.scm.system.domain.ScmTenantMenuPauseLog;

/**
 * 客户菜单功能暂停 服务
 */
public interface IScmTenantMenuPauseService
{
    List<ScmTenantMenuPause> selectByTenantId(String tenantId);

    List<ScmTenantMenuPauseLog> selectPauseLogsByTenantId(String tenantId);

    /** 查询某客户下处于暂停状态的菜单ID列表（租户用户首页用于拦截点击） */
    List<Long> selectPausedMenuIdsByTenantId(String tenantId);

    /** 暂停某客户下某菜单 */
    int pauseMenu(String tenantId, Long menuId, String operBy, String remark);

    /** 恢复某客户下某菜单 */
    int resumeMenu(String tenantId, Long menuId, String operBy, String remark);
}
