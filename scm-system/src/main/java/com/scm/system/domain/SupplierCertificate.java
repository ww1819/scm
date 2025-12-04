package com.scm.system.domain;

import java.util.Date;
import javax.validation.constraints.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.scm.common.annotation.Excel;
import com.scm.common.annotation.Excel.ColumnType;
import com.scm.common.core.domain.BaseEntity;

/**
 * 供应商证件表 scm_supplier_certificate
 * 
 * @author scm
 */
public class SupplierCertificate extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 证件ID */
    @Excel(name = "证件ID", cellType = ColumnType.NUMERIC)
    private Long certificateId;

    /** 供应商ID */
    private Long supplierId;

    /** 供应商名称 */
    private String supplierName;

    /** 证件类型（营业执照、经营许可证等） */
    @Excel(name = "证件类型")
    private String certificateType;

    /** 证件名称 */
    @Excel(name = "证件名称")
    private String certificateName;

    /** 证件编号 */
    @Excel(name = "证件编号")
    private String certificateNo;

    /** 发证日期 */
    @Excel(name = "发证日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date issueDate;

    /** 有效期至 */
    @Excel(name = "有效期至", width = 30, dateFormat = "yyyy-MM-dd")
    private Date expireDate;

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

    @NotNull(message = "供应商ID不能为空")
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

    @NotBlank(message = "证件类型不能为空")
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

    @Size(min = 0, max = 100, message = "证件编号不能超过100个字符")
    public String getCertificateNo()
    {
        return certificateNo;
    }

    public void setCertificateNo(String certificateNo)
    {
        this.certificateNo = certificateNo;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("certificateId", getCertificateId())
            .append("supplierId", getSupplierId())
            .append("supplierName", getSupplierName())
            .append("certificateType", getCertificateType())
            .append("certificateName", getCertificateName())
            .append("certificateNo", getCertificateNo())
            .append("issueDate", getIssueDate())
            .append("expireDate", getExpireDate())
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

