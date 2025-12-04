package com.scm.system.domain;

import java.math.BigDecimal;
import javax.validation.constraints.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
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

    /** 厂家 */
    @Excel(name = "厂家")
    private String manufacturer;

    /** 注册证号 */
    @Excel(name = "注册证号")
    private String registerNo;

    @NotNull(message = "订单ID不能为空")
    public Long getOrderId()
    {
        return orderId;
    }

    public void setOrderId(Long orderId)
    {
        this.orderId = orderId;
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
            .append("manufacturer", getManufacturer())
            .append("registerNo", getRegisterNo())
            .toString();
    }
}

