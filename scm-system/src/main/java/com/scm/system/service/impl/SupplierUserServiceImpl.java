package com.scm.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.scm.common.core.text.Convert;
import com.scm.system.mapper.SupplierUserMapper;
import com.scm.system.domain.SupplierUser;
import com.scm.system.service.ISupplierUserService;

/**
 * 供应商用户 服务层实现
 * 
 * @author scm
 */
@Service
public class SupplierUserServiceImpl implements ISupplierUserService
{
    @Autowired
    private SupplierUserMapper supplierUserMapper;

    /**
     * 查询供应商用户
     * 
     * @param supplierUserId 供应商用户ID
     * @return 供应商用户信息
     */
    @Override
    public SupplierUser selectSupplierUserById(Long supplierUserId)
    {
        return supplierUserMapper.selectSupplierUserById(supplierUserId);
    }

    /**
     * 查询供应商用户列表
     * 
     * @param supplierUser 供应商用户信息
     * @return 供应商用户集合
     */
    @Override
    public List<SupplierUser> selectSupplierUserList(SupplierUser supplierUser)
    {
        return supplierUserMapper.selectSupplierUserList(supplierUser);
    }

    /**
     * 根据供应商ID查询供应商用户列表
     * 
     * @param supplierId 供应商ID
     * @return 供应商用户集合
     */
    @Override
    public List<SupplierUser> selectSupplierUserListBySupplierId(Long supplierId)
    {
        return supplierUserMapper.selectSupplierUserListBySupplierId(supplierId);
    }

    /**
     * 根据用户ID查询供应商用户
     * 
     * @param userId 用户ID
     * @return 供应商用户信息
     */
    @Override
    public SupplierUser selectSupplierUserByUserId(Long userId)
    {
        return supplierUserMapper.selectSupplierUserByUserId(userId);
    }

    /**
     * 新增供应商用户
     * 
     * @param supplierUser 供应商用户信息
     * @return 结果
     */
    @Override
    public int insertSupplierUser(SupplierUser supplierUser)
    {
        return supplierUserMapper.insertSupplierUser(supplierUser);
    }

    /**
     * 修改供应商用户
     * 
     * @param supplierUser 供应商用户信息
     * @return 结果
     */
    @Override
    public int updateSupplierUser(SupplierUser supplierUser)
    {
        return supplierUserMapper.updateSupplierUser(supplierUser);
    }

    /**
     * 批量删除供应商用户
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    @Override
    public int deleteSupplierUserByIds(String ids)
    {
        return supplierUserMapper.deleteSupplierUserByIds(Convert.toStrArray(ids));
    }

    /**
     * 删除供应商用户信息
     * 
     * @param supplierUserId 供应商用户ID
     * @return 结果
     */
    @Override
    public int deleteSupplierUserById(Long supplierUserId)
    {
        return supplierUserMapper.deleteSupplierUserById(supplierUserId);
    }
}

