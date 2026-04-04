package com.scm.system.domain.vo;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单明细关联的配送单行（用于弹窗列表）
 */
public class OrderDetailDeliveryTraceVo implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long deliveryId;
    private String deliveryNo;
    private Long deliveryDetailId;
    private String auditStatus;
    private String deliveryStatus;
    private BigDecimal deliveryQuantity;

    public Long getDeliveryId()
    {
        return deliveryId;
    }

    public void setDeliveryId(Long deliveryId)
    {
        this.deliveryId = deliveryId;
    }

    public String getDeliveryNo()
    {
        return deliveryNo;
    }

    public void setDeliveryNo(String deliveryNo)
    {
        this.deliveryNo = deliveryNo;
    }

    public Long getDeliveryDetailId()
    {
        return deliveryDetailId;
    }

    public void setDeliveryDetailId(Long deliveryDetailId)
    {
        this.deliveryDetailId = deliveryDetailId;
    }

    public String getAuditStatus()
    {
        return auditStatus;
    }

    public void setAuditStatus(String auditStatus)
    {
        this.auditStatus = auditStatus;
    }

    public String getDeliveryStatus()
    {
        return deliveryStatus;
    }

    public void setDeliveryStatus(String deliveryStatus)
    {
        this.deliveryStatus = deliveryStatus;
    }

    public BigDecimal getDeliveryQuantity()
    {
        return deliveryQuantity;
    }

    public void setDeliveryQuantity(BigDecimal deliveryQuantity)
    {
        this.deliveryQuantity = deliveryQuantity;
    }
}
