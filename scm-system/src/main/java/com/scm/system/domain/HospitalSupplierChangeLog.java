package com.scm.system.domain;

import java.util.Date;

/**
 * 医院供应商关联变更日志
 */
public class HospitalSupplierChangeLog
{
    private String logId;
    private String relationId;
    private String hospitalId;
    private String supplierId;
    private String hospitalName;
    private String supplierName;
    private String changeType;
    private String operBy;
    private Date operTime;
    private String changeSnapshot;

    public String getLogId()
    {
        return logId;
    }

    public void setLogId(String logId)
    {
        this.logId = logId;
    }

    public String getRelationId()
    {
        return relationId;
    }

    public void setRelationId(String relationId)
    {
        this.relationId = relationId;
    }

    public String getHospitalId()
    {
        return hospitalId;
    }

    public void setHospitalId(String hospitalId)
    {
        this.hospitalId = hospitalId;
    }

    public String getSupplierId()
    {
        return supplierId;
    }

    public void setSupplierId(String supplierId)
    {
        this.supplierId = supplierId;
    }

    public String getHospitalName()
    {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName)
    {
        this.hospitalName = hospitalName;
    }

    public String getSupplierName()
    {
        return supplierName;
    }

    public void setSupplierName(String supplierName)
    {
        this.supplierName = supplierName;
    }

    public String getChangeType()
    {
        return changeType;
    }

    public void setChangeType(String changeType)
    {
        this.changeType = changeType;
    }

    public String getOperBy()
    {
        return operBy;
    }

    public void setOperBy(String operBy)
    {
        this.operBy = operBy;
    }

    public Date getOperTime()
    {
        return operTime;
    }

    public void setOperTime(Date operTime)
    {
        this.operTime = operTime;
    }

    public String getChangeSnapshot()
    {
        return changeSnapshot;
    }

    public void setChangeSnapshot(String changeSnapshot)
    {
        this.changeSnapshot = changeSnapshot;
    }
}
