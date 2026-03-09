package com.scm.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.scm.system.domain.ScmTenantMenuPause;

public interface ScmTenantMenuPauseMapper
{
    ScmTenantMenuPause selectByTenantAndMenu(@Param("tenantId") String tenantId, @Param("menuId") Long menuId);

    List<ScmTenantMenuPause> selectByTenantId(String tenantId);

    /** 查询某客户下处于暂停状态的菜单ID列表 */
    List<Long> selectPausedMenuIdsByTenantId(String tenantId);

    int insert(ScmTenantMenuPause row);

    int update(ScmTenantMenuPause row);
}
