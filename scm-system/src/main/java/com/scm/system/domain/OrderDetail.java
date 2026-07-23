package com.scm.system.domain;

import java.math.BigDecimal;
import java.util.Date;
import javax.validation.constraints.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.scm.common.annotation.Excel;
import com.scm.common.annotation.Excel.ColumnType;
import com.scm.common.core.domain.BaseEntity;

/**
 * 订单明细表 scm_order_detail
 * 
 * @author scm
 */
public class OrderDetail extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 明细ID */
    @Excel(name = "明细ID", cellType = ColumnType.NUMERIC)
    private Long detailId;

    /** 订单ID */
    private Long orderId;

    /** 订单编号（冗余自 scm_order） */
    @Excel(name = "订单编号")
    private String orderNo;

    /** 医院ID（冗余自 scm_order） */
    private Long hospitalId;

    /** 平台医院编码（冗余） */
    private String hospitalCode;

    /** 平台供应商ID（冗余自 scm_order.supplier_id） */
    private Long supplierId;

    /** 平台供应商编码（冗余） */
    @Excel(name = "供应商编码")
    private String supplierCode;

    /** SPD 明细主键 purchase_order_entry.id */
    private Long spdEntryId;

    /** 物资ID */
    private Long materialId;

    /** 产品编码 */
    @Excel(name = "产品编码")
    private String materialCode;

    /** 产品名称 */
    @Excel(name = "产品名称")
    private String materialName;

    /** 规格 */
    @Excel(name = "规格")
    private String specification;

    /** 型号 */
    @Excel(name = "型号")
    private String model;

    /** 单位 */
    @Excel(name = "单位")
    private String unit;

    /** 采购价格 */
    @Excel(name = "采购价格", cellType = ColumnType.NUMERIC)
    private BigDecimal purchasePrice;

    /** 订货数量 */
    @Excel(name = "订货数量", cellType = ColumnType.NUMERIC)
    private Integer orderQuantity;

    /** 剩余待配送数 */
    @Excel(name = "剩余待配送数", cellType = ColumnType.NUMERIC)
    private Integer remainingQuantity;

    /** 金额 */
    @Excel(name = "金额", cellType = ColumnType.NUMERIC)
    private BigDecimal amount;

    /** 打包系数 */
    private BigDecimal packCoefficient;

    /** 厂家 */
    @Excel(name = "厂家")
    private String manufacturer;

    /** 注册证号 */
    @Excel(name = "注册证号")
    private String registerNo;

    /** 注册证有效期（关联产品证件） */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Excel(name = "注册证有效期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date registerExpireDate;

    /** 国家医保编码 */
    @Excel(name = "国家医保编码")
    private String nationalInsuranceCode;

    /** 阳光采购平台编码 */
    @Excel(name = "阳光采购平台编码")
    private String sunshineProcurementCode;

    /** 已审核配送数量（关联配送单审核通过） */
    private BigDecimal deliveredAuditedQty;

    /** 待审核配送数量（配送单未审核） */
    private BigDecimal deliveredPendingAuditQty;

    /** 未配送数量（含审核拒绝未计入已发数量部分） */
    private BigDecimal undeliveredQty;

    @NotNull(message = "订单ID不能为空")
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

    public Long getHospitalId()
    {
        return hospitalId;
    }

    public void setHospitalId(Long hospitalId)
    {
        this.hospitalId = hospitalId;
    }

    public String getHospitalCode()
    {
        return hospitalCode;
    }

    public void setHospitalCode(String hospitalCode)
    {
        this.hospitalCode = hospitalCode;
    }

    public Long getSupplierId()
    {
        return supplierId;
    }

    public void setSupplierId(Long supplierId)
    {
        this.supplierId = supplierId;
    }

    public String getSupplierCode()
    {
        return supplierCode;
    }

    public void setSupplierCode(String supplierCode)
    {
        this.supplierCode = supplierCode;
    }

    public Long getSpdEntryId()
    {
        return spdEntryId;
    }

    public void setSpdEntryId(Long spdEntryId)
    {
        this.spdEntryId = spdEntryId;
    }

    @NotNull(message = "物资ID不能为空")
    public Long getMaterialId()
    {
        return materialId;
    }

    public void setMaterialId(Long materialId)
    {
        this.materialId = materialId;
    }

    public String getMaterialCode()
    {
        return materialCode;
    }

    public void setMaterialCode(String materialCode)
    {
        this.materialCode = materialCode;
    }

    public String getMaterialName()
    {
        return materialName;
    }

    public void setMaterialName(String materialName)
    {
        this.materialName = materialName;
    }

    public String getSpecification()
    {
        return specification;
    }

    public void setSpecification(String specification)
    {
        this.specification = specification;
    }

    public String getModel()
    {
        return model;
    }

    public void setModel(String model)
    {
        this.model = model;
    }

    public String getUnit()
    {
        return unit;
    }

    public void setUnit(String unit)
    {
        this.unit = unit;
    }

    public BigDecimal getPurchasePrice()
    {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice)
    {
        this.purchasePrice = purchasePrice;
    }

    @NotNull(message = "订货数量不能为空")
    public Integer getOrderQuantity()
    {
        return orderQuantity;
    }

    public void setOrderQuantity(Integer orderQuantity)
    {
        this.orderQuantity = orderQuantity;
    }

    public Integer getRemainingQuantity()
    {
        return remainingQuantity;
    }

    public void setRemainingQuantity(Integer remainingQuantity)
    {
        this.remainingQuantity = remainingQuantity;
    }

    public BigDecimal getAmount()
    {
        return amount;
    }

    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }

    public BigDecimal getPackCoefficient()
    {
        return packCoefficient;
    }

    public void setPackCoefficient(BigDecimal packCoefficient)
    {
        this.packCoefficient = packCoefficient;
    }

    public String getManufacturer()
    {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer)
    {
        this.manufacturer = manufacturer;
    }

    public String getRegisterNo()
    {
        return registerNo;
    }

    public void setRegisterNo(String registerNo)
    {
        this.registerNo = registerNo;
    }

    public Date getRegisterExpireDate()
    {
        return registerExpireDate;
    }

    public void setRegisterExpireDate(Date registerExpireDate)
    {
        this.registerExpireDate = registerExpireDate;
    }

    public String getNationalInsuranceCode()
    {
        return nationalInsuranceCode;
    }

    public void setNationalInsuranceCode(String nationalInsuranceCode)
    {
        this.nationalInsuranceCode = nationalInsuranceCode;
    }

    public String getSunshineProcurementCode()
    {
        return sunshineProcurementCode;
    }

    public void setSunshineProcurementCode(String sunshineProcurementCode)
    {
        this.sunshineProcurementCode = sunshineProcurementCode;
    }

    public BigDecimal getDeliveredAuditedQty()
    {
        return deliveredAuditedQty;
    }

    public void setDeliveredAuditedQty(BigDecimal deliveredAuditedQty)
    {
        this.deliveredAuditedQty = deliveredAuditedQty;
    }

    public BigDecimal getDeliveredPendingAuditQty()
    {
        return deliveredPendingAuditQty;
    }

    public void setDeliveredPendingAuditQty(BigDecimal deliveredPendingAuditQty)
    {
        this.deliveredPendingAuditQty = deliveredPendingAuditQty;
    }

    public BigDecimal getUndeliveredQty()
    {
        return undeliveredQty;
    }

    public void setUndeliveredQty(BigDecimal undeliveredQty)
    {
        this.undeliveredQty = undeliveredQty;
    }

    public Long getDetailId()
    {
        return detailId;
    }

    public void setDetailId(Long detailId)
    {
        this.detailId = detailId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("detailId", getDetailId())
            .append("orderId", getOrderId())
            .append("orderNo", getOrderNo())
            .append("hospitalId", getHospitalId())
            .append("hospitalCode", getHospitalCode())
            .append("supplierId", getSupplierId())
            .append("supplierCode", getSupplierCode())
            .append("spdEntryId", getSpdEntryId())
            .append("materialId", getMaterialId())
            .append("materialCode", getMaterialCode())
            .append("materialName", getMaterialName())
            .append("specification", getSpecification())
            .append("model", getModel())
            .append("unit", getUnit())
            .append("purchasePrice", getPurchasePrice())
            .append("orderQuantity", getOrderQuantity())
            .append("remainingQuantity", getRemainingQuantity())
            .append("amount", getAmount())
            .append("packCoefficient", getPackCoefficient())
            .append("manufacturer", getManufacturer())
            .append("registerNo", getRegisterNo())
            .append("registerExpireDate", getRegisterExpireDate())
            .append("nationalInsuranceCode", getNationalInsuranceCode())
            .append("sunshineProcurementCode", getSunshineProcurementCode())
            .toString();
    }
}

