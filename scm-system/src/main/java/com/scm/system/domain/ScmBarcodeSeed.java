package com.scm.system.domain;

import java.io.Serializable;

/**
 * 中设条码种子序列表 scm_barcode_seed
 */
public class ScmBarcodeSeed implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String id;
    private String counterType;
    private String tenantId;
    private String zsCustomerId;
    private String warehouseId;
    private String highLowFlag;
    private Long seedValue;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getCounterType()
    {
        return counterType;
    }

    public void setCounterType(String counterType)
    {
        this.counterType = counterType;
    }

    public String getTenantId()
    {
        return tenantId;
    }

    public void setTenantId(String tenantId)
    {
        this.tenantId = tenantId;
    }

    public String getZsCustomerId()
    {
        return zsCustomerId;
    }

    public void setZsCustomerId(String zsCustomerId)
    {
        this.zsCustomerId = zsCustomerId;
    }

    public String getWarehouseId()
    {
        return warehouseId;
    }

    public void setWarehouseId(String warehouseId)
    {
        this.warehouseId = warehouseId;
    }

    public String getHighLowFlag()
    {
        return highLowFlag;
    }

    public void setHighLowFlag(String highLowFlag)
    {
        this.highLowFlag = highLowFlag;
    }

    public Long getSeedValue()
    {
        return seedValue;
    }

    public void setSeedValue(Long seedValue)
    {
        this.seedValue = seedValue;
    }
}
