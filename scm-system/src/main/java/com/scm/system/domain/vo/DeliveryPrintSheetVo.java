package com.scm.system.domain.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import com.scm.system.domain.Delivery;
import com.scm.system.domain.DeliveryDetail;

/**
 * 配送单打印/打印样式导出：单张「物资配送单」页所需数据（与单页 print 模板字段一致）
 */
public class DeliveryPrintSheetVo implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Delivery delivery;

    private List<DeliveryDetail> deliveryDetails;

    private int totalQuantity;

    private BigDecimal printTotalAmount;

    private String printInputCode;

    public Delivery getDelivery()
    {
        return delivery;
    }

    public void setDelivery(Delivery delivery)
    {
        this.delivery = delivery;
    }

    public List<DeliveryDetail> getDeliveryDetails()
    {
        return deliveryDetails;
    }

    public void setDeliveryDetails(List<DeliveryDetail> deliveryDetails)
    {
        this.deliveryDetails = deliveryDetails;
    }

    public int getTotalQuantity()
    {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity)
    {
        this.totalQuantity = totalQuantity;
    }

    public BigDecimal getPrintTotalAmount()
    {
        return printTotalAmount;
    }

    public void setPrintTotalAmount(BigDecimal printTotalAmount)
    {
        this.printTotalAmount = printTotalAmount;
    }

    public String getPrintInputCode()
    {
        return printInputCode;
    }

    public void setPrintInputCode(String printInputCode)
    {
        this.printInputCode = printInputCode;
    }
}
