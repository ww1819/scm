package com.scm.system.domain;

import java.util.Date;
import com.scm.common.annotation.Excel;
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
    @Excel(name = "医院编码", sort = 10)
    private String hospitalCode;
    @Excel(name = "医院名称", sort = 20)
    private String hospitalName;
    @Excel(name = "供应商编码", sort = 30)
    private String supplierCode;
    @Excel(name = "供应商名称", sort = 40)
    private String supplierName;
    @Excel(name = "供货开始", width = 20, dateFormat = "yyyy-MM-dd HH:mm:ss", sort = 50)
    private Date supplyStartDate;
    @Excel(name = "供货结束", width = 20, dateFormat = "yyyy-MM-dd HH:mm:ss", sort = 60)
    private Date supplyEndDate;
    @Excel(name = "合同编号", sort = 70)
    private String contractNo;
    @Excel(name = "申请说明", width = 30, sort = 80)
    private String applyReason;
    @Excel(name = "联系人", sort = 90)
    private String contactPerson;
    @Excel(name = "联系电话", sort = 100)
    private String contactPhone;
    @Excel(name = "审核状态", readConverterExp = "0=待审核,1=已通过,2=已拒绝", sort = 110)
    private String auditStatus;
    @Excel(name = "审核人", sort = 120)
    private String auditBy;
    @Excel(name = "审核时间", width = 20, dateFormat = "yyyy-MM-dd HH:mm:ss", sort = 130)
    private Date auditTime;
    @Excel(name = "审核备注", width = 30, sort = 140)
    private String auditRemark;
    private String delFlag;
    private String delBy;
    private Date delTime;

    /** 查询条件：医院编码/名称/拼音简码（模糊，不入库） */
    private String hospitalKeyword;
    /** 查询条件：供应商编码/名称/拼音简码（模糊，不入库） */
    private String supplierKeyword;

    public String getApplyId() { return applyId; }
    public void setApplyId(String applyId) { this.applyId = applyId; }
    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }
    public String getHospitalId() { return hospitalId; }
    public void setHospitalId(String hospitalId) { this.hospitalId = hospitalId; }
    public String getHospitalCode() { return hospitalCode; }
    public void setHospitalCode(String hospitalCode) { this.hospitalCode = hospitalCode; }
    public String getSupplierCode() { return supplierCode; }
    public void setSupplierCode(String supplierCode) { this.supplierCode = supplierCode; }
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
    public String getHospitalKeyword() { return hospitalKeyword; }
    public void setHospitalKeyword(String hospitalKeyword) { this.hospitalKeyword = hospitalKeyword; }
    public String getSupplierKeyword() { return supplierKeyword; }
    public void setSupplierKeyword(String supplierKeyword) { this.supplierKeyword = supplierKeyword; }
}
