package com.scm.system.domain.vo;

import java.math.BigDecimal;
import java.util.Date;
import com.scm.common.annotation.Excel;
import com.scm.common.annotation.Excel.ColumnType;

/**
 * 产品证件目录导入模板行
 */
public class ProductCertificateImportVo
{
    @Excel(name = "产品名称")
    private String materialName;

    @Excel(name = "注册证号")
    private String registerNo;

    @Excel(name = "规格")
    private String specification;

    @Excel(name = "型号")
    private String model;

    @Excel(name = "单位")
    private String unit;

    @Excel(name = "生产厂家")
    private String manufacturerName;

    @Excel(name = "采购价格", cellType = ColumnType.NUMERIC)
    private BigDecimal purchasePrice;

    @Excel(name = "注册证名称")
    private String registerName;

    @Excel(name = "条码号(UDI)")
    private String udiCode;

    @Excel(name = "注册证发证日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date registerIssueDate;

    @Excel(name = "有效期至", width = 30, dateFormat = "yyyy-MM-dd")
    private Date expireDate;

    @Excel(name = "产品类别")
    private String productCategory;

    @Excel(name = "供应商名称")
    private String supplierName;

    public String getMaterialName()
    {
        return materialName;
    }

    public void setMaterialName(String materialName)
    {
        this.materialName = materialName;
    }

    public String getRegisterNo()
    {
        return registerNo;
    }

    public void setRegisterNo(String registerNo)
    {
        this.registerNo = registerNo;
    }

    public String getSpecification()
    {
        return specification;
    }

    public void setSpecification(String specification)
    {
        this.specification = specification;
    }

    public String getModel()
    {
        return model;
    }

    public void setModel(String model)
    {
        this.model = model;
    }

    public String getUnit()
    {
        return unit;
    }

    public void setUnit(String unit)
    {
        this.unit = unit;
    }

    public String getManufacturerName()
    {
        return manufacturerName;
    }

    public void setManufacturerName(String manufacturerName)
    {
        this.manufacturerName = manufacturerName;
    }

    public BigDecimal getPurchasePrice()
    {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice)
    {
        this.purchasePrice = purchasePrice;
    }

    public String getRegisterName()
    {
        return registerName;
    }

    public void setRegisterName(String registerName)
    {
        this.registerName = registerName;
    }

    public String getUdiCode()
    {
        return udiCode;
    }

    public void setUdiCode(String udiCode)
    {
        this.udiCode = udiCode;
    }

    public Date getRegisterIssueDate()
    {
        return registerIssueDate;
    }

    public void setRegisterIssueDate(Date registerIssueDate)
    {
        this.registerIssueDate = registerIssueDate;
    }

    public Date getExpireDate()
    {
        return expireDate;
    }

    public void setExpireDate(Date expireDate)
    {
        this.expireDate = expireDate;
    }

    public String getProductCategory()
    {
        return productCategory;
    }

    public void setProductCategory(String productCategory)
    {
        this.productCategory = productCategory;
    }

    public String getSupplierName()
    {
        return supplierName;
    }

    public void setSupplierName(String supplierName)
    {
        this.supplierName = supplierName;
    }
}
