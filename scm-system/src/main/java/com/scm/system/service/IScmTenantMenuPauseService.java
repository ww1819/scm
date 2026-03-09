package com.scm.system.service;

import java.util.List;
import com.scm.system.domain.ScmTenantMenuPause;
import com.scm.system.domain.ScmTenantMenuPauseLog;
import com.scm.system.domain.ScmTenantMenuPauseLogVo;
import com.scm.system.domain.ScmTenantMenuPauseManageVo;

/**
 * 客户菜单功能暂停 服务
 */
public interface IScmTenantMenuPauseService
{
    /** 客户菜单功能管理：列出该客户所有菜单权限及暂停状态 */
    List<ScmTenantMenuPauseManageVo> listMenusWithStatusByTenantId(String tenantId);

    List<ScmTenantMenuPause> selectByTenantId(String tenantId);

    List<ScmTenantMenuPauseLog> selectPauseLogsByTenantId(String tenantId);

    /** 客户菜单暂停记录（含菜单名称） */
    List<ScmTenantMenuPauseLogVo> listPauseLogsWithMenuNameByTenantId(String tenantId);

    /** 查询某客户下处于暂停状态的菜单ID列表（租户用户首页用于拦截点击） */
    List<Long> selectPausedMenuIdsByTenantId(String tenantId);

    /** 暂停某客户下某菜单 */
    int pauseMenu(String tenantId, Long menuId, String operBy, String remark);

    /** 恢复某客户下某菜单 */
    int resumeMenu(String tenantId, Long menuId, String operBy, String remark);
}
