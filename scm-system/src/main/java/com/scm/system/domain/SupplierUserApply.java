package com.scm.system.domain;

import java.util.Date;
import com.scm.common.core.domain.BaseEntity;

/**
 * 供应商业务员关联申请 scm_supplier_user_apply
 */
public class SupplierUserApply extends BaseEntity {

    private static final long serialVersionUID = 1L;

    public static final String STATUS_PENDING = "0";
    public static final String STATUS_APPROVED = "1";
    public static final String STATUS_REJECTED = "2";

    private Long applyId;
    private Long supplierId;
    private Long userId;
    private String status;
    private Date applyTime;
    private String auditBy;
    private Date auditTime;
    private String auditRemark;
    /** 申请人登录名、姓名（查询填充） */
    private String loginName;
    private String userName;

    public Long getApplyId() { return applyId; }
    public void setApplyId(Long applyId) { this.applyId = applyId; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getApplyTime() { return applyTime; }
    public void setApplyTime(Date applyTime) { this.applyTime = applyTime; }
    public String getAuditBy() { return auditBy; }
    public void setAuditBy(String auditBy) { this.auditBy = auditBy; }
    public Date getAuditTime() { return auditTime; }
    public void setAuditTime(Date auditTime) { this.auditTime = auditTime; }
    public String getAuditRemark() { return auditRemark; }
    public void setAuditRemark(String auditRemark) { this.auditRemark = auditRemark; }
    public String getLoginName() { return loginName; }
    public void setLoginName(String loginName) { this.loginName = loginName; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
}
