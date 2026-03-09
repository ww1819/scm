package com.scm.system.service;

import java.util.List;
import com.scm.system.domain.ScmTenant;
import com.scm.system.domain.ScmTenantModifyLog;
import com.scm.system.domain.ScmTenantStatusLog;
import com.scm.system.domain.ScmTenantStatusPeriod;

/**
 * 客户（租户） 服务
 */
public interface IScmTenantService
{
    ScmTenant selectScmTenantById(String tenantId);

    List<ScmTenant> selectScmTenantList(ScmTenant query);

    boolean checkTenantCodeUnique(ScmTenant tenant);

    int insertScmTenant(ScmTenant tenant, String createBy);

    int updateScmTenant(ScmTenant tenant, String updateBy);

    int deleteScmTenantByIds(String ids, String delBy);

    List<ScmTenantStatusPeriod> selectStatusPeriodsByTenantId(String tenantId);

    List<ScmTenantStatusLog> selectStatusLogsByTenantId(String tenantId);

    List<ScmTenantModifyLog> selectModifyLogsByTenantId(String tenantId);

    /** 重置：确保医院管理员角色和用户存在 */
    void resetTenantAdmin(String tenantId, String operBy);
}
