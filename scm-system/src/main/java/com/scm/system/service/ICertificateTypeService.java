package com.scm.system.service;

import java.util.List;
import com.scm.system.domain.CertificateType;

/**
 * 证件类型 服务层
 * 
 * @author scm
 */
public interface ICertificateTypeService
{
    /**
     * 查询证件类型信息
     * 
     * @param typeId 类型ID
     * @return 类型信息
     */
    public CertificateType selectCertificateTypeById(Long typeId);

    /**
     * 查询证件类型列表
     * 
     * @param certificateType 类型信息
     * @return 类型集合
     */
    public List<CertificateType> selectCertificateTypeList(CertificateType certificateType);

    /**
     * 新增证件类型信息
     * 
     * @param certificateType 类型信息
     * @return 结果
     */
    public int insertCertificateType(CertificateType certificateType);

    /**
     * 修改证件类型信息
     * 
     * @param certificateType 类型信息
     * @return 结果
     */
    public int updateCertificateType(CertificateType certificateType);

    /**
     * 批量删除证件类型信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteCertificateTypeByIds(String ids);

    /**
     * 删除证件类型信息
     * 
     * @param typeId 类型ID
     * @return 结果
     */
    public int deleteCertificateTypeById(Long typeId);

    /**
     * 校验类型编码是否唯一
     * 
     * @param certificateType 类型信息
     * @return 结果
     */
    public boolean checkTypeCodeUnique(CertificateType certificateType);

    /**
     * 产品证件扩展证照：按 scm_certificate_config(config_type=product_certificate) 与 scm_certificate_type 联动；
     * 若配置为空则回退为 type_category=product 的全部启用类型。
     */
    public List<CertificateType> selectProductExtensionTypesForSnap();

    /**
     * 供应商证件：config_type=supplier_certificate，配置为空则回退 type_category=supplier。
     */
    public List<CertificateType> selectSupplierExtensionTypesForSnap();

    /** 按类型编码取一条启用类型（无则返回 null） */
    public CertificateType selectByTypeCode(String typeCode);
}

