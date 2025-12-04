package com.scm.system.mapper;

import java.util.List;
import com.scm.system.domain.SupplierUser;

/**
 * 供应商用户 数据层
 * 
 * @author scm
 */
public interface SupplierUserMapper
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
     * 删除供应商用户
     * 
     * @param supplierUserId 供应商用户主键
     * @return 结果
     */
    public int deleteSupplierUserById(Long supplierUserId);

    /**
     * 批量删除供应商用户
     * 
     * @param supplierUserIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteSupplierUserByIds(String[] supplierUserIds);

    /**
     * 根据供应商ID删除供应商用户
     * 
     * @param supplierId 供应商ID
     * @return 结果
     */
    public int deleteSupplierUserBySupplierId(Long supplierId);
}

