package com.scm.system.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * 配送单接口下载记录 scm_delivery_download_log（主键 UUID7；delivery_id 为 varchar 逻辑外键）
 */
public class DeliveryDownloadLog implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 主键 UUID7（36 位含连字符） */
    private String id;
    /** 配送单 ID 字符串（与 scm_delivery.delivery_id 对应） */
    private String deliveryId;
    private Date downloadTime;
    /** SPD_XML / ZS_XML */
    private String downloadChannel;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getDeliveryId()
    {
        return deliveryId;
    }

    public void setDeliveryId(String deliveryId)
    {
        this.deliveryId = deliveryId;
    }

    public Date getDownloadTime()
    {
        return downloadTime;
    }

    public void setDownloadTime(Date downloadTime)
    {
        this.downloadTime = downloadTime;
    }

    public String getDownloadChannel()
    {
        return downloadChannel;
    }

    public void setDownloadChannel(String downloadChannel)
    {
        this.downloadChannel = downloadChannel;
    }
}
