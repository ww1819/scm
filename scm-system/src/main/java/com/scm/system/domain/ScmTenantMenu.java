package com.scm.system.domain;

import java.util.Date;

/**
 * 租户功能菜单授权 scm_tenant_menu（主键 UUID7）
 */
public class ScmTenantMenu
{
    private String id;
    private String tenantId;
    private Long menuId;
    private String createBy;
    private Date createTime;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public Long getMenuId() { return menuId; }
    public void setMenuId(Long menuId) { this.menuId = menuId; }
    public String getCreateBy() { return createBy; }
    public void setCreateBy(String createBy) { this.createBy = createBy; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
