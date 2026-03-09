package com.scm.system.domain;

import java.util.Date;

/**
 * 客户菜单功能管理列表 VO：客户已授权菜单 + 暂停状态
 */
public class ScmTenantMenuPauseManageVo {

    private String tenantId;
    private Long menuId;
    private String menuName;
    /** 暂停状态（0正常 1暂停），无暂停记录时为 0 */
    private String pauseStatus;
    private Date pauseTime;
    private String pauseId;

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public Long getMenuId() { return menuId; }
    public void setMenuId(Long menuId) { this.menuId = menuId; }
    public String getMenuName() { return menuName; }
    public void setMenuName(String menuName) { this.menuName = menuName; }
    public String getPauseStatus() { return pauseStatus; }
    public void setPauseStatus(String pauseStatus) { this.pauseStatus = pauseStatus; }
    public Date getPauseTime() { return pauseTime; }
    public void setPauseTime(Date pauseTime) { this.pauseTime = pauseTime; }
    public String getPauseId() { return pauseId; }
    public void setPauseId(String pauseId) { this.pauseId = pauseId; }
}
