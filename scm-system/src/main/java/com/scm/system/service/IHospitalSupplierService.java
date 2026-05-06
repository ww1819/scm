package com.scm.system.service;

import java.util.List;
import com.scm.system.domain.HospitalSupplier;
import com.scm.system.domain.ScmHospitalSupplierApply;

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
     * 供应商产品档案场景：已审核、未停用、正常状态下的关联医院列表（含医院编码）
     */
    public List<HospitalSupplier> selectSupplierLinkedHospitalsForProduct(Long supplierId);

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

    /**
     * 供应商向医院发起关联申请（待医院审核）
     *
     * @param supplierId 供应商ID
     * @param hospitalId 医院ID
     * @param supplyStartDate 供货开始日期
     * @param supplyEndDate 供货结束日期
     * @param createBy 发起人
     * @return 结果
     */
    public int submitAssociationFromSupplier(Long supplierId, Long hospitalId, java.util.Date supplyStartDate,
        java.util.Date supplyEndDate, String createBy, String contractNo, String applyReason, String contactPerson,
        String contactPhone);

    /**
     * 医院侧查看关联申请列表
     */
    public List<ScmHospitalSupplierApply> selectAssociationApplyList(ScmHospitalSupplierApply query);

    /**
     * 供应商侧查看自己的关联申请
     */
    public List<ScmHospitalSupplierApply> selectSupplierApplyList(Long supplierId, String auditStatus);

    /**
     * 供应商撤回申请（仅待审核）
     */
    public int withdrawAssociationApply(String applyId, Long supplierId, String operBy);

    /**
     * 医院审核申请（通过后写入 scm_hospital_supplier）
     */
    public int auditAssociationApply(String applyId, String approved, String auditRemark, String operBy, Long hospitalCtx);
}

