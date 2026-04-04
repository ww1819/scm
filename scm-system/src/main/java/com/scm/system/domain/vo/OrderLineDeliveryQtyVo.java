package com.scm.system.domain.vo;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单明细行维度的配送数量汇总（已审核 / 待审核 / 已拒绝）
 */
public class OrderLineDeliveryQtyVo implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 订单明细主键（与我方明细 Long 或中设明细 id 字符串一致） */
    private String lineKey;

    private BigDecimal auditedQty;
    private BigDecimal pendingQty;
    private BigDecimal rejectedQty;

    public String getLineKey()
    {
        return lineKey;
    }

    public void setLineKey(String lineKey)
    {
        this.lineKey = lineKey;
    }

    public BigDecimal getAuditedQty()
    {
        return auditedQty;
    }

    public void setAuditedQty(BigDecimal auditedQty)
    {
        this.auditedQty = auditedQty;
    }

    public BigDecimal getPendingQty()
    {
        return pendingQty;
    }

    public void setPendingQty(BigDecimal pendingQty)
    {
        this.pendingQty = pendingQty;
    }

    public BigDecimal getRejectedQty()
    {
        return rejectedQty;
    }

    public void setRejectedQty(BigDecimal rejectedQty)
    {
        this.rejectedQty = rejectedQty;
    }
}
