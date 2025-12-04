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
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}

