package com.scm.system.mapper;

import java.util.List;
import com.scm.system.domain.Supplier;

/**
 * 供应商信息 数据层
 * 
 * @author scm
 */
public interface SupplierMapper
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
     * 根据供应商编码查询供应商
     * 
     * @param supplierCode 供应商编码
     * @return 供应商信息
     */
    public Supplier checkSupplierCodeUnique(String supplierCode);

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
     * 删除供应商信息
     * 
     * @param supplierId 供应商主键
     * @return 结果
     */
    public int deleteSupplierById(Long supplierId);

    /**
     * 批量删除供应商信息
     * 
     * @param supplierIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteSupplierByIds(String[] supplierIds);
}

