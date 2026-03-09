package com.scm.system.domain;

import java.util.Date;

/**
 * 客户信息修改记录 scm_tenant_modify_log
 */
public class ScmTenantModifyLog
{
    private String logId;
    private String tenantId;
    private String fieldName;
    private String oldValue;
    private String newValue;
    private String operBy;
    private Date operTime;

    public String getLogId() { return logId; }
    public void setLogId(String logId) { this.logId = logId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }
    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }
    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }
    public String getOperBy() { return operBy; }
    public void setOperBy(String operBy) { this.operBy = operBy; }
    public Date getOperTime() { return operTime; }
    public void setOperTime(Date operTime) { this.operTime = operTime; }
}
