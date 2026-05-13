package com.scm.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.scm.system.domain.MenuGrantRoleMenuPair;
import com.scm.system.domain.SysRoleMenu;

/**
 * 角色与菜单关联表 数据层
 * 
 * @author scm
 */
public interface SysRoleMenuMapper
{
    /**
     * 通过角色ID删除角色和菜单关联
     * 
     * @param roleId 角色ID
     * @return 结果
     */
    public int deleteRoleMenuByRoleId(Long roleId);
    
    /**
     * 批量删除角色菜单关联信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteRoleMenu(Long[] ids);
    
    /**
     * 查询菜单使用数量
     * 
     * @param menuId 菜单ID
     * @return 结果
     */
    public int selectCountRoleMenuByMenuId(Long menuId);
    
    /**
     * 批量新增角色菜单信息
     * 
     * @param roleMenuList 角色菜单列表
     * @return 结果
     */
    public int batchRoleMenu(List<SysRoleMenu> roleMenuList);

    /**
     * 批量新增角色菜单（忽略 uk_role_menu_scope 重复）
     */
    int batchRoleMenuIgnore(List<SysRoleMenu> roleMenuList);

    /**
     * 查询某角色在指定医院/供应商维度下已授权菜单ID（空串表示不限制该维）
     */
    public List<Long> selectMenuIdsByRoleAndScope(@Param("roleId") Long roleId, @Param("hospitalId") String hospitalId,
        @Param("supplierId") String supplierId);

    /**
     * 删除某医院维度下所有角色菜单（hospital_id 存为数字字符串，与 CAST(hospital_id AS CHAR) 一致）
     */
    int deleteRoleMenuByHospitalScope(@Param("hospitalId") String hospitalId);

    /**
     * 删除某供应商维度下所有角色菜单
     */
    int deleteRoleMenuBySupplierScope(@Param("supplierId") String supplierId);

    /** 活跃医院角色在医院维度下已有菜单，供全院赋权一次性比对 */
    List<MenuGrantRoleMenuPair> selectHospitalRoleMenuPairsForActiveHospitals();

    /** 活跃供应商角色在供应商维度下已有菜单，供全商赋权一次性比对 */
    List<MenuGrantRoleMenuPair> selectSupplierRoleMenuPairsForActiveSuppliers();
}
