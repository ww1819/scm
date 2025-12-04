package com.scm.system.domain;

import java.math.BigDecimal;
import java.util.Date;
import javax.validation.constraints.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.scm.common.annotation.Excel;
import com.scm.common.annotation.Excel.ColumnType;
import com.scm.common.core.domain.BaseEntity;

/**
 * 配送明细表 scm_delivery_detail
 * 
 * @author scm
 */
public class DeliveryDetail extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 明细ID */
    @Excel(name = "明细ID", cellType = ColumnType.NUMERIC)
    private Long detailId;

    /** 配送单ID */
    private Long deliveryId;

    /** 订单明细ID */
    private Long orderDetailId;

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

    /** 剩余配送数量 */
    @Excel(name = "剩余配送数量", cellType = ColumnType.NUMERIC)
    private BigDecimal remainingQuantity;

    /** 配送数量 */
    @Excel(name = "配送数量", cellType = ColumnType.NUMERIC)
    private BigDecimal deliveryQuantity;

    /** 单价 */
    @Excel(name = "单价", cellType = ColumnType.NUMERIC)
    private BigDecimal price;

    /** 金额 */
    @Excel(name = "金额", cellType = ColumnType.NUMERIC)
    private BigDecimal amount;

    /** 批号 */
    @Excel(name = "批号")
    private String batchNo;

    /** 生产日期 */
    @Excel(name = "生产日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date productionDate;

    /** 有效期 */
    @Excel(name = "有效期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date expireDate;

    /** 生产厂家 */
    @Excel(name = "生产厂家")
    private String manufacturer;

    /** 注册证号 */
    @Excel(name = "注册证号")
    private String registerNo;

    /** 配送单号 */
    @Excel(name = "配送单号")
    private String deliveryNo;

    @NotNull(message = "配送单ID不能为空")
    public Long getDeliveryId()
    {
        return deliveryId;
    }

    public void setDeliveryId(Long deliveryId)
    {
        this.deliveryId = deliveryId;
    }

    public Long getOrderDetailId()
    {
        return orderDetailId;
    }

    public void setOrderDetailId(Long orderDetailId)
    {
        this.orderDetailId = orderDetailId;
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

    public BigDecimal getRemainingQuantity()
    {
        return remainingQuantity;
    }

    public void setRemainingQuantity(BigDecimal remainingQuantity)
    {
        this.remainingQuantity = remainingQuantity;
    }

    @NotNull(message = "配送数量不能为空")
    public BigDecimal getDeliveryQuantity()
    {
        return deliveryQuantity;
    }

    public void setDeliveryQuantity(BigDecimal deliveryQuantity)
    {
        this.deliveryQuantity = deliveryQuantity;
    }

    public BigDecimal getPrice()
    {
        return price;
    }

    public void setPrice(BigDecimal price)
    {
        this.price = price;
    }

    public BigDecimal getAmount()
    {
        return amount;
    }

    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }

    @Size(min = 0, max = 50, message = "批号不能超过50个字符")
    public String getBatchNo()
    {
        return batchNo;
    }

    public void setBatchNo(String batchNo)
    {
        this.batchNo = batchNo;
    }

    public Date getProductionDate()
    {
        return productionDate;
    }

    public void setProductionDate(Date productionDate)
    {
        this.productionDate = productionDate;
    }

    public Date getExpireDate()
    {
        return expireDate;
    }

    public void setExpireDate(Date expireDate)
    {
        this.expireDate = expireDate;
    }

    @Size(min = 0, max = 200, message = "生产厂家不能超过200个字符")
    public String getManufacturer()
    {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer)
    {
        this.manufacturer = manufacturer;
    }

    @Size(min = 0, max = 100, message = "注册证号不能超过100个字符")
    public String getRegisterNo()
    {
        return registerNo;
    }

    public void setRegisterNo(String registerNo)
    {
        this.registerNo = registerNo;
    }

    public String getDeliveryNo()
    {
        return deliveryNo;
    }

    public void setDeliveryNo(String deliveryNo)
    {
        this.deliveryNo = deliveryNo;
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
            .append("deliveryId", getDeliveryId())
            .append("orderDetailId", getOrderDetailId())
            .append("materialId", getMaterialId())
            .append("materialCode", getMaterialCode())
            .append("materialName", getMaterialName())
            .append("specification", getSpecification())
            .append("model", getModel())
            .append("unit", getUnit())
            .append("remainingQuantity", getRemainingQuantity())
            .append("deliveryQuantity", getDeliveryQuantity())
            .append("price", getPrice())
            .append("amount", getAmount())
            .append("batchNo", getBatchNo())
            .append("productionDate", getProductionDate())
            .append("expireDate", getExpireDate())
            .append("manufacturer", getManufacturer())
            .append("registerNo", getRegisterNo())
            .append("deliveryNo", getDeliveryNo())
            .toString();
    }
}

