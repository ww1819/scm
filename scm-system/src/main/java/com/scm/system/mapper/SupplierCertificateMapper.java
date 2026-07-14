package com.scm.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.scm.system.domain.SupplierCertificate;

/**
 * 供应商证件 数据层
 * 
 * @author scm
 */
public interface SupplierCertificateMapper
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
     * @return 证件集合
     */
    public List<SupplierCertificate> selectSupplierCertificateListBySupplierIds(@Param("supplierCertificate") SupplierCertificate supplierCertificate, @Param("supplierIds") List<Long> supplierIds, @Param("hospitalId") Long hospitalId);

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
     * 仅更新证照图片路径（允许置空）
     */
    public int updateSupplierCertificateFile(SupplierCertificate supplierCertificate);

    /**
     * 上传/修改页：更新证照信息与图片（不含证件类型、审核状态）
     */
    public int updateSupplierCertificateUpload(SupplierCertificate supplierCertificate);

    /**
     * 删除供应商证件信息
     * 
     * @param certificateId 证件主键
     * @return 结果
     */
    public int deleteSupplierCertificateById(Long certificateId);

    /**
     * 批量删除供应商证件信息
     * 
     * @param certificateIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteSupplierCertificateByIds(String[] certificateIds);

    int countBySupplierIdAndCertificateType(@Param("supplierId") Long supplierId,
        @Param("hospitalId") Long hospitalId, @Param("certificateType") String certificateType);

    int deleteDuplicateCertificatesBySupplierId(@Param("supplierId") Long supplierId,
        @Param("hospitalId") Long hospitalId);

    int assignNullHospitalCertificates(@Param("supplierId") Long supplierId, @Param("hospitalId") Long hospitalId);

    int countBySupplierAndHospital(@Param("supplierId") Long supplierId, @Param("hospitalId") Long hospitalId);

    List<String> selectCertificateTypeNamesBySupplierAndHospital(@Param("supplierId") Long supplierId,
        @Param("hospitalId") Long hospitalId);

    /**
     * 将误存为 type_id 或 type_code 的 certificate_type 修正为类型名称
     */
    int repairStoredCertificateTypeValues();
}

