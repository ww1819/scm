package com.scm.system.domain;

import java.math.BigDecimal;
import java.util.Date;
import javax.validation.constraints.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.scm.common.annotation.Excel;
import com.scm.common.annotation.Excel.ColumnType;
import com.scm.common.core.domain.BaseEntity;

/**
 * 供应商信息表 scm_supplier
 * 
 * @author scm
 */
public class Supplier extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 供应商ID */
    @Excel(name = "供应商ID", cellType = ColumnType.NUMERIC)
    private Long supplierId;

    /** 供应商编码 */
    @Excel(name = "供应商编码")
    private String supplierCode;

    /** 公司名称 */
    @Excel(name = "公司名称")
    private String companyName;

    /** 拼音简码 */
    @Excel(name = "拼音简码")
    private String companyShortName;

    /** 法人 */
    @Excel(name = "法人")
    private String legalPerson;

    /** 注册资金 */
    @Excel(name = "注册资金", cellType = ColumnType.NUMERIC)
    private BigDecimal registeredCapital;

    /** 省份/直辖市 */
    @Excel(name = "省份/直辖市")
    private String province;

    /** 城市 */
    @Excel(name = "城市")
    private String city;

    /** 县级/区 */
    @Excel(name = "县级/区")
    private String district;

    /** 详细联系地址 */
    @Excel(name = "详细联系地址")
    private String address;

    /** 经营范围 */
    @Excel(name = "经营范围")
    private String businessScope;

    /** 邮箱 */
    @Excel(name = "邮箱")
    private String email;

    /** 网址 */
    @Excel(name = "网址")
    private String website;

    /** 联系人 */
    @Excel(name = "联系人")
    private String contactPerson;

    /** 联系电话 */
    @Excel(name = "联系电话")
    private String contactPhone;

    /** 税号 */
    @Excel(name = "税号")
    private String taxNumber;

    /** 资质有效期 */
    @Excel(name = "资质有效期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date qualificationExpiryDate;

    /** 状态（0待审核 1正常 2停用） */
    @Excel(name = "状态", readConverterExp = "0=待审核,1=正常,2=停用")
    private String status;

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

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    /** 配送公司（多个公司名称用逗号分隔） */
    private String deliveryHospitals;

    public Long getSupplierId()
    {
        return supplierId;
    }

    public void setSupplierId(Long supplierId)
    {
        this.supplierId = supplierId;
    }

    public String getSupplierCode()
    {
        return supplierCode;
    }

    public void setSupplierCode(String supplierCode)
    {
        this.supplierCode = supplierCode;
    }

    @NotBlank(message = "公司名称不能为空")
    @Size(min = 0, max = 200, message = "公司名称不能超过200个字符")
    public String getCompanyName()
    {
        return companyName;
    }

    public void setCompanyName(String companyName)
    {
        this.companyName = companyName;
    }

    @Size(min = 0, max = 100, message = "公司简称不能超过100个字符")
    public String getCompanyShortName()
    {
        return companyShortName;
    }

    public void setCompanyShortName(String companyShortName)
    {
        this.companyShortName = companyShortName;
    }

    @Size(min = 0, max = 50, message = "法人不能超过50个字符")
    public String getLegalPerson()
    {
        return legalPerson;
    }

    public void setLegalPerson(String legalPerson)
    {
        this.legalPerson = legalPerson;
    }

    public BigDecimal getRegisteredCapital()
    {
        return registeredCapital;
    }

    public void setRegisteredCapital(BigDecimal registeredCapital)
    {
        this.registeredCapital = registeredCapital;
    }

    @Size(min = 0, max = 50, message = "省份/直辖市不能超过50个字符")
    public String getProvince()
    {
        return province;
    }

    public void setProvince(String province)
    {
        this.province = province;
    }

    @Size(min = 0, max = 50, message = "城市不能超过50个字符")
    public String getCity()
    {
        return city;
    }

    public void setCity(String city)
    {
        this.city = city;
    }

    @Size(min = 0, max = 50, message = "县级/区不能超过50个字符")
    public String getDistrict()
    {
        return district;
    }

    public void setDistrict(String district)
    {
        this.district = district;
    }

    @Size(min = 0, max = 500, message = "详细联系地址不能超过500个字符")
    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    @Size(min = 0, max = 1000, message = "经营范围不能超过1000个字符")
    public String getBusinessScope()
    {
        return businessScope;
    }

    public void setBusinessScope(String businessScope)
    {
        this.businessScope = businessScope;
    }

    @Email(message = "邮箱格式不正确")
    @Size(min = 0, max = 100, message = "邮箱不能超过100个字符")
    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    @Size(min = 0, max = 200, message = "网址不能超过200个字符")
    public String getWebsite()
    {
        return website;
    }

    public void setWebsite(String website)
    {
        this.website = website;
    }

    @Size(min = 0, max = 50, message = "联系人不能超过50个字符")
    public String getContactPerson()
    {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson)
    {
        this.contactPerson = contactPerson;
    }

    @Size(min = 0, max = 20, message = "联系电话不能超过20个字符")
    public String getContactPhone()
    {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone)
    {
        this.contactPhone = contactPhone;
    }

    @Size(min = 0, max = 50, message = "税号不能超过50个字符")
    public String getTaxNumber()
    {
        return taxNumber;
    }

    public void setTaxNumber(String taxNumber)
    {
        this.taxNumber = taxNumber;
    }

    public Date getQualificationExpiryDate()
    {
        return qualificationExpiryDate;
    }

    public void setQualificationExpiryDate(Date qualificationExpiryDate)
    {
        this.qualificationExpiryDate = qualificationExpiryDate;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
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

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public String getDeliveryHospitals()
    {
        return deliveryHospitals;
    }

    public void setDeliveryHospitals(String deliveryHospitals)
    {
        this.deliveryHospitals = deliveryHospitals;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("supplierId", getSupplierId())
            .append("supplierCode", getSupplierCode())
            .append("companyName", getCompanyName())
            .append("companyShortName", getCompanyShortName())
            .append("legalPerson", getLegalPerson())
            .append("registeredCapital", getRegisteredCapital())
            .append("province", getProvince())
            .append("city", getCity())
            .append("district", getDistrict())
            .append("address", getAddress())
            .append("businessScope", getBusinessScope())
            .append("email", getEmail())
            .append("website", getWebsite())
            .append("contactPerson", getContactPerson())
            .append("contactPhone", getContactPhone())
            .append("status", getStatus())
            .append("auditStatus", getAuditStatus())
            .append("auditBy", getAuditBy())
            .append("auditTime", getAuditTime())
            .append("auditRemark", getAuditRemark())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}

