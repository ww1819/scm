package com.scm.system.service;

import java.util.List;
import com.scm.system.domain.HospitalSupplier;

/**
 * 医院供应商关联 服务层
 * 
 * @author scm
 */
public interface IHospitalSupplierService
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
     * 批量删除医院供应商关联
     * 
     * @param relationIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteHospitalSupplierByIds(String relationIds);

    /**
     * 根据供应商ID删除所有关联
     * 
     * @param supplierId 供应商ID
     * @return 结果
     */
    public int deleteHospitalSupplierBySupplierId(Long supplierId);

    /**
     * 保存供应商的配送公司（先删除旧的，再插入新的）
     * 
     * @param supplierId 供应商ID
     * @param hospitalIds 医院ID数组
     * @param createBy 创建人
     * @return 结果
     */
    public int saveSupplierHospitals(Long supplierId, String[] hospitalIds, String createBy);
}

