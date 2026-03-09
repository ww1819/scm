package com.scm.system.mapper;

import java.util.List;
import com.scm.system.domain.ScmTenantMenu;
import com.scm.system.domain.ScmTenantMenuPauseManageVo;
import org.apache.ibatis.annotations.Param;

public interface ScmTenantMenuMapper
{
    List<Long> selectMenuIdsByTenantId(String tenantId);

    /** 客户菜单功能管理：客户所有已授权菜单及暂停状态（仅目录、菜单） */
    List<ScmTenantMenuPauseManageVo> selectListWithMenuNameAndPauseByTenantId(@Param("tenantId") String tenantId);

    int deleteByTenantId(String tenantId);

    int insert(ScmTenantMenu row);
}
