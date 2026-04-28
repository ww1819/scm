package com.scm.system.domain;

import java.util.Date;

/**
 * 供应商关联医院申请变更日志
 */
public class ScmHospitalSupplierApplyLog
{
    private String logId;
    private String applyId;
    private String supplierId;
    private String hospitalId;
    private String actionType;
    private String operBy;
    private Date operTime;
    private String snapshot;

    public String getLogId() { return logId; }
    public void setLogId(String logId) { this.logId = logId; }
    public String getApplyId() { return applyId; }
    public void setApplyId(String applyId) { this.applyId = applyId; }
    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }
    public String getHospitalId() { return hospitalId; }
    public void setHospitalId(String hospitalId) { this.hospitalId = hospitalId; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public String getOperBy() { return operBy; }
    public void setOperBy(String operBy) { this.operBy = operBy; }
    public Date getOperTime() { return operTime; }
    public void setOperTime(Date operTime) { this.operTime = operTime; }
    public String getSnapshot() { return snapshot; }
    public void setSnapshot(String snapshot) { this.snapshot = snapshot; }
}
