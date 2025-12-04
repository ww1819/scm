package com.scm.system.domain;

import java.util.Date;
import javax.validation.constraints.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.scm.common.annotation.Excel;
import com.scm.common.annotation.Excel.ColumnType;
import com.scm.common.core.domain.BaseEntity;

/**
 * 产品证件表 scm_product_certificate
 * 
 * @author scm
 */
public class ProductCertificate extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 证件ID */
    @Excel(name = "证件ID", cellType = ColumnType.NUMERIC)
    private Long certificateId;

    /** 物资ID */
    private Long materialId;

    /** 物资名称 */
    private String materialName;

    /** 规格 */
    private String specification;

    /** 型号 */
    private String model;

    /** 生产厂家 */
    private String manufacturerName;

    /** 供应商ID */
    private Long supplierId;

    /** 供应商名称 */
    private String supplierName;

    /** 证件类型 */
    @Excel(name = "证件类型")
    private String certificateType;

    /** 证件名称 */
    @Excel(name = "证件名称")
    private String certificateName;

    /** 注册证号 */
    @Excel(name = "注册证号")
    private String registerNo;

    /** 条码号(UDI) */
    @Excel(name = "条码号(UDI)")
    private String udiCode;

    /** 拼音简码 */
    @Excel(name = "拼音简码")
    private String pinyinCode;

    /** 注册证名称 */
    @Excel(name = "注册证名称")
    private String registerName;

    /** 注册日期 */
    @Excel(name = "注册日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date registerDate;

    /** 注册证发证日期 */
    @Excel(name = "注册证发证日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date registerIssueDate;

    /** 有效期至 */
    @Excel(name = "有效期至", width = 30, dateFormat = "yyyy-MM-dd")
    private Date expireDate;

    /** 注册有效期 */
    @Excel(name = "注册有效期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date registerValidDate;

    /** 中标价格 */
    @Excel(name = "中标价格", cellType = ColumnType.NUMERIC)
    private java.math.BigDecimal bidPrice;

    /** 销售价格 */
    @Excel(name = "销售价格", cellType = ColumnType.NUMERIC)
    private java.math.BigDecimal salePrice;

    /** 医院编码 */
    @Excel(name = "医院编码")
    private String hospitalCode;

    /** 销售客户 */
    @Excel(name = "销售客户")
    private String saleCustomer;

    /** 产品类别（高值、低值） */
    @Excel(name = "产品类别")
    private String productCategory;

    /** 证件文件路径 */
    private String certificateFile;

    /** 审核状态（0待审核 1已审核 2已拒绝） */
    @Excel(name = "审核状态", readConverterExp = "0=待审核,1=已审核,2=已拒绝")
    private String auditStatus;

    /** 审核人 */
    private String auditBy;

    /** 审核时间 */
    @Excel(name = "审核时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date auditTime;

    /** 审核备注 */
    private String auditRemark;

    /** 是否过期（0否 1是） */
    @Excel(name = "是否过期", readConverterExp = "0=否,1=是")
    private String isExpired;

    /** 是否预警（0否 1是） */
    @Excel(name = "是否预警", readConverterExp = "0=否,1=是")
    private String isWarning;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    // 物资ID在保存时自动生成，不需要验证
    // @NotNull(message = "物资ID不能为空")
    public Long getMaterialId()
    {
        return materialId;
    }

    public void setMaterialId(Long materialId)
    {
        this.materialId = materialId;
    }

    public String getMaterialName()
    {
        return materialName;
    }

    public void setMaterialName(String materialName)
    {
        this.materialName = materialName;
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

    public String getManufacturerName()
    {
        return manufacturerName;
    }

    public void setManufacturerName(String manufacturerName)
    {
        this.manufacturerName = manufacturerName;
    }

    // 供应商ID为选填字段，不需要验证
    // @NotNull(message = "供应商ID不能为空")
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

    @Size(min = 0, max = 50, message = "证件类型不能超过50个字符")
    public String getCertificateType()
    {
        return certificateType;
    }

    public void setCertificateType(String certificateType)
    {
        this.certificateType = certificateType;
    }

    @Size(min = 0, max = 200, message = "证件名称不能超过200个字符")
    public String getCertificateName()
    {
        return certificateName;
    }

    public void setCertificateName(String certificateName)
    {
        this.certificateName = certificateName;
    }

    @Size(min = 0, max = 100, message = "注册证号不能超过100个字符")
    public String getRegisterNo()
    {
        return registerNo;
    }

    public void setRegisterNo(String registerNo)
    {
        this.registerNo = registerNo;
    }

    public Date getRegisterDate()
    {
        return registerDate;
    }

    public void setRegisterDate(Date registerDate)
    {
        this.registerDate = registerDate;
    }

    public Date getExpireDate()
    {
        return expireDate;
    }

    public void setExpireDate(Date expireDate)
    {
        this.expireDate = expireDate;
    }

    @Size(min = 0, max = 500, message = "证件文件路径不能超过500个字符")
    public String getCertificateFile()
    {
        return certificateFile;
    }

    public void setCertificateFile(String certificateFile)
    {
        this.certificateFile = certificateFile;
    }

    public String getAuditStatus()
    {
        return auditStatus;
    }

    public void setAuditStatus(String auditStatus)
    {
        this.auditStatus = auditStatus;
    }

    public String getAuditBy()
    {
        return auditBy;
    }

    public void setAuditBy(String auditBy)
    {
        this.auditBy = auditBy;
    }

    public Date getAuditTime()
    {
        return auditTime;
    }

    public void setAuditTime(Date auditTime)
    {
        this.auditTime = auditTime;
    }

    public String getAuditRemark()
    {
        return auditRemark;
    }

    public void setAuditRemark(String auditRemark)
    {
        this.auditRemark = auditRemark;
    }

    public String getIsExpired()
    {
        return isExpired;
    }

    public void setIsExpired(String isExpired)
    {
        this.isExpired = isExpired;
    }

    public String getIsWarning()
    {
        return isWarning;
    }

    public void setIsWarning(String isWarning)
    {
        this.isWarning = isWarning;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Long getCertificateId()
    {
        return certificateId;
    }

    public void setCertificateId(Long certificateId)
    {
        this.certificateId = certificateId;
    }

    public String getUdiCode()
    {
        return udiCode;
    }

    public void setUdiCode(String udiCode)
    {
        this.udiCode = udiCode;
    }

    public String getPinyinCode()
    {
        return pinyinCode;
    }

    public void setPinyinCode(String pinyinCode)
    {
        this.pinyinCode = pinyinCode;
    }

    public String getRegisterName()
    {
        return registerName;
    }

    public void setRegisterName(String registerName)
    {
        this.registerName = registerName;
    }

    public Date getRegisterIssueDate()
    {
        return registerIssueDate;
    }

    public void setRegisterIssueDate(Date registerIssueDate)
    {
        this.registerIssueDate = registerIssueDate;
    }

    public Date getRegisterValidDate()
    {
        return registerValidDate;
    }

    public void setRegisterValidDate(Date registerValidDate)
    {
        this.registerValidDate = registerValidDate;
    }

    public java.math.BigDecimal getBidPrice()
    {
        return bidPrice;
    }

    public void setBidPrice(java.math.BigDecimal bidPrice)
    {
        this.bidPrice = bidPrice;
    }

    public java.math.BigDecimal getSalePrice()
    {
        return salePrice;
    }

    public void setSalePrice(java.math.BigDecimal salePrice)
    {
        this.salePrice = salePrice;
    }

    public String getHospitalCode()
    {
        return hospitalCode;
    }

    public void setHospitalCode(String hospitalCode)
    {
        this.hospitalCode = hospitalCode;
    }

    public String getSaleCustomer()
    {
        return saleCustomer;
    }

    public void setSaleCustomer(String saleCustomer)
    {
        this.saleCustomer = saleCustomer;
    }

    public String getProductCategory()
    {
        return productCategory;
    }

    public void setProductCategory(String productCategory)
    {
        this.productCategory = productCategory;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("certificateId", getCertificateId())
            .append("materialId", getMaterialId())
            .append("materialName", getMaterialName())
            .append("specification", getSpecification())
            .append("model", getModel())
            .append("manufacturerName", getManufacturerName())
            .append("supplierId", getSupplierId())
            .append("supplierName", getSupplierName())
            .append("certificateType", getCertificateType())
            .append("certificateName", getCertificateName())
            .append("registerNo", getRegisterNo())
            .append("udiCode", getUdiCode())
            .append("pinyinCode", getPinyinCode())
            .append("registerName", getRegisterName())
            .append("registerDate", getRegisterDate())
            .append("registerIssueDate", getRegisterIssueDate())
            .append("expireDate", getExpireDate())
            .append("registerValidDate", getRegisterValidDate())
            .append("bidPrice", getBidPrice())
            .append("salePrice", getSalePrice())
            .append("hospitalCode", getHospitalCode())
            .append("saleCustomer", getSaleCustomer())
            .append("productCategory", getProductCategory())
            .append("certificateFile", getCertificateFile())
            .append("auditStatus", getAuditStatus())
            .append("auditBy", getAuditBy())
            .append("auditTime", getAuditTime())
            .append("auditRemark", getAuditRemark())
            .append("isExpired", getIsExpired())
            .append("isWarning", getIsWarning())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}

