package com.scm.system.domain;

import java.util.Date;
import com.scm.common.core.domain.BaseEntity;

/**
 * 供应商资质变更抄送记录 scm_supplier_cert_change_log
 */
public class ScmSupplierCertChangeLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private String logId;
    private Long supplierId;
    private Long hospitalId;
    private Long certificateId;
    /** INSERT / UPDATE / DELETE / AUDIT */
    private String changeType;
    private String beforeJson;
    private String afterJson;
    private Date createTime;

    public String getLogId()
    {
        return logId;
    }

    public void setLogId(String logId)
    {
        this.logId = logId;
    }

    public Long getSupplierId()
    {
        return supplierId;
    }

    public void setSupplierId(Long supplierId)
    {
        this.supplierId = supplierId;
    }

    public Long getHospitalId()
    {
        return hospitalId;
    }

    public void setHospitalId(Long hospitalId)
    {
        this.hospitalId = hospitalId;
    }

    public Long getCertificateId()
    {
        return certificateId;
    }

    public void setCertificateId(Long certificateId)
    {
        this.certificateId = certificateId;
    }

    public String getChangeType()
    {
        return changeType;
    }

    public void setChangeType(String changeType)
    {
        this.changeType = changeType;
    }

    public String getBeforeJson()
    {
        return beforeJson;
    }

    public void setBeforeJson(String beforeJson)
    {
        this.beforeJson = beforeJson;
    }

    public String getAfterJson()
    {
        return afterJson;
    }

    public void setAfterJson(String afterJson)
    {
        this.afterJson = afterJson;
    }

    @Override
    public Date getCreateTime()
    {
        return createTime;
    }

    @Override
    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }
}
