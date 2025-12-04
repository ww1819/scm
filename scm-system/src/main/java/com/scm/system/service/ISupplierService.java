package com.scm.system.service;

import java.util.List;
import com.scm.system.domain.Supplier;

/**
 * 供应商信息 服务层
 * 
 * @author scm
 */
public interface ISupplierService
{
    /**
     * 查询供应商信息
     * 
     * @param supplierId 供应商ID
     * @return 供应商信息
     */
    public Supplier selectSupplierById(Long supplierId);

    /**
     * 查询供应商信息列表
     * 
     * @param supplier 供应商信息
     * @return 供应商集合
     */
    public List<Supplier> selectSupplierList(Supplier supplier);

    /**
     * 新增供应商信息
     * 
     * @param supplier 供应商信息
     * @return 结果
     */
    public int insertSupplier(Supplier supplier);

    /**
     * 修改供应商信息
     * 
     * @param supplier 供应商信息
     * @return 结果
     */
    public int updateSupplier(Supplier supplier);

    /**
     * 批量删除供应商信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteSupplierByIds(String ids);

    /**
     * 删除供应商信息
     * 
     * @param supplierId 供应商ID
     * @return 结果
     */
    public int deleteSupplierById(Long supplierId);

    /**
     * 校验供应商编码是否唯一
     * 
     * @param supplier 供应商信息
     * @return 结果
     */
    public boolean checkSupplierCodeUnique(Supplier supplier);

    /**
     * 审核供应商
     * 
     * @param supplier 供应商信息
     * @return 结果
     */
    public int auditSupplier(Supplier supplier);
}

