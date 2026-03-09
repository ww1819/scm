package com.scm.system.mapper;

import java.util.List;
import com.scm.system.domain.ScmTenantMenu;

public interface ScmTenantMenuMapper
{
    List<Long> selectMenuIdsByTenantId(String tenantId);

    int deleteByTenantId(String tenantId);

    int insert(ScmTenantMenu row);
}
