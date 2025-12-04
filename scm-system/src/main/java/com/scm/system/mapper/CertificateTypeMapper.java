package com.scm.system.mapper;

import java.util.List;
import com.scm.system.domain.CertificateType;

/**
 * 证件类型 数据层
 * 
 * @author scm
 */
public interface CertificateTypeMapper
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
     * 根据类型编码查询类型
     * 
     * @param typeCode 类型编码
     * @return 类型信息
     */
    public CertificateType checkTypeCodeUnique(String typeCode);

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
     * 删除证件类型信息
     * 
     * @param typeId 类型主键
     * @return 结果
     */
    public int deleteCertificateTypeById(Long typeId);

    /**
     * 批量删除证件类型信息
     * 
     * @param typeIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteCertificateTypeByIds(String[] typeIds);
}

