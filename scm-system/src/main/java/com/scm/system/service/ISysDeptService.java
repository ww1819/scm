package com.scm.system.service;

import java.util.List;
import com.scm.common.core.domain.Ztree;
import com.scm.common.core.domain.entity.SysDept;
import com.scm.common.core.domain.entity.SysRole;

/**
 * 部门管理 服务层
 * 
 * @author scm
 */
public interface ISysDeptService
{
    /**
     * 查询部门管理数据
     * 
     * @param dept 部门信息
     * @return 部门信息集合
     */
    public List<SysDept> selectDeptList(SysDept dept);

    /**
     * 为部门列表填充树表末级标记（无直属子部门则为 true），用于懒加载分页
     *
     * @param depts 当前页或当前批次的部门
     */
    public void fillDeptTreeLeafFlag(List<SysDept> depts);

    /**
     * 查询部门管理树
     * 
     * @param dept 部门信息
     * @return 所有部门信息
     */
    public List<Ztree> selectDeptTree(SysDept dept);

    /**
     * 查询部门管理树（排除下级）
     * 
     * @param dept 部门信息
     * @return 所有部门信息
     */
    public List<Ztree> selectDeptTreeExcludeChild(SysDept dept);

    /**
     * 根据角色ID查询菜单
     *
     * @param role 角色对象
     * @return 菜单列表
     */
    public List<Ztree> roleDeptTreeData(SysRole role);

    /**
     * 根据父部门ID查询下级部门数量
     * 
     * @param parentId 父部门ID
     * @return 结果
     */
    public int selectDeptCount(Long parentId);

    /**
     * 查询部门是否存在用户
     * 
     * @param deptId 部门ID
     * @return 结果 true 存在 false 不存在
     */
    public boolean checkDeptExistUser(Long deptId);

    /**
     * 删除部门管理信息
     * 
     * @param deptId 部门ID
     * @return 结果
     */
    public int deleteDeptById(Long deptId);

    /**
     * 新增保存部门信息
     * 
     * @param dept 部门信息
     * @return 结果
     */
    public int insertDept(SysDept dept);

    /**
     * 修改保存部门信息
     * 
     * @param dept 部门信息
     * @return 结果
     */
    public int updateDept(SysDept dept);

    /**
     * 根据部门ID查询信息
     * 
     * @param deptId 部门ID
     * @return 部门信息
     */
    public SysDept selectDeptById(Long deptId);

    /**
     * 根据ID查询所有子部门（正常状态）
     * 
     * @param deptId 部门ID
     * @return 子部门数
     */
    public int selectNormalChildrenDeptById(Long deptId);

    /**
     * 校验部门名称是否唯一
     * 
     * @param dept 部门信息
     * @return 结果
     */
    public boolean checkDeptNameUnique(SysDept dept);

    /**
     * 校验部门是否有数据权限
     * 
     * @param deptId 部门id
     */
    public void checkDeptDataScope(Long deptId);

    /**
     * 平台根部门：优先名称为「医承云配」且 parent_id=0；不存在则取第一个根部门（用于超级管理员默认归属）
     */
    public SysDept selectPreferredRootDept();

    /**
     * 供应商注册页省市区下拉：不传或 parentId≤0 时取「医承云配」下直属部门作为省；否则取该父部门直属子部门（不应用数据权限）
     */
    public List<SysDept> listDeptChildrenForSupplierRegister(Long parentDeptId);
}
