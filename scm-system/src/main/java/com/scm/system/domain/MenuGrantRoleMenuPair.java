package com.scm.system.domain;

/**
 * 角色已授权菜单（院/商维度由查询限定）
 */
public class MenuGrantRoleMenuPair
{
    private Long roleId;
    private Long menuId;

    public Long getRoleId()
    {
        return roleId;
    }

    public void setRoleId(Long roleId)
    {
        this.roleId = roleId;
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
