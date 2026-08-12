package com.scm.system.domain.vo;

import java.math.BigDecimal;

/**
 * 合单订单 Sheet：进入合单模式的订单及配送进度
 */
public class CombinedOrderProgressVo
{
    private Long orderId;
    private String orderNo;
    private Long hospitalId;
    private String hospitalName;
    private String warehouse;
    private String deliveryAddress;
    private BigDecimal orderQty;
    private BigDecimal deliveredQty;
    private String progressStatus;

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getHospitalId() { return hospitalId; }
    public void setHospitalId(Long hospitalId) { this.hospitalId = hospitalId; }
    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }
    public String getWarehouse() { return warehouse; }
    public void setWarehouse(String warehouse) { this.warehouse = warehouse; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public BigDecimal getOrderQty() { return orderQty; }
    public void setOrderQty(BigDecimal orderQty) { this.orderQty = orderQty; }
    public BigDecimal getDeliveredQty() { return deliveredQty; }
    public void setDeliveredQty(BigDecimal deliveredQty) { this.deliveredQty = deliveredQty; }
    public String getProgressStatus() { return progressStatus; }
    public void setProgressStatus(String progressStatus) { this.progressStatus = progressStatus; }
}
