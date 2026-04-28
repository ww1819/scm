package com.scm.system.service;

import java.util.List;
import java.util.Map;
import com.scm.system.domain.Hospital;
import com.scm.system.domain.ScmHospitalSupplierScopePair;
import com.scm.system.domain.Supplier;

/**
 * 医院-供应商（auth_type=hospital_supplier）菜单下的数据范围：联合键与下拉数据
 */
public interface IScmHospitalSupplierMenuScopeService
{
    /**
     * 当前用户在指定菜单下允许访问的 (医院ID, 供应商ID) 联合键。
     * 平台用户（非医院、非供应商主体）返回空列表，表示不按联合键限制。
     */
    List<ScmHospitalSupplierScopePair> listAuthorizedPairs(Long userId, Long menuId);

    List<Hospital> listHospitalsForDropdown(Long userId, Long menuId);

    List<Supplier> listSuppliersForDropdown(Long userId, Long menuId);

    /**
     * 列表查询 params 中若含 scopeMenuId（Long 或可解析字符串），则写入 scopePairs 或 scopePairBlock 供 Mapper 使用。
     */
    void applyMenuPairScopeToParams(Map<String, Object> params, Long userId);
}
