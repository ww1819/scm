package com.scm.system.mapper;

import java.util.List;
import com.scm.system.domain.ScmTenantStatusPeriod;

public interface ScmTenantStatusPeriodMapper
{
    List<ScmTenantStatusPeriod> selectByTenantId(String tenantId);

    int insert(ScmTenantStatusPeriod row);

    int update(ScmTenantStatusPeriod row);
}
