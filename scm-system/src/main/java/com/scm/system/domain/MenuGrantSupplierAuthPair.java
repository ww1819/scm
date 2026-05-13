package com.scm.system.domain;

/**
 * 供应商全局菜单白名单预加载行（活跃供应商，hospital_id 为空）
 */
public class MenuGrantSupplierAuthPair
{
    private Long supplierId;
    private Long menuId;

    public Long getSupplierId()
    {
        return supplierId;
    }

    public void setSupplierId(Long supplierId)
    {
        this.supplierId = supplierId;
    }

    public Long getMenuId()
    {
        return menuId;
    }

    public void setMenuId(Long menuId)
    {
        this.menuId = menuId;
    }
}
