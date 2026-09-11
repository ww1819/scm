package com.scm.common.core.domain.entity;

import java.io.Serializable;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 角色菜单权限变更留痕 sys_role_menu_change_log（log_id 为 UUID7）
 */
public class SysRoleMenuChangeLog implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String logId;
    private Long roleId;
    /** ROLE_ADD / ROLE_EDIT / ROLE_DELETE / BOOTSTRAP / LEGACY_REPAIR / OTHER */
    private String changeSource;
    private String operBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date operTime;
    /** JSON 数组：变更前菜单ID */
    private String beforeMenuIds;
    /** JSON 数组：变更后菜单ID */
    private String afterMenuIds;
    /** JSON 数组：新增菜单ID */
    private String addedMenuIds;
    /** JSON 数组：移除菜单ID */
    private String removedMenuIds;
    private String remark;

    public String getLogId()
    {
        return logId;
    }

    public void setLogId(String logId)
    {
        this.logId = logId;
    }

    public Long getRoleId()
    {
        return roleId;
    }

    public void setRoleId(Long roleId)
    {
        this.roleId = roleId;
    }

    public String getChangeSource()
    {
        return changeSource;
    }

    public void setChangeSource(String changeSource)
    {
        this.changeSource = changeSource;
    }

    public String getOperBy()
    {
        return operBy;
    }

    public void setOperBy(String operBy)
    {
        this.operBy = operBy;
    }

    public Date getOperTime()
    {
        return operTime;
    }

    public void setOperTime(Date operTime)
    {
        this.operTime = operTime;
    }

    public String getBeforeMenuIds()
    {
        return beforeMenuIds;
    }

    public void setBeforeMenuIds(String beforeMenuIds)
    {
        this.beforeMenuIds = beforeMenuIds;
    }

    public String getAfterMenuIds()
    {
        return afterMenuIds;
    }

    public void setAfterMenuIds(String afterMenuIds)
    {
        this.afterMenuIds = afterMenuIds;
    }

    public String getAddedMenuIds()
    {
        return addedMenuIds;
    }

    public void setAddedMenuIds(String addedMenuIds)
    {
        this.addedMenuIds = addedMenuIds;
    }

    public String getRemovedMenuIds()
    {
        return removedMenuIds;
    }

    public void setRemovedMenuIds(String removedMenuIds)
    {
        this.removedMenuIds = removedMenuIds;
    }

    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }
}
