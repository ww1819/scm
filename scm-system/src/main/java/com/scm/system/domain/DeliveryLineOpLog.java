package com.scm.system.domain;

import java.util.Date;
import com.scm.common.core.domain.BaseEntity;

/**
 * 配送/合单明细操作日志 scm_delivery_line_op_log（主键 UUID7，外键均为字符串）
 */
public class DeliveryLineOpLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    public static final String KIND_DELIVERY = "DELIVERY";
    public static final String KIND_COMBINED = "COMBINED";
    public static final String ACTION_INSERT = "INSERT";
    public static final String ACTION_UPDATE = "UPDATE";
    public static final String ACTION_DELETE = "DELETE";
    public static final String ACTION_PRINT = "PRINT";

    private String logId;
    private String billKind;
    private String billId;
    private String billNo;
    private String detailId;
    private String orderId;
    private String orderNo;
    private String orderDetailId;
    private String action;
    private String beforeJson;
    private String afterJson;
    private String relatedBillId;
    private String relatedBillNo;
    private String operName;
    private Date operTime;

    public String getLogId() { return logId; }
    public void setLogId(String logId) { this.logId = logId; }
    public String getBillKind() { return billKind; }
    public void setBillKind(String billKind) { this.billKind = billKind; }
    public String getBillId() { return billId; }
    public void setBillId(String billId) { this.billId = billId; }
    public String getBillNo() { return billNo; }
    public void setBillNo(String billNo) { this.billNo = billNo; }
    public String getDetailId() { return detailId; }
    public void setDetailId(String detailId) { this.detailId = detailId; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public String getOrderDetailId() { return orderDetailId; }
    public void setOrderDetailId(String orderDetailId) { this.orderDetailId = orderDetailId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getBeforeJson() { return beforeJson; }
    public void setBeforeJson(String beforeJson) { this.beforeJson = beforeJson; }
    public String getAfterJson() { return afterJson; }
    public void setAfterJson(String afterJson) { this.afterJson = afterJson; }
    public String getRelatedBillId() { return relatedBillId; }
    public void setRelatedBillId(String relatedBillId) { this.relatedBillId = relatedBillId; }
    public String getRelatedBillNo() { return relatedBillNo; }
    public void setRelatedBillNo(String relatedBillNo) { this.relatedBillNo = relatedBillNo; }
    public String getOperName() { return operName; }
    public void setOperName(String operName) { this.operName = operName; }
    public Date getOperTime() { return operTime; }
    public void setOperTime(Date operTime) { this.operTime = operTime; }
}
