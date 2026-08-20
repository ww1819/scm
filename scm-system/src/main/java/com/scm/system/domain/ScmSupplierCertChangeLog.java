package com.scm.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.scm.common.core.domain.BaseEntity;

/**
 * 供应商资质变更抄送记录 scm_supplier_cert_change_log
 */
public class ScmSupplierCertChangeLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private String logId;
    private Long supplierId;
    private Long hospitalId;
    private Long certificateId;
    /** INSERT / UPDATE / DELETE / AUDIT */
    private String changeType;
    private String beforeJson;
    private String afterJson;
    private Date createTime;

    /** 展示/筛选：供应商名称 */
    private String supplierName;
    /** 供应商编码 */
    private String supplierCode;
    /** 展示/筛选：医院名称 */
    private String hospitalName;
    /** 医院编码 */
    private String hospitalCode;
    /** 展示/筛选：证照类型（查询条件时按 JSON 模糊匹配） */
    private String certificateType;
    /** 证照编号（变更后优先） */
    private String certificateNo;
    /** 授权日期（成立/发证日期，变更后优先） */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date issueDate;
    /** 有效期（变更后优先） */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date expireDate;
    /** 是否上传：1是 0否 */
    private String uploadedFlag;
    /** 证件创建人（快照） */
    private String certCreateBy;
    /** 证件创建时间（快照） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date certCreateTime;
    /** 原证照号 */
    private String oldCertificateNo;
    /** 原效期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date oldExpireDate;
    /** 图片 URL（变更后优先） */
    private String certificateFile;
    /** 修改后证照号 */
    private String newCertificateNo;
    /** 修改后效期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date newExpireDate;
    /** 审核人（取自变更后快照） */
    private String auditBy;
    /** 审核日期（取自变更后快照） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date auditTime;

    public String getLogId()
    {
        return logId;
    }

    public void setLogId(String logId)
    {
        this.logId = logId;
    }

    public Long getSupplierId()
    {
        return supplierId;
    }

    public void setSupplierId(Long supplierId)
    {
        this.supplierId = supplierId;
    }

    public Long getHospitalId()
    {
        return hospitalId;
    }

    public void setHospitalId(Long hospitalId)
    {
        this.hospitalId = hospitalId;
    }

    public Long getCertificateId()
    {
        return certificateId;
    }

    public void setCertificateId(Long certificateId)
    {
        this.certificateId = certificateId;
    }

    public String getChangeType()
    {
        return changeType;
    }

    public void setChangeType(String changeType)
    {
        this.changeType = changeType;
    }

    public String getBeforeJson()
    {
        return beforeJson;
    }

    public void setBeforeJson(String beforeJson)
    {
        this.beforeJson = beforeJson;
    }

    public String getAfterJson()
    {
        return afterJson;
    }

    public void setAfterJson(String afterJson)
    {
        this.afterJson = afterJson;
    }

    @Override
    public Date getCreateTime()
    {
        return createTime;
    }

    @Override
    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
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

    public String getHospitalName()
    {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName)
    {
        this.hospitalName = hospitalName;
    }

    public String getHospitalCode()
    {
        return hospitalCode;
    }

    public void setHospitalCode(String hospitalCode)
    {
        this.hospitalCode = hospitalCode;
    }

    public String getCertificateType()
    {
        return certificateType;
    }

    public void setCertificateType(String certificateType)
    {
        this.certificateType = certificateType;
    }

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

    public String getUploadedFlag()
    {
        return uploadedFlag;
    }

    public void setUploadedFlag(String uploadedFlag)
    {
        this.uploadedFlag = uploadedFlag;
    }

    public String getCertCreateBy()
    {
        return certCreateBy;
    }

    public void setCertCreateBy(String certCreateBy)
    {
        this.certCreateBy = certCreateBy;
    }

    public Date getCertCreateTime()
    {
        return certCreateTime;
    }

    public void setCertCreateTime(Date certCreateTime)
    {
        this.certCreateTime = certCreateTime;
    }

    public String getOldCertificateNo()
    {
        return oldCertificateNo;
    }

    public void setOldCertificateNo(String oldCertificateNo)
    {
        this.oldCertificateNo = oldCertificateNo;
    }

    public Date getOldExpireDate()
    {
        return oldExpireDate;
    }

    public void setOldExpireDate(Date oldExpireDate)
    {
        this.oldExpireDate = oldExpireDate;
    }

    public String getCertificateFile()
    {
        return certificateFile;
    }

    public void setCertificateFile(String certificateFile)
    {
        this.certificateFile = certificateFile;
    }

    public String getNewCertificateNo()
    {
        return newCertificateNo;
    }

    public void setNewCertificateNo(String newCertificateNo)
    {
        this.newCertificateNo = newCertificateNo;
    }

    public Date getNewExpireDate()
    {
        return newExpireDate;
    }

    public void setNewExpireDate(Date newExpireDate)
    {
        this.newExpireDate = newExpireDate;
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
}
