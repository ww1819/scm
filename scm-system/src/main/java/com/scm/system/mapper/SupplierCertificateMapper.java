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
    public List<SupplierCertificate> selectSupplierCertificateListBySupplierIds(@Param("supplierCertificate") SupplierCertificate supplierCertificate, @Param("supplierIds") List<Long> supplierIds);

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
}

