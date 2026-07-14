package com.scm.system.domain;

import java.math.BigDecimal;
import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.scm.common.core.domain.BaseEntity;

/**
 * 对账表 scm_reconciliation
 */
public class ScmReconciliation extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long reconciliationId;

    /** 医院ID */
    private Long hospitalId;

    /** 医院名称 */
    private String hospitalName;

    /** 对账日期 */
    private Date reconcileDate;

    /** HIS金额 */
    private BigDecimal hisAmount;

    /** SPD金额 */
    private BigDecimal spdAmount;

    /** 品规总数 */
    private Integer itemCount;

    /** 异常记录数 */
    private Integer abnormalCount;

    /** 状态（0未生成 1已生成） */
    private String status;

    /** 查询：月份开始 yyyy-MM-dd */
    private String monthBegin;

    /** 查询：月份结束 yyyy-MM-dd */
    private String monthEnd;

    public Long getReconciliationId()
    {
        return reconciliationId;
    }

    public void setReconciliationId(Long reconciliationId)
    {
        this.reconciliationId = reconciliationId;
    }

    public Long getHospitalId()
    {
        return hospitalId;
    }

    public void setHospitalId(Long hospitalId)
    {
        this.hospitalId = hospitalId;
    }

    public String getHospitalName()
    {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName)
    {
        this.hospitalName = hospitalName;
    }

    public Date getReconcileDate()
    {
        return reconcileDate;
    }

    public void setReconcileDate(Date reconcileDate)
    {
        this.reconcileDate = reconcileDate;
    }

    public BigDecimal getHisAmount()
    {
        return hisAmount;
    }

    public void setHisAmount(BigDecimal hisAmount)
    {
        this.hisAmount = hisAmount;
    }

    public BigDecimal getSpdAmount()
    {
        return spdAmount;
    }

    public void setSpdAmount(BigDecimal spdAmount)
    {
        this.spdAmount = spdAmount;
    }

    public Integer getItemCount()
    {
        return itemCount;
    }

    public void setItemCount(Integer itemCount)
    {
        this.itemCount = itemCount;
    }

    public Integer getAbnormalCount()
    {
        return abnormalCount;
    }

    public void setAbnormalCount(Integer abnormalCount)
    {
        this.abnormalCount = abnormalCount;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getMonthBegin()
    {
        return monthBegin;
    }

    public void setMonthBegin(String monthBegin)
    {
        this.monthBegin = monthBegin;
    }

    public String getMonthEnd()
    {
        return monthEnd;
    }

    public void setMonthEnd(String monthEnd)
    {
        this.monthEnd = monthEnd;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("reconciliationId", getReconciliationId())
            .append("hospitalId", getHospitalId())
            .append("hospitalName", getHospitalName())
            .append("reconcileDate", getReconcileDate())
            .append("hisAmount", getHisAmount())
            .append("spdAmount", getSpdAmount())
            .append("itemCount", getItemCount())
            .append("abnormalCount", getAbnormalCount())
            .append("status", getStatus())
            .toString();
    }
}
