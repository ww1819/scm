package com.scm.system.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 医院/供应商维度角色与白名单、角色菜单初始化
 */
public interface IScmScopeBootstrapService
{
    /** 返回某权限类型下所有菜单ID（含祖先目录），用于授权页全量候选 */
    Set<Long> listAllScopeMenuIds(String authType);

    /**
     * 新供应商注册后：创建供应商管理员+供应商业务员角色、写入菜单白名单、角色菜单；返回供应商管理员角色ID
     */
    Long bootstrapAfterSupplierRegister(Long supplierId, String operBy);

    /**
     * 新医院创建后：创建医院管理员+医院职工角色、白名单与角色菜单
     */
    void bootstrapAfterHospitalCreated(Long hospitalId, String operBy);

    /** 医院菜单白名单与医院管理员角色菜单重置为「全部医院类菜单」 */
    void resetHospitalMenuAuth(Long hospitalId, String operBy);

    /**
     * 按系统默认开放规则重置本院菜单白名单，并仅重建「医院管理员」「医院职工」两个内置角色的菜单权限（不修改其他自定义医院角色）。
     */
    void resetHospitalBuiltinRoleMenus(Long hospitalId, String operBy);

    /** 供应商菜单白名单与供应商管理员角色菜单重置为「全部供应商类菜单」 */
    void resetSupplierMenuAuth(Long supplierId, String operBy);

    /**
     * 按系统默认开放规则重置本供应商菜单白名单，并仅重建「供应商管理员」「供应商业务员」两个内置角色的菜单权限（不修改其他自定义供应商角色）。
     */
    void resetSupplierBuiltinRoleMenus(Long supplierId, String operBy);

    /** 覆盖医院白名单，并同步医院管理员角色菜单为传入集合 */
    void replaceHospitalMenuAuth(Long hospitalId, List<Long> menuIds, String operBy);

    /** 覆盖供应商白名单，并同步供应商管理员角色菜单为传入集合 */
    void replaceSupplierMenuAuth(Long supplierId, List<Long> menuIds, String operBy);

    /**
     * 升级维护：为老医院/老供应商补齐管理员/职工/业务员角色，并补齐管理员角色菜单与白名单（仅补充缺失，不删除既有授权）。
     * @return 统计信息（createdHospitalAdminRole/createdHospitalStaffRole/createdSupplierAdminRole/createdSupplierSalesRole/addedHospitalMenuAuth/addedSupplierMenuAuth/addedHospitalRoleMenu/addedSupplierRoleMenu）
     */
    Map<String, Integer> repairLegacyAdminScopes(String operBy);
}
