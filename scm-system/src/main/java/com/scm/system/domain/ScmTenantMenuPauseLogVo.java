package com.scm.system.domain;

import java.util.Date;

/** 客户菜单暂停记录列表 VO（含菜单名称） */
public class ScmTenantMenuPauseLogVo {

    private String logId;
    private String pauseId;
    private String tenantId;
    private Long menuId;
    private String menuName;
    private String action;
    private String operBy;
    private Date operTime;
    private String remark;

    public String getLogId() { return logId; }
    public void setLogId(String logId) { this.logId = logId; }
    public String getPauseId() { return pauseId; }
    public void setPauseId(String pauseId) { this.pauseId = pauseId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public Long getMenuId() { return menuId; }
    public void setMenuId(Long menuId) { this.menuId = menuId; }
    public String getMenuName() { return menuName; }
    public void setMenuName(String menuName) { this.menuName = menuName; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getOperBy() { return operBy; }
    public void setOperBy(String operBy) { this.operBy = operBy; }
    public Date getOperTime() { return operTime; }
    public void setOperTime(Date operTime) { this.operTime = operTime; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
