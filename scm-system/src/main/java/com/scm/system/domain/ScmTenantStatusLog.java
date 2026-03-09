package com.scm.system.domain;

import java.util.Date;

/**
 * 客户启用停用记录 scm_tenant_status_log
 */
public class ScmTenantStatusLog
{
    private String logId;
    private String tenantId;
    /** 动作（0启用 1停用） */
    private String action;
    private String operBy;
    private Date operTime;
    private String remark;

    public String getLogId() { return logId; }
    public void setLogId(String logId) { this.logId = logId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getOperBy() { return operBy; }
    public void setOperBy(String operBy) { this.operBy = operBy; }
    public Date getOperTime() { return operTime; }
    public void setOperTime(Date operTime) { this.operTime = operTime; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
