package com.scm.system.service;

import java.util.List;
import com.scm.system.domain.SupplierUser;

/**
 * 供应商用户 服务层
 * 
 * @author scm
 */
public interface ISupplierUserService
{
    /**
     * 查询供应商用户
     * 
     * @param supplierUserId 供应商用户ID
     * @return 供应商用户信息
     */
    public SupplierUser selectSupplierUserById(Long supplierUserId);

    /**
     * 查询供应商用户列表
     * 
     * @param supplierUser 供应商用户信息
     * @return 供应商用户集合
     */
    public List<SupplierUser> selectSupplierUserList(SupplierUser supplierUser);

    /**
     * 根据供应商ID查询供应商用户列表
     * 
     * @param supplierId 供应商ID
     * @return 供应商用户集合
     */
    public List<SupplierUser> selectSupplierUserListBySupplierId(Long supplierId);

    /**
     * 根据用户ID查询供应商用户
     * 
     * @param userId 用户ID
     * @return 供应商用户信息
     */
    public SupplierUser selectSupplierUserByUserId(Long userId);

    /**
     * 获取用户作为主账号管理的供应商ID（供应商管理员）
     * @param userId 用户ID
     * @return 供应商ID，若不是任何供应商的主账号则返回 null
     */
    public Long getManagedSupplierId(Long userId);

    /**
     * 新增供应商用户
     * 
     * @param supplierUser 供应商用户信息
     * @return 结果
     */
    public int insertSupplierUser(SupplierUser supplierUser);

    /**
     * 修改供应商用户
     * 
     * @param supplierUser 供应商用户信息
     * @return 结果
     */
    public int updateSupplierUser(SupplierUser supplierUser);

    /**
     * 批量删除供应商用户
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteSupplierUserByIds(String ids);

    /**
     * 删除供应商用户信息
     * 
     * @param supplierUserId 供应商用户ID
     * @return 结果
     */
    public int deleteSupplierUserById(Long supplierUserId);
}

