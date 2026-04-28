package com.scm.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 角色和菜单关联 sys_role_menu（主键 UUID，可按 hospital_id/supplier_id 收窄数据范围）
 *
 * @author scm
 */
public class SysRoleMenu
{
    /** 主键 UUID（UUID7） */
    private String id;

    /** 角色ID */
    private Long roleId;

    /** 菜单ID */
    private Long menuId;

    /** 绑定医院 ID（varchar，空串表示不按医院收窄） */
    private String hospitalId;

    /** 绑定供应商 ID（varchar，空串表示不按供应商收窄） */
    private String supplierId;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

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

    public String getHospitalId()
    {
        return hospitalId;
    }

    public void setHospitalId(String hospitalId)
    {
        this.hospitalId = hospitalId;
    }

    public String getSupplierId()
    {
        return supplierId;
    }

    public void setSupplierId(String supplierId)
    {
        this.supplierId = supplierId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("roleId", getRoleId())
            .append("menuId", getMenuId())
            .append("hospitalId", getHospitalId())
            .append("supplierId", getSupplierId())
            .toString();
    }
}
