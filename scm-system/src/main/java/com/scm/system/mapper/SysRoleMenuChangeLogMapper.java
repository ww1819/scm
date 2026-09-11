package com.scm.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.scm.common.core.domain.entity.SysRoleMenuChangeLog;

/**
 * 角色菜单权限变更留痕
 */
public interface SysRoleMenuChangeLogMapper
{
    int insertSysRoleMenuChangeLog(SysRoleMenuChangeLog row);

    List<SysRoleMenuChangeLog> selectByRoleIdOrderDesc(@Param("roleId") Long roleId);

    SysRoleMenuChangeLog selectByLogId(@Param("logId") String logId);
}
