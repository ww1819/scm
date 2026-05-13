package com.scm.system.domain;

/**
 * 医院菜单白名单预加载行（活跃医院）
 */
public class MenuGrantHospitalAuthPair
{
    private Long hospitalId;
    private Long menuId;

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
