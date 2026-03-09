package com.scm.system.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.scm.common.utils.uuid.IdUtils;
import com.scm.system.domain.ScmTenantMenu;
import com.scm.system.mapper.ScmTenantMenuMapper;
import com.scm.system.service.IScmTenantMenuService;

import java.util.Date;
import java.util.List;

/**
 * 租户功能菜单授权：scm_tenant_menu（主键 UUID7）
 */
@Service
public class ScmTenantMenuServiceImpl implements IScmTenantMenuService
{
    @Autowired
    private ScmTenantMenuMapper scmTenantMenuMapper;

    @Override
    public List<Long> selectMenuIdsByTenantId(String tenantId)
    {
        return scmTenantMenuMapper.selectMenuIdsByTenantId(tenantId);
    }

    @Override
    public void saveTenantMenus(String tenantId, Long[] menuIds, String createBy)
    {
        scmTenantMenuMapper.deleteByTenantId(tenantId);
        if (menuIds != null && menuIds.length > 0) {
            Date now = new Date();
            for (Long menuId : menuIds) {
                ScmTenantMenu row = new ScmTenantMenu();
                row.setId(IdUtils.simpleUuid7());
                row.setTenantId(tenantId);
                row.setMenuId(menuId);
                row.setCreateBy(createBy);
                row.setCreateTime(now);
                scmTenantMenuMapper.insert(row);
            }
        }
    }
}
