package com.scm.system.domain.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 首页仪表盘统计数据
 */
public class DashboardStatsVO implements Serializable
{
    private static final long serialVersionUID = 1L;

    private long deliveryCount;
    private long orderCount;
    private long settlementCount;
    private BigDecimal monthSettlementAmount = BigDecimal.ZERO;

    private List<String> monthLabels = new ArrayList<>();
    private List<BigDecimal> monthlySalesWan = new ArrayList<>();
    private List<Long> monthlyOrderCounts = new ArrayList<>();

    private List<String> supplierNames = new ArrayList<>();
    private List<BigDecimal> supplierAmounts = new ArrayList<>();

    private List<String> categoryNames = new ArrayList<>();
    private List<BigDecimal> categoryAmounts = new ArrayList<>();

    public long getDeliveryCount()
    {
        return deliveryCount;
    }

    public void setDeliveryCount(long deliveryCount)
    {
        this.deliveryCount = deliveryCount;
    }

    public long getOrderCount()
    {
        return orderCount;
    }

    public void setOrderCount(long orderCount)
    {
        this.orderCount = orderCount;
    }

    public long getSettlementCount()
    {
        return settlementCount;
    }

    public void setSettlementCount(long settlementCount)
    {
        this.settlementCount = settlementCount;
    }

    public BigDecimal getMonthSettlementAmount()
    {
        return monthSettlementAmount;
    }

    public void setMonthSettlementAmount(BigDecimal monthSettlementAmount)
    {
        this.monthSettlementAmount = monthSettlementAmount != null ? monthSettlementAmount : BigDecimal.ZERO;
    }

    public List<String> getMonthLabels()
    {
        return monthLabels;
    }

    public void setMonthLabels(List<String> monthLabels)
    {
        this.monthLabels = monthLabels;
    }

    public List<BigDecimal> getMonthlySalesWan()
    {
        return monthlySalesWan;
    }

    public void setMonthlySalesWan(List<BigDecimal> monthlySalesWan)
    {
        this.monthlySalesWan = monthlySalesWan;
    }

    public List<Long> getMonthlyOrderCounts()
    {
        return monthlyOrderCounts;
    }

    public void setMonthlyOrderCounts(List<Long> monthlyOrderCounts)
    {
        this.monthlyOrderCounts = monthlyOrderCounts;
    }

    public List<String> getSupplierNames()
    {
        return supplierNames;
    }

    public void setSupplierNames(List<String> supplierNames)
    {
        this.supplierNames = supplierNames;
    }

    public List<BigDecimal> getSupplierAmounts()
    {
        return supplierAmounts;
    }

    public void setSupplierAmounts(List<BigDecimal> supplierAmounts)
    {
        this.supplierAmounts = supplierAmounts;
    }

    public List<String> getCategoryNames()
    {
        return categoryNames;
    }

    public void setCategoryNames(List<String> categoryNames)
    {
        this.categoryNames = categoryNames;
    }

    public List<BigDecimal> getCategoryAmounts()
    {
        return categoryAmounts;
    }

    public void setCategoryAmounts(List<BigDecimal> categoryAmounts)
    {
        this.categoryAmounts = categoryAmounts;
    }
}
