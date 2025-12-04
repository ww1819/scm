package com.scm.system.mapper;

import java.util.List;
import com.scm.system.domain.HospitalSupplier;

/**
 * 医院供应商关联 数据层
 * 
 * @author scm
 */
public interface HospitalSupplierMapper
{
    /**
     * 查询医院供应商关联信息
     * 
     * @param relationId 关联ID
     * @return 关联信息
     */
    public HospitalSupplier selectHospitalSupplierById(Long relationId);

    /**
     * 查询医院供应商关联列表
     * 
     * @param hospitalSupplier 关联信息
     * @return 关联集合
     */
    public List<HospitalSupplier> selectHospitalSupplierList(HospitalSupplier hospitalSupplier);

    /**
     * 根据供应商ID查询关联的医院列表
     * 
     * @param supplierId 供应商ID
     * @return 关联集合
     */
    public List<HospitalSupplier> selectHospitalSupplierBySupplierId(Long supplierId);

    /**
     * 新增医院供应商关联
     * 
     * @param hospitalSupplier 关联信息
     * @return 结果
     */
    public int insertHospitalSupplier(HospitalSupplier hospitalSupplier);

    /**
     * 修改医院供应商关联
     * 
     * @param hospitalSupplier 关联信息
     * @return 结果
     */
    public int updateHospitalSupplier(HospitalSupplier hospitalSupplier);

    /**
     * 删除医院供应商关联
     * 
     * @param relationId 关联主键
     * @return 结果
     */
    public int deleteHospitalSupplierById(Long relationId);

    /**
     * 批量删除医院供应商关联
     * 
     * @param relationIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteHospitalSupplierByIds(String[] relationIds);

    /**
     * 根据供应商ID删除所有关联
     * 
     * @param supplierId 供应商ID
     * @return 结果
     */
    public int deleteHospitalSupplierBySupplierId(Long supplierId);
}

