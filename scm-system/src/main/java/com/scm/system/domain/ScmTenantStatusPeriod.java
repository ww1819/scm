package com.scm.system.domain;

import java.util.Date;
import com.scm.common.core.domain.BaseEntity;

/**
 * 客户实际启用停用时间段 scm_tenant_status_period
 */
public class ScmTenantStatusPeriod extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private String periodId;
    private String tenantId;
    /** 状态（0启用 1停用） */
    private String status;
    private Date startTime;
    private Date endTime;

    public String getPeriodId() { return periodId; }
    public void setPeriodId(String periodId) { this.periodId = periodId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }
    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }
}
