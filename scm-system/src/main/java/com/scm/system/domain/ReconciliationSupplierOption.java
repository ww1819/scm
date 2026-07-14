package com.scm.system.domain;

/**
 * 对账表 — 供应商下拉项
 */
public class ReconciliationSupplierOption
{
    private Long supplierId;

    private String supplierName;

    private String supplierCode;

    private String pinyinCode;

    public Long getSupplierId()
    {
        return supplierId;
    }

    public void setSupplierId(Long supplierId)
    {
        this.supplierId = supplierId;
    }

    public String getSupplierName()
    {
        return supplierName;
    }

    public void setSupplierName(String supplierName)
    {
        this.supplierName = supplierName;
    }

    public String getSupplierCode()
    {
        return supplierCode;
    }

    public void setSupplierCode(String supplierCode)
    {
        this.supplierCode = supplierCode;
    }

    public String getPinyinCode()
    {
        return pinyinCode;
    }

    public void setPinyinCode(String pinyinCode)
    {
        this.pinyinCode = pinyinCode;
    }
}
