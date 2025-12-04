package com.scm.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.scm.common.core.text.Convert;
import com.scm.common.utils.DateUtils;
import com.scm.common.utils.StringUtils;
import com.scm.system.domain.CertificateType;
import com.scm.system.mapper.CertificateTypeMapper;
import com.scm.system.service.ICertificateTypeService;

/**
 * 证件类型 服务层实现
 * 
 * @author scm
 */
@Service
public class CertificateTypeServiceImpl implements ICertificateTypeService
{
    @Autowired
    private CertificateTypeMapper certificateTypeMapper;

    /**
     * 查询证件类型信息
     * 
     * @param typeId 类型ID
     * @return 类型信息
     */
    @Override
    public CertificateType selectCertificateTypeById(Long typeId)
    {
        return certificateTypeMapper.selectCertificateTypeById(typeId);
    }

    /**
     * 查询证件类型列表
     * 
     * @param certificateType 类型信息
     * @return 类型集合
     */
    @Override
    public List<CertificateType> selectCertificateTypeList(CertificateType certificateType)
    {
        return certificateTypeMapper.selectCertificateTypeList(certificateType);
    }

    /**
     * 新增证件类型信息
     * 
     * @param certificateType 类型信息
     * @return 结果
     */
    @Override
    public int insertCertificateType(CertificateType certificateType)
    {
        if (StringUtils.isEmpty(certificateType.getStatus()))
        {
            certificateType.setStatus("0");
        }
        if (certificateType.getOrderNum() == null)
        {
            certificateType.setOrderNum(0);
        }
        certificateType.setCreateTime(DateUtils.getNowDate());
        return certificateTypeMapper.insertCertificateType(certificateType);
    }

    /**
     * 修改证件类型信息
     * 
     * @param certificateType 类型信息
     * @return 结果
     */
    @Override
    public int updateCertificateType(CertificateType certificateType)
    {
        certificateType.setUpdateTime(DateUtils.getNowDate());
        return certificateTypeMapper.updateCertificateType(certificateType);
    }

    /**
     * 批量删除证件类型信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    @Override
    public int deleteCertificateTypeByIds(String ids)
    {
        return certificateTypeMapper.deleteCertificateTypeByIds(Convert.toStrArray(ids));
    }

    /**
     * 删除证件类型信息
     * 
     * @param typeId 类型ID
     * @return 结果
     */
    @Override
    public int deleteCertificateTypeById(Long typeId)
    {
        return certificateTypeMapper.deleteCertificateTypeById(typeId);
    }

    /**
     * 校验类型编码是否唯一
     * 
     * @param certificateType 类型信息
     * @return 结果
     */
    @Override
    public boolean checkTypeCodeUnique(CertificateType certificateType)
    {
        Long typeId = StringUtils.isNull(certificateType.getTypeId()) ? -1L : certificateType.getTypeId();
        CertificateType info = certificateTypeMapper.checkTypeCodeUnique(certificateType.getTypeCode());
        if (StringUtils.isNotNull(info) && info.getTypeId().longValue() != typeId.longValue())
        {
            return false;
        }
        return true;
    }
}

