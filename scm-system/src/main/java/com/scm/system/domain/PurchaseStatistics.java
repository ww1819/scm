package com.scm.system.domain;

import java.math.BigDecimal;
import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.scm.common.annotation.Excel;
import com.scm.common.annotation.Excel.ColumnType;
import com.scm.common.core.domain.BaseEntity;

/**
 * 采购统计表 scm_purchase_statistics
 * 
 * @author scm
 */
public class PurchaseStatistics extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 统计ID */
    @Excel(name = "统计ID", cellType = ColumnType.NUMERIC)
    private Long statisticsId;

    /** 统计年份 */
    @Excel(name = "统计年份", cellType = ColumnType.NUMERIC)
    private Integer statisticsYear;

    /** 统计月份 */
    @Excel(name = "统计月份", cellType = ColumnType.NUMERIC)
    private Integer statisticsMonth;

    /** 医院ID */
    private Long hospitalId;

    /** 医院名称 */
    @Excel(name = "医院名称")
    private String hospitalName;

    /** 供应商ID */
    private Long supplierId;

    /** 供应商名称 */
    @Excel(name = "供应商名称")
    private String supplierName;

    /** 物资ID */
    private Long materialId;

    /** 物资名称 */
    @Excel(name = "物资名称")
    private String materialName;

    /** 采购数量 */
    @Excel(name = "采购数量", cellType = ColumnType.NUMERIC)
    private BigDecimal purchaseQuantity;

    /** 采购金额 */
    @Excel(name = "采购金额", cellType = ColumnType.NUMERIC)
    private BigDecimal purchaseAmount;

    /** 订单数量 */
    @Excel(name = "订单数量", cellType = ColumnType.NUMERIC)
    private Integer orderCount;

    public Long getStatisticsId()
    {
        return statisticsId;
    }

    public void setStatisticsId(Long statisticsId)
    {
        this.statisticsId = statisticsId;
    }

    public Integer getStatisticsYear()
    {
        return statisticsYear;
    }

    public void setStatisticsYear(Integer statisticsYear)
    {
        this.statisticsYear = statisticsYear;
    }

    public Integer getStatisticsMonth()
    {
        return statisticsMonth;
    }

    public void setStatisticsMonth(Integer statisticsMonth)
    {
        this.statisticsMonth = statisticsMonth;
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

    public Long getMaterialId()
    {
        return materialId;
    }

    public void setMaterialId(Long materialId)
    {
        this.materialId = materialId;
    }

    public String getMaterialName()
    {
        return materialName;
    }

    public void setMaterialName(String materialName)
    {
        this.materialName = materialName;
    }

    public BigDecimal getPurchaseQuantity()
    {
        return purchaseQuantity;
    }

    public void setPurchaseQuantity(BigDecimal purchaseQuantity)
    {
        this.purchaseQuantity = purchaseQuantity;
    }

    public BigDecimal getPurchaseAmount()
    {
        return purchaseAmount;
    }

    public void setPurchaseAmount(BigDecimal purchaseAmount)
    {
        this.purchaseAmount = purchaseAmount;
    }

    public Integer getOrderCount()
    {
        return orderCount;
    }

    public void setOrderCount(Integer orderCount)
    {
        this.orderCount = orderCount;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("statisticsId", getStatisticsId())
            .append("statisticsYear", getStatisticsYear())
            .append("statisticsMonth", getStatisticsMonth())
            .append("hospitalId", getHospitalId())
            .append("hospitalName", getHospitalName())
            .append("supplierId", getSupplierId())
            .append("supplierName", getSupplierName())
            .append("materialId", getMaterialId())
            .append("materialName", getMaterialName())
            .append("purchaseQuantity", getPurchaseQuantity())
            .append("purchaseAmount", getPurchaseAmount())
            .append("orderCount", getOrderCount())
            .toString();
    }
}

