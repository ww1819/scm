package com.scm.system.domain;

import java.util.Date;
import javax.validation.constraints.NotBlank;
import com.scm.common.annotation.Excel;
import com.scm.common.core.domain.BaseEntity;

/**
 * SCM租户表（客户） scm_tenant
 */
public class ScmTenant extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 租户ID（UUID7） */
    private String tenantId;
    /** 租户名称 */
    @Excel(name = "租户名称")
    @NotBlank(message = "租户名称不能为空")
    private String tenantName;
    /** 租户编码 */
    @Excel(name = "租户编码")
    private String tenantCode;
    /** 拼音简码 */
    @Excel(name = "拼音简码")
    private String pinyinCode;
    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;
    /** 计划停用时间 */
    @Excel(name = "计划停用时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date plannedStopTime;
    /** 联系人 */
    @Excel(name = "联系人")
    private String contactPerson;
    /** 联系电话 */
    @Excel(name = "联系电话")
    private String contactPhone;

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getTenantName() { return tenantName; }
    public void setTenantName(String tenantName) { this.tenantName = tenantName; }
    public String getTenantCode() { return tenantCode; }
    public void setTenantCode(String tenantCode) { this.tenantCode = tenantCode; }
    public String getPinyinCode() { return pinyinCode; }
    public void setPinyinCode(String pinyinCode) { this.pinyinCode = pinyinCode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getPlannedStopTime() { return plannedStopTime; }
    public void setPlannedStopTime(Date plannedStopTime) { this.plannedStopTime = plannedStopTime; }
    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
}
