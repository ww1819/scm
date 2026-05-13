package com.scm.system.domain;

/**
 * 活跃医院下角色引用（用于批量赋权）
 */
public class MenuGrantHospitalRoleRef
{
    private Long roleId;
    private Long hospitalId;

    public Long getRoleId()
    {
        return roleId;
    }

    public void setRoleId(Long roleId)
    {
        this.roleId = roleId;
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
