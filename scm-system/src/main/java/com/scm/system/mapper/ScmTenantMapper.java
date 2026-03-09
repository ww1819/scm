package com.scm.system.mapper;

import java.util.List;
import com.scm.system.domain.ScmTenant;

/**
 * 客户（租户） 数据层
 */
public interface ScmTenantMapper
{
    ScmTenant selectScmTenantById(String tenantId);

    List<ScmTenant> selectScmTenantList(ScmTenant query);

    ScmTenant checkTenantCodeUnique(String tenantCode);

    int insertScmTenant(ScmTenant row);

    int updateScmTenant(ScmTenant row);

    int deleteScmTenantById(String tenantId);

    int deleteScmTenantByIds(String[] tenantIds);
}
