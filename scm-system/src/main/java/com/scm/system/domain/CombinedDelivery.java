package com.scm.system.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.scm.common.annotation.Excel;
import com.scm.common.core.domain.BaseEntity;

/**
 * 合单配送主表 scm_combined_delivery（主键 UUID7）
 */
public class CombinedDelivery extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 合单ID（UUID7） */
    private String combinedId;
    @Excel(name = "合单号")
    private String combinedNo;
    /** 医院ID（字符串外键；实体用 Long 便于与现网医院主数据对接，入库为字符串） */
    private Long hospitalId;
    @Excel(name = "医院")
    private String hospitalName;
    @Excel(name = "仓库")
    private String warehouse;
    @Excel(name = "收货地址")
    private String deliveryAddress;
    private Long supplierId;
    private String spdSupplierId;
    private String spdTenantId;
    @Excel(name = "供应商")
    private String supplierName;
    @Excel(name = "金额")
    private BigDecimal deliveryAmount;
    private String deliveryStatus;
    private String deliveryPerson;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date expectedDeliveryDate;
    @Excel(name = "审核状态", readConverterExp = "0=待审核,1=已审核")
    private String auditStatus;
    private String auditBy;
    private String auditByNameSnapshot;
    private Date auditTime;
    private String auditRemark;
    private String createByNameSnapshot;
    private String createByDisplay;
    private String auditByDisplay;
    private List<CombinedDeliveryDetail> details;

    public String getCombinedId() { return combinedId; }
    public void setCombinedId(String combinedId) { this.combinedId = combinedId; }
    public String getCombinedNo() { return combinedNo; }
    public void setCombinedNo(String combinedNo) { this.combinedNo = combinedNo; }
    public Long getHospitalId() { return hospitalId; }
    public void setHospitalId(Long hospitalId) { this.hospitalId = hospitalId; }
    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }
    public String getWarehouse() { return warehouse; }
    public void setWarehouse(String warehouse) { this.warehouse = warehouse; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public String getSpdSupplierId() { return spdSupplierId; }
    public void setSpdSupplierId(String spdSupplierId) { this.spdSupplierId = spdSupplierId; }
    public String getSpdTenantId() { return spdTenantId; }
    public void setSpdTenantId(String spdTenantId) { this.spdTenantId = spdTenantId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public BigDecimal getDeliveryAmount() { return deliveryAmount; }
    public void setDeliveryAmount(BigDecimal deliveryAmount) { this.deliveryAmount = deliveryAmount; }
    public String getDeliveryStatus() { return deliveryStatus; }
    public void setDeliveryStatus(String deliveryStatus) { this.deliveryStatus = deliveryStatus; }
    public String getDeliveryPerson() { return deliveryPerson; }
    public void setDeliveryPerson(String deliveryPerson) { this.deliveryPerson = deliveryPerson; }
    public Date getExpectedDeliveryDate() { return expectedDeliveryDate; }
    public void setExpectedDeliveryDate(Date expectedDeliveryDate) { this.expectedDeliveryDate = expectedDeliveryDate; }
    public String getAuditStatus() { return auditStatus; }
    public void setAuditStatus(String auditStatus) { this.auditStatus = auditStatus; }
    public String getAuditBy() { return auditBy; }
    public void setAuditBy(String auditBy) { this.auditBy = auditBy; }
    public String getAuditByNameSnapshot() { return auditByNameSnapshot; }
    public void setAuditByNameSnapshot(String auditByNameSnapshot) { this.auditByNameSnapshot = auditByNameSnapshot; }
    public Date getAuditTime() { return auditTime; }
    public void setAuditTime(Date auditTime) { this.auditTime = auditTime; }
    public String getAuditRemark() { return auditRemark; }
    public void setAuditRemark(String auditRemark) { this.auditRemark = auditRemark; }
    public String getCreateByNameSnapshot() { return createByNameSnapshot; }
    public void setCreateByNameSnapshot(String createByNameSnapshot) { this.createByNameSnapshot = createByNameSnapshot; }
    public String getCreateByDisplay() { return createByDisplay; }
    public void setCreateByDisplay(String createByDisplay) { this.createByDisplay = createByDisplay; }
    public String getAuditByDisplay() { return auditByDisplay; }
    public void setAuditByDisplay(String auditByDisplay) { this.auditByDisplay = auditByDisplay; }
    public List<CombinedDeliveryDetail> getDetails() { return details; }
    public void setDetails(List<CombinedDeliveryDetail> details) { this.details = details; }
}
