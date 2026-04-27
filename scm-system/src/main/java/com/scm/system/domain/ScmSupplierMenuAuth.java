package com.scm.system.domain;

import com.scm.common.core.domain.BaseEntity;

/**
 * 供应商菜单授权白名单 scm_supplier_menu_auth
 */
public class ScmSupplierMenuAuth extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long supplierId;
    private Long hospitalId;
    private Long menuId;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

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

    public Long getHospitalId()
    {
        return hospitalId;
    }

    public void setHospitalId(Long hospitalId)
    {
        this.hospitalId = hospitalId;
    }
}
