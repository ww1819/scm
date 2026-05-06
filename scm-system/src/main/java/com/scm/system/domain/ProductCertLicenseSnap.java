package com.scm.system.domain;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.scm.common.core.domain.BaseEntity;

/**
 * 产品证件扩展证照快照 scm_product_cert_license_snap
 * <p>主键 UUID7（36 位）；关联证件/物资/供应商/医院均以 varchar 存逻辑外键与冗余快照。</p>
 */
public class ProductCertLicenseSnap extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private String licenseId;

    /** 关联产品证件 ID（字符串，与 scm_product_certificate.certificate_id 逻辑关联） */
    private String certificateId;

    private String materialId;
    private String supplierId;
    private String hospitalId;
    private String hospitalCode;

    /** 证照类别编码，如 PRODUCTION_LICENSE、MANUFACTURER_BIZ_LICENSE */
    private String licenseKindCode;
    /** 证照类别名称快照 */
    private String licenseKindName;
    private String licenseTitle;
    private String licenseNo;
    private String issuingBodySnap;
    private Date issueDate;
    private Date expireDate;

    private String productNameSnap;
    private String manufacturerNameSnap;
    private String supplierCompanyNameSnap;
    private String registerNoSnap;

    /** 影像附件，多路径逗号分隔 */
    private String certificateFile;

    private String delFlag;

    public String getLicenseId()
    {
        return licenseId;
    }

    public void setLicenseId(String licenseId)
    {
        this.licenseId = licenseId;
    }

    public String getCertificateId()
    {
        return certificateId;
    }

    public void setCertificateId(String certificateId)
    {
        this.certificateId = certificateId;
    }

    public String getMaterialId()
    {
        return materialId;
    }

    public void setMaterialId(String materialId)
    {
        this.materialId = materialId;
    }

    public String getSupplierId()
    {
        return supplierId;
    }

    public void setSupplierId(String supplierId)
    {
        this.supplierId = supplierId;
    }

    public String getHospitalId()
    {
        return hospitalId;
    }

    public void setHospitalId(String hospitalId)
    {
        this.hospitalId = hospitalId;
    }

    public String getHospitalCode()
    {
        return hospitalCode;
    }

    public void setHospitalCode(String hospitalCode)
    {
        this.hospitalCode = hospitalCode;
    }

    public String getLicenseKindCode()
    {
        return licenseKindCode;
    }

    public void setLicenseKindCode(String licenseKindCode)
    {
        this.licenseKindCode = licenseKindCode;
    }

    public String getLicenseKindName()
    {
        return licenseKindName;
    }

    public void setLicenseKindName(String licenseKindName)
    {
        this.licenseKindName = licenseKindName;
    }

    public String getLicenseTitle()
    {
        return licenseTitle;
    }

    public void setLicenseTitle(String licenseTitle)
    {
        this.licenseTitle = licenseTitle;
    }

    public String getLicenseNo()
    {
        return licenseNo;
    }

    public void setLicenseNo(String licenseNo)
    {
        this.licenseNo = licenseNo;
    }

    public String getIssuingBodySnap()
    {
        return issuingBodySnap;
    }

    public void setIssuingBodySnap(String issuingBodySnap)
    {
        this.issuingBodySnap = issuingBodySnap;
    }

    public Date getIssueDate()
    {
        return issueDate;
    }

    public void setIssueDate(Date issueDate)
    {
        this.issueDate = issueDate;
    }

    public Date getExpireDate()
    {
        return expireDate;
    }

    public void setExpireDate(Date expireDate)
    {
        this.expireDate = expireDate;
    }

    public String getProductNameSnap()
    {
        return productNameSnap;
    }

    public void setProductNameSnap(String productNameSnap)
    {
        this.productNameSnap = productNameSnap;
    }

    public String getManufacturerNameSnap()
    {
        return manufacturerNameSnap;
    }

    public void setManufacturerNameSnap(String manufacturerNameSnap)
    {
        this.manufacturerNameSnap = manufacturerNameSnap;
    }

    public String getSupplierCompanyNameSnap()
    {
        return supplierCompanyNameSnap;
    }

    public void setSupplierCompanyNameSnap(String supplierCompanyNameSnap)
    {
        this.supplierCompanyNameSnap = supplierCompanyNameSnap;
    }

    public String getRegisterNoSnap()
    {
        return registerNoSnap;
    }

    public void setRegisterNoSnap(String registerNoSnap)
    {
        this.registerNoSnap = registerNoSnap;
    }

    public String getCertificateFile()
    {
        return certificateFile;
    }

    public void setCertificateFile(String certificateFile)
    {
        this.certificateFile = certificateFile;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("licenseId", getLicenseId())
            .append("certificateId", getCertificateId())
            .append("licenseKindCode", getLicenseKindCode())
            .append("licenseNo", getLicenseNo())
            .toString();
    }
}
