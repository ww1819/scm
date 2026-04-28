package com.scm.system.domain;

import java.util.Date;
import com.scm.common.core.domain.BaseEntity;

/**
 * 供应商关联医院申请
 */
public class ScmHospitalSupplierApply extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private String applyId;
    private String supplierId;
    private String hospitalId;
    private String supplierName;
    private String hospitalName;
    private Date supplyStartDate;
    private Date supplyEndDate;
    private String contractNo;
    private String applyReason;
    private String contactPerson;
    private String contactPhone;
    private String auditStatus;
    private String auditBy;
    private Date auditTime;
    private String auditRemark;
    private String delFlag;
    private String delBy;
    private Date delTime;

    public String getApplyId() { return applyId; }
    public void setApplyId(String applyId) { this.applyId = applyId; }
    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }
    public String getHospitalId() { return hospitalId; }
    public void setHospitalId(String hospitalId) { this.hospitalId = hospitalId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }
    public Date getSupplyStartDate() { return supplyStartDate; }
    public void setSupplyStartDate(Date supplyStartDate) { this.supplyStartDate = supplyStartDate; }
    public Date getSupplyEndDate() { return supplyEndDate; }
    public void setSupplyEndDate(Date supplyEndDate) { this.supplyEndDate = supplyEndDate; }
    public String getContractNo() { return contractNo; }
    public void setContractNo(String contractNo) { this.contractNo = contractNo; }
    public String getApplyReason() { return applyReason; }
    public void setApplyReason(String applyReason) { this.applyReason = applyReason; }
    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getAuditStatus() { return auditStatus; }
    public void setAuditStatus(String auditStatus) { this.auditStatus = auditStatus; }
    public String getAuditBy() { return auditBy; }
    public void setAuditBy(String auditBy) { this.auditBy = auditBy; }
    public Date getAuditTime() { return auditTime; }
    public void setAuditTime(Date auditTime) { this.auditTime = auditTime; }
    public String getAuditRemark() { return auditRemark; }
    public void setAuditRemark(String auditRemark) { this.auditRemark = auditRemark; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getDelBy() { return delBy; }
    public void setDelBy(String delBy) { this.delBy = delBy; }
    public Date getDelTime() { return delTime; }
    public void setDelTime(Date delTime) { this.delTime = delTime; }
}
