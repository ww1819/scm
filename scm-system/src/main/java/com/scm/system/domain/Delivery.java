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
 * 配送单主表 scm_delivery
 * 
 * @author scm
 */
public class Delivery extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 配送单ID */
    @Excel(name = "配送单ID", cellType = ColumnType.NUMERIC)
    private Long deliveryId;

    /** 配送单号 */
    @Excel(name = "配送单号")
    private String deliveryNo;

    /** 医院ID */
    private Long hospitalId;

    /** 医院名称 */
    @Excel(name = "医院名称")
    private String hospitalName;

    /** 仓库 */
    @Excel(name = "仓库")
    private String warehouse;

    /** 订单ID */
    private Long orderId;

    /** 订单号 */
    @Excel(name = "订单号")
    private String orderNo;

    /** 供应商ID */
    private Long supplierId;

    /** 供应商名称 */
    @Excel(name = "供应商名称")
    private String supplierName;

    /** 配送金额 */
    @Excel(name = "配送金额", cellType = ColumnType.NUMERIC)
    private BigDecimal deliveryAmount;

    /** 单据状态（0未审核 1已审核 2已配送 3已入库） */
    @Excel(name = "单据状态", readConverterExp = "0=未审核,1=已审核,2=已配送,3=已入库")
    private String deliveryStatus;

    /** 配送员 */
    @Excel(name = "配送员")
    private String deliveryPerson;

    /** 配送地址 */
    @Excel(name = "配送地址")
    private String deliveryAddress;

    /** 预计配送时间 */
    @Excel(name = "预计配送时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date expectedDeliveryDate;

    /** 发票号 */
    @Excel(name = "发票号")
    private String invoiceNo;

    /** 发票金额 */
    @Excel(name = "发票金额", cellType = ColumnType.NUMERIC)
    private BigDecimal invoiceAmount;

    /** 发票日期 */
    @Excel(name = "发票日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date invoiceDate;

    /** 订单日期 */
    @Excel(name = "订单日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date orderDate;

    /** 配送明细 */
    private List<DeliveryDetail> deliveryDetails;

    @Size(min = 0, max = 50, message = "配送单号不能超过50个字符")
    public String getDeliveryNo()
    {
        return deliveryNo;
    }

    public void setDeliveryNo(String deliveryNo)
    {
        this.deliveryNo = deliveryNo;
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

    @Size(min = 0, max = 200, message = "仓库不能超过200个字符")
    public String getWarehouse()
    {
        return warehouse;
    }

    public void setWarehouse(String warehouse)
    {
        this.warehouse = warehouse;
    }

    public Long getOrderId()
    {
        return orderId;
    }

    public void setOrderId(Long orderId)
    {
        this.orderId = orderId;
    }

    public String getOrderNo()
    {
        return orderNo;
    }

    public void setOrderNo(String orderNo)
    {
        this.orderNo = orderNo;
    }

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

    public BigDecimal getDeliveryAmount()
    {
        return deliveryAmount;
    }

    public void setDeliveryAmount(BigDecimal deliveryAmount)
    {
        this.deliveryAmount = deliveryAmount;
    }

    public String getDeliveryStatus()
    {
        return deliveryStatus;
    }

    public void setDeliveryStatus(String deliveryStatus)
    {
        this.deliveryStatus = deliveryStatus;
    }

    @Size(min = 0, max = 50, message = "配送员不能超过50个字符")
    public String getDeliveryPerson()
    {
        return deliveryPerson;
    }

    public void setDeliveryPerson(String deliveryPerson)
    {
        this.deliveryPerson = deliveryPerson;
    }

    @Size(min = 0, max = 500, message = "配送地址不能超过500个字符")
    public String getDeliveryAddress()
    {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress)
    {
        this.deliveryAddress = deliveryAddress;
    }

    public Date getExpectedDeliveryDate()
    {
        return expectedDeliveryDate;
    }

    public void setExpectedDeliveryDate(Date expectedDeliveryDate)
    {
        this.expectedDeliveryDate = expectedDeliveryDate;
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

    public BigDecimal getInvoiceAmount()
    {
        return invoiceAmount;
    }

    public void setInvoiceAmount(BigDecimal invoiceAmount)
    {
        this.invoiceAmount = invoiceAmount;
    }

    public Date getInvoiceDate()
    {
        return invoiceDate;
    }

    public void setInvoiceDate(Date invoiceDate)
    {
        this.invoiceDate = invoiceDate;
    }

    public Date getOrderDate()
    {
        return orderDate;
    }

    public void setOrderDate(Date orderDate)
    {
        this.orderDate = orderDate;
    }

    public Long getDeliveryId()
    {
        return deliveryId;
    }

    public void setDeliveryId(Long deliveryId)
    {
        this.deliveryId = deliveryId;
    }

    public List<DeliveryDetail> getDeliveryDetails()
    {
        return deliveryDetails;
    }

    public void setDeliveryDetails(List<DeliveryDetail> deliveryDetails)
    {
        this.deliveryDetails = deliveryDetails;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("deliveryId", getDeliveryId())
            .append("deliveryNo", getDeliveryNo())
            .append("hospitalId", getHospitalId())
            .append("hospitalName", getHospitalName())
            .append("warehouse", getWarehouse())
            .append("orderId", getOrderId())
            .append("orderNo", getOrderNo())
            .append("supplierId", getSupplierId())
            .append("supplierName", getSupplierName())
            .append("deliveryAmount", getDeliveryAmount())
            .append("deliveryStatus", getDeliveryStatus())
            .append("deliveryPerson", getDeliveryPerson())
            .append("deliveryAddress", getDeliveryAddress())
            .append("expectedDeliveryDate", getExpectedDeliveryDate())
            .append("invoiceNo", getInvoiceNo())
            .append("invoiceAmount", getInvoiceAmount())
            .append("invoiceDate", getInvoiceDate())
            .append("orderDate", getOrderDate())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}

