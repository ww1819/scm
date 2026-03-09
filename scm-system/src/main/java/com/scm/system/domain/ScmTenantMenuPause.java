package com.scm.system.domain;

import com.scm.common.core.domain.BaseEntity;
import java.util.Date;

/**
 * 客户菜单功能暂停 scm_tenant_menu_pause
 */
public class ScmTenantMenuPause extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private String pauseId;
    private String tenantId;
    private Long menuId;
    /** 暂停状态（0正常 1暂停） */
    private String pauseStatus;
    /** 暂停时间 */
    private Date pauseTime;

    public String getPauseId() { return pauseId; }
    public void setPauseId(String pauseId) { this.pauseId = pauseId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public Long getMenuId() { return menuId; }
    public void setMenuId(Long menuId) { this.menuId = menuId; }
    public String getPauseStatus() { return pauseStatus; }
    public void setPauseStatus(String pauseStatus) { this.pauseStatus = pauseStatus; }
    public Date getPauseTime() { return pauseTime; }
    public void setPauseTime(Date pauseTime) { this.pauseTime = pauseTime; }
}
