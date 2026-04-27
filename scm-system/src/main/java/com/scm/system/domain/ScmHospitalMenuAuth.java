package com.scm.system.domain;

import com.scm.common.core.domain.BaseEntity;

/**
 * 医院菜单授权白名单 scm_hospital_menu_auth
 */
public class ScmHospitalMenuAuth extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
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

    public Long getHospitalId()
    {
        return hospitalId;
    }

    public void setHospitalId(Long hospitalId)
    {
        this.hospitalId = hospitalId;
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
