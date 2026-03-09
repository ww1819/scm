package com.scm.system.mapper;

import java.util.List;
import com.scm.system.domain.ScmTenantMenuPauseLog;
import org.apache.ibatis.annotations.Param;

public interface ScmTenantMenuPauseLogMapper
{
    List<ScmTenantMenuPauseLog> selectByTenantId(String tenantId);

    List<ScmTenantMenuPauseLog> selectByPauseId(String pauseId);

    int insert(ScmTenantMenuPauseLog row);
}
