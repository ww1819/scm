package com.scm.system.mapper;

import java.util.List;
import com.scm.system.domain.ScmTenantModifyLog;

public interface ScmTenantModifyLogMapper
{
    List<ScmTenantModifyLog> selectByTenantId(String tenantId);

    int insert(ScmTenantModifyLog row);
}
