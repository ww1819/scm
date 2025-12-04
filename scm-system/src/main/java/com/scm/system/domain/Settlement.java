package com.scm.system.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import javax.validation.constraints.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.scm.common.annotation.Excel;
import com.scm.common.annotation.Excel.ColumnType;
import com.scm.common.core.domain.BaseEntity;

/**
 * 结算单主表 scm_settlement
 * 
 * @author scm
 */
public class Settlement extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 结算单ID */
    @Excel(name = "结算单ID", cellType = ColumnType.NUMERIC)
    private Long settlementId;

    /** 结算单号 */
    @Excel(name = "结算单号")
    private String settlementNo;

    /** 发票号 */
    @Excel(name = "发票号")
    private String invoiceNo;

    /** 发票日期 */
    @Excel(name = "发票日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date invoiceDate;

    /** 客户ID（医院ID） */
    private Long customerId;

    /** 客户名称（医院名称） */
    @Excel(name = "客户名称")
    private String customerName;

    /** 供应商ID */
    private Long supplierId;

    /** 供应商名称 */
    @Excel(name = "供应商名称")
    private String supplierName;

    /** 总金额 */
    @Excel(name = "总金额", cellType = ColumnType.NUMERIC)
    private BigDecimal totalAmount;

    /** 客户结算状态（0未结算 1已结算） */
    @Excel(name = "客户结算状态", readConverterExp = "0=未结算,1=已结算")
    private String customerSettlementStatus;

    /** 审核状态（0待审核 1已审核 2已拒绝） */
    @Excel(name = "审核状态", readConverterExp = "0=待审核,1=已审核,2=已拒绝")
    private String auditStatus;

    /** 审核人 */
    private String auditBy;

    /** 审核时间 */
    @Excel(name = "审核时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date auditTime;

    /** 客户验收日期 */
    @Excel(name = "客户验收日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date customerAcceptDate;

    /** 经办人 */
    @Excel(name = "经办人")
    private String handler;

    /** 结算明细 */
    private List<SettlementDetail> settlementDetails;

    @NotBlank(message = "结算单号不能为空")
    @Size(min = 0, max = 50, message = "结算单号不能超过50个字符")
    public String getSettlementNo()
    {
        return settlementNo;
    }

    public void setSettlementNo(String settlementNo)
    {
        this.settlementNo = settlementNo;
    }

    @Size(min = 0, max = 50, message = "发票号不能超过50个字符")
    public String getInvoiceNo()
    {
        return invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo)
    {
        this.invoiceNo = invoiceNo;
    }

    public Date getInvoiceDate()
    {
        return invoiceDate;
    }

    public void setInvoiceDate(Date invoiceDate)
    {
        this.invoiceDate = invoiceDate;
    }

    @NotNull(message = "客户ID不能为空")
    public Long getCustomerId()
    {
        return customerId;
    }

    public void setCustomerId(Long customerId)
    {
        this.customerId = customerId;
    }

    public String getCustomerName()
    {
        return customerName;
    }

    public void setCustomerName(String customerName)
    {
        this.customerName = customerName;
    }

    @NotNull(message = "供应商ID不能为空")
    public Long getSupplierId()
    {
        return supplierId;
    }

    public void setSupplierId(Long supplierId)
    {
        this.supplierId = supplierId;
    }

    public String getSupplierName()
    {
        return supplierName;
    }

    public void setSupplierName(String supplierName)
    {
        this.supplierName = supplierName;
    }

    public BigDecimal getTotalAmount()
    {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount)
    {
        this.totalAmount = totalAmount;
    }

    public String getCustomerSettlementStatus()
    {
        return customerSettlementStatus;
    }

    public void setCustomerSettlementStatus(String customerSettlementStatus)
    {
        this.customerSettlementStatus = customerSettlementStatus;
    }

    public String getAuditStatus()
    {
        return auditStatus;
    }

    public void setAuditStatus(String auditStatus)
    {
        this.auditStatus = auditStatus;
    }

    public String getAuditBy()
    {
        return auditBy;
    }

    public void setAuditBy(String auditBy)
    {
        this.auditBy = auditBy;
    }

    public Date getAuditTime()
    {
        return auditTime;
    }

    public void setAuditTime(Date auditTime)
    {
        this.auditTime = auditTime;
    }

    public Date getCustomerAcceptDate()
    {
        return customerAcceptDate;
    }

    public void setCustomerAcceptDate(Date customerAcceptDate)
    {
        this.customerAcceptDate = customerAcceptDate;
    }

    @Size(min = 0, max = 50, message = "经办人不能超过50个字符")
    public String getHandler()
    {
        return handler;
    }

    public void setHandler(String handler)
    {
        this.handler = handler;
    }

    public Long getSettlementId()
    {
        return settlementId;
    }

    public void setSettlementId(Long settlementId)
    {
        this.settlementId = settlementId;
    }

    public List<SettlementDetail> getSettlementDetails()
    {
        return settlementDetails;
    }

    public void setSettlementDetails(List<SettlementDetail> settlementDetails)
    {
        this.settlementDetails = settlementDetails;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("settlementId", getSettlementId())
            .append("settlementNo", getSettlementNo())
            .append("invoiceNo", getInvoiceNo())
            .append("invoiceDate", getInvoiceDate())
            .append("customerId", getCustomerId())
            .append("customerName", getCustomerName())
            .append("supplierId", getSupplierId())
            .append("supplierName", getSupplierName())
            .append("totalAmount", getTotalAmount())
            .append("customerSettlementStatus", getCustomerSettlementStatus())
            .append("auditStatus", getAuditStatus())
            .append("auditBy", getAuditBy())
            .append("auditTime", getAuditTime())
            .append("customerAcceptDate", getCustomerAcceptDate())
            .append("handler", getHandler())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}

