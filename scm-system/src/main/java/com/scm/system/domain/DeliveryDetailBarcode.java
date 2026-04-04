package com.scm.system.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * 配送单明细条码从表 scm_delivery_detail_barcode
 */
public class DeliveryDetailBarcode implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String id;
    private Long deliveryId;
    private String deliveryNo;
    private Long deliveryDetailId;
    private Long seedNum;
    private String barcodeNo;
    private Date createTime;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

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

    public Long getSeedNum()
    {
        return seedNum;
    }

    public void setSeedNum(Long seedNum)
    {
        this.seedNum = seedNum;
    }

    public String getBarcodeNo()
    {
        return barcodeNo;
    }

    public void setBarcodeNo(String barcodeNo)
    {
        this.barcodeNo = barcodeNo;
    }

    public Date getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }
}
