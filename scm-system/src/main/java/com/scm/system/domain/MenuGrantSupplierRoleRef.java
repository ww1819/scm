package com.scm.system.domain;

/**
 * 活跃供应商下角色引用（用于批量赋权）
 */
public class MenuGrantSupplierRoleRef
{
    private Long roleId;
    private Long supplierId;

    public Long getRoleId()
    {
        return roleId;
    }

    public void setRoleId(Long roleId)
    {
        this.roleId = roleId;
    }

    public Long getSupplierId()
    {
        return supplierId;
    }

    public void setSupplierId(Long supplierId)
    {
        this.supplierId = supplierId;
    }
}
