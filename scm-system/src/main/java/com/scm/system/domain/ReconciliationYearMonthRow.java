package com.scm.system.domain;

import java.math.BigDecimal;

/**
 * 对账表 — 年度按月汇总行
 */
public class ReconciliationYearMonthRow
{
    /** 月份 1-12 */
    private Integer month;

    /** 月份显示，如「一月」 */
    private String monthLabel;

    /** 配送总金额 */
    private BigDecimal deliveryAmount;

    /** 配送总数量 */
    private BigDecimal deliveryQuantity;

    /** 结算总金额 */
    private BigDecimal settlementAmount;

    /** 结算总数量 */
    private BigDecimal settlementQuantity;

    public Integer getMonth()
    {
        return month;
    }

    public void setMonth(Integer month)
    {
        this.month = month;
    }

    public String getMonthLabel()
    {
        return monthLabel;
    }

    public void setMonthLabel(String monthLabel)
    {
        this.monthLabel = monthLabel;
    }

    public BigDecimal getDeliveryAmount()
    {
        return deliveryAmount;
    }

    public void setDeliveryAmount(BigDecimal deliveryAmount)
    {
        this.deliveryAmount = deliveryAmount;
    }

    public BigDecimal getDeliveryQuantity()
    {
        return deliveryQuantity;
    }

    public void setDeliveryQuantity(BigDecimal deliveryQuantity)
    {
        this.deliveryQuantity = deliveryQuantity;
    }

    public BigDecimal getSettlementAmount()
    {
        return settlementAmount;
    }

    public void setSettlementAmount(BigDecimal settlementAmount)
    {
        this.settlementAmount = settlementAmount;
    }

    public BigDecimal getSettlementQuantity()
    {
        return settlementQuantity;
    }

    public void setSettlementQuantity(BigDecimal settlementQuantity)
    {
        this.settlementQuantity = settlementQuantity;
    }
}
