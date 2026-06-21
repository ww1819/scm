package com.scm.system.service;

import java.util.List;
import com.scm.system.domain.SupplierCertificate;

/**
 * 供应商证件 服务层
 * 
 * @author scm
 */
public interface ISupplierCertificateService
{
    /**
     * 查询供应商证件信息
     * 
     * @param certificateId 证件ID
     * @return 证件信息
     */
    public SupplierCertificate selectSupplierCertificateById(Long certificateId);

    /**
     * 查询供应商证件列表
     * 
     * @param supplierCertificate 证件信息
     * @return 证件集合
     */
    public List<SupplierCertificate> selectSupplierCertificateList(SupplierCertificate supplierCertificate);

    /**
     * 根据供应商ID列表查询供应商证件列表
     * 
     * @param supplierCertificate 证件信息
     * @param supplierIds 供应商ID列表
     * @param hospitalId 医院ID（可选，用于过滤只显示绑定该医院的供应商证件）
     * @return 证件集合
     */
    public List<SupplierCertificate> selectSupplierCertificateListBySupplierIds(SupplierCertificate supplierCertificate, List<Long> supplierIds, Long hospitalId);

    /**
     * 查询过期预警的供应商证件列表
     * 
     * @param supplierCertificate 证件信息
     * @return 证件集合
     */
    public List<SupplierCertificate> selectExpiringCertificateList(SupplierCertificate supplierCertificate);

    /**
     * 新增供应商证件信息
     * 
     * @param supplierCertificate 证件信息
     * @return 结果
     */
    public int insertSupplierCertificate(SupplierCertificate supplierCertificate);

    /**
     * 修改供应商证件信息
     * 
     * @param supplierCertificate 证件信息
     * @return 结果
     */
    public int updateSupplierCertificate(SupplierCertificate supplierCertificate);

    /**
     * 仅更新证照图片（可清空）
     */
    public int updateCertificateFile(Long certificateId, String certificateFileIds, String updateBy);

    /**
     * 上传/修改页：更新证照信息与图片（未审核方可修改）
     */
    public int updateSupplierCertificateUpload(SupplierCertificate supplierCertificate);

    /**
     * 批量删除供应商证件信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteSupplierCertificateByIds(String ids);

    /**
     * 删除供应商证件信息
     * 
     * @param certificateId 证件ID
     * @return 结果
     */
    public int deleteSupplierCertificateById(Long certificateId);

    /**
     * 审核供应商证件
     * 
     * @param supplierCertificate 证件信息
     * @return 结果
     */
    public int auditSupplierCertificate(SupplierCertificate supplierCertificate);

    /**
     * 检查并更新证件过期状态
     */
    public void checkAndUpdateExpiredStatus();

    /**
     * 按「证件配置 supplier_certificate」与 scm_certificate_type 为供应商在各关联医院补齐缺失的证件占位行
     */
    public void ensureMissingCertificatesForSupplier(Long supplierId, String createBy);

    /**
     * 为指定供应商在指定医院补齐缺失的企业证件占位行
     */
    public void ensureMissingCertificatesForSupplierAtHospital(Long supplierId, Long hospitalId, String createBy);

    /**
     * 登记/查询列表前，按当前筛选上下文为相关供应商补齐企业证件占位行
     */
    public void ensureMissingCertificatesForListContext(Long bindSupplierId, String supplierIdsCsv, Long hospitalId,
        String createBy);
}

