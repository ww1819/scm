package com.scm.common.core.domain.entity;

import java.io.Serializable;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 菜单变更记录 sys_menu_change_log
 */
public class SysMenuChangeLog implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long logId;
    private Long menuId;
    /** I 新增 U 修改 D 删除 S 排序 */
    private String changeType;
    private String operBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date operTime;
    /** JSON：含 before/after 快照 */
    private String menuSnapshot;

    public Long getLogId()
    {
        return logId;
    }

    public void setLogId(Long logId)
    {
        this.logId = logId;
    }

    public Long getMenuId()
    {
        return menuId;
    }

    public void setMenuId(Long menuId)
    {
        this.menuId = menuId;
    }

    public String getChangeType()
    {
        return changeType;
    }

    public void setChangeType(String changeType)
    {
        this.changeType = changeType;
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

    public String getMenuSnapshot()
    {
        return menuSnapshot;
    }

    public void setMenuSnapshot(String menuSnapshot)
    {
        this.menuSnapshot = menuSnapshot;
    }
}
