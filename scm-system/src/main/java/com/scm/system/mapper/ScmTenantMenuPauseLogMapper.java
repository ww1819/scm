package com.scm.system.mapper;

import java.util.List;
import com.scm.system.domain.ScmTenantMenuPauseLog;
import com.scm.system.domain.ScmTenantMenuPauseLogVo;
import org.apache.ibatis.annotations.Param;

public interface ScmTenantMenuPauseLogMapper
{
    List<ScmTenantMenuPauseLog> selectByTenantId(String tenantId);

    /** 客户菜单暂停记录（含菜单名称） */
    List<ScmTenantMenuPauseLogVo> selectByTenantIdWithMenuName(@Param("tenantId") String tenantId);

    List<ScmTenantMenuPauseLog> selectByPauseId(String pauseId);

    int insert(ScmTenantMenuPauseLog row);
}
