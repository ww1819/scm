package com.scm.system.domain;

import java.io.Serializable;

/**
 * 中设订单明细与配送单明细关联 zs_tp_order_detail_delivery_rel
 */
public class ZsTpOrderDetailDeliveryRel implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String id;
    private String orderDetailId;
    private String orderId;
    private String orderNo;
    private String deliveryId;
    private String deliveryNo;
    private String deliveryDetailId;
    private String createTime;
    private String createBy;
    private String tenantId;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getOrderDetailId()
    {
        return orderDetailId;
    }

    public void setOrderDetailId(String orderDetailId)
    {
        this.orderDetailId = orderDetailId;
    }

    public String getOrderId()
    {
        return orderId;
    }

    public void setOrderId(String orderId)
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

    public String getDeliveryId()
    {
        return deliveryId;
    }

    public void setDeliveryId(String deliveryId)
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

    public String getDeliveryDetailId()
    {
        return deliveryDetailId;
    }

    public void setDeliveryDetailId(String deliveryDetailId)
    {
        this.deliveryDetailId = deliveryDetailId;
    }

    public String getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(String createTime)
    {
        this.createTime = createTime;
    }

    public String getCreateBy()
    {
        return createBy;
    }

    public void setCreateBy(String createBy)
    {
        this.createBy = createBy;
    }

    public String getTenantId()
    {
        return tenantId;
    }

    public void setTenantId(String tenantId)
    {
        this.tenantId = tenantId;
    }
}
