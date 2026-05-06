package com.scm.system.domain;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.scm.common.core.domain.BaseEntity;

/**
 * 医院供应商关联表 scm_hospital_supplier
 * 
 * @author scm
 */
public class HospitalSupplier extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 关联ID */
    private Long relationId;

    /** 医院ID */
    private Long hospitalId;

    /** 供应商ID */
    private Long supplierId;

    /** 绑定状态（0待审核 1已绑定 2已解绑） */
    private String bindStatus;

    /** 绑定时间 */
    private Date bindTime;

    /** 绑定操作人 */
    private String bindBy;

    /** 解绑时间 */
    private Date unbindTime;

    /** 解绑操作人 */
    private String unbindBy;

    /** 状态（0正常 1停用） */
    private String status;

    /** 关联审核状态（0待审核 1已通过 2已拒绝） */
    private String auditStatus;

    /** 关联审核时间 */
    private Date auditTime;

    /** 关联审核人 */
    private String auditBy;

    /** 停用状态（0启用 1停用） */
    private String disableStatus;

    /** 停用时间 */
    private Date stopTime;

    /** 停用操作人 */
    private String stopBy;

    /** 供货开始日期 */
    private Date supplyStartDate;

    /** 供货结束日期 */
    private Date supplyEndDate;

    /** 医院名称 */
    private String hospitalName;

    /** 供应商名称 */
    private String supplierName;

    /** 医院编码（关联 scm_hospital，列表展示用） */
    private String hospitalCode;

    public Long getRelationId()
    {
        return relationId;
    }

    public void setRelationId(Long relationId)
    {
        this.relationId = relationId;
    }

    public Long getHospitalId()
    {
        return hospitalId;
    }

    public void setHospitalId(Long hospitalId)
    {
        this.hospitalId = hospitalId;
    }

    public Long getSupplierId()
    {
        return supplierId;
    }

    public void setSupplierId(Long supplierId)
    {
        this.supplierId = supplierId;
    }

    public String getBindStatus()
    {
        return bindStatus;
    }

    public void setBindStatus(String bindStatus)
    {
        this.bindStatus = bindStatus;
    }

    public Date getBindTime()
    {
        return bindTime;
    }

    public void setBindTime(Date bindTime)
    {
        this.bindTime = bindTime;
    }

    public String getBindBy()
    {
        return bindBy;
    }

    public void setBindBy(String bindBy)
    {
        this.bindBy = bindBy;
    }

    public Date getUnbindTime()
    {
        return unbindTime;
    }

    public void setUnbindTime(Date unbindTime)
    {
        this.unbindTime = unbindTime;
    }

    public String getUnbindBy()
    {
        return unbindBy;
    }

    public void setUnbindBy(String unbindBy)
    {
        this.unbindBy = unbindBy;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getAuditStatus()
    {
        return auditStatus;
    }

    public void setAuditStatus(String auditStatus)
    {
        this.auditStatus = auditStatus;
    }

    public Date getAuditTime()
    {
        return auditTime;
    }

    public void setAuditTime(Date auditTime)
    {
        this.auditTime = auditTime;
    }

    public String getAuditBy()
    {
        return auditBy;
    }

    public void setAuditBy(String auditBy)
    {
        this.auditBy = auditBy;
    }

    public String getDisableStatus()
    {
        return disableStatus;
    }

    public void setDisableStatus(String disableStatus)
    {
        this.disableStatus = disableStatus;
    }

    public Date getStopTime()
    {
        return stopTime;
    }

    public void setStopTime(Date stopTime)
    {
        this.stopTime = stopTime;
    }

    public String getStopBy()
    {
        return stopBy;
    }

    public void setStopBy(String stopBy)
    {
        this.stopBy = stopBy;
    }

    public Date getSupplyStartDate()
    {
        return supplyStartDate;
    }

    public void setSupplyStartDate(Date supplyStartDate)
    {
        this.supplyStartDate = supplyStartDate;
    }

    public Date getSupplyEndDate()
    {
        return supplyEndDate;
    }

    public void setSupplyEndDate(Date supplyEndDate)
    {
        this.supplyEndDate = supplyEndDate;
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

    public String getHospitalCode()
    {
        return hospitalCode;
    }

    public void setHospitalCode(String hospitalCode)
    {
        this.hospitalCode = hospitalCode;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("relationId", getRelationId())
            .append("hospitalId", getHospitalId())
            .append("supplierId", getSupplierId())
            .append("bindStatus", getBindStatus())
            .append("bindTime", getBindTime())
            .append("bindBy", getBindBy())
            .append("unbindTime", getUnbindTime())
            .append("unbindBy", getUnbindBy())
            .append("status", getStatus())
            .append("auditStatus", getAuditStatus())
            .append("auditTime", getAuditTime())
            .append("auditBy", getAuditBy())
            .append("disableStatus", getDisableStatus())
            .append("stopTime", getStopTime())
            .append("stopBy", getStopBy())
            .append("supplyStartDate", getSupplyStartDate())
            .append("supplyEndDate", getSupplyEndDate())
            .append("hospitalName", getHospitalName())
            .append("hospitalCode", getHospitalCode())
            .append("supplierName", getSupplierName())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}

