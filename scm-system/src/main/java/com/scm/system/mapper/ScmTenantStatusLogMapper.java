package com.scm.system.mapper;

import java.util.List;
import com.scm.system.domain.ScmTenantStatusLog;

public interface ScmTenantStatusLogMapper
{
    List<ScmTenantStatusLog> selectByTenantId(String tenantId);

    int insert(ScmTenantStatusLog row);
}
