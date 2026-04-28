package com.scm.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.scm.common.core.domain.entity.SysMenuChangeLog;

/**
 * 菜单变更记录
 */
public interface SysMenuChangeLogMapper
{
    int insertSysMenuChangeLog(SysMenuChangeLog row);

    List<SysMenuChangeLog> selectByMenuIdOrderAsc(@Param("menuId") String menuId);

    SysMenuChangeLog selectByLogId(@Param("logId") String logId);
}
