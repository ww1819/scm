package com.scm.system.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.scm.common.core.text.Convert;
import com.scm.common.utils.DateUtils;
import com.scm.common.utils.StringUtils;
import com.scm.system.domain.CertificateConfig;
import com.scm.system.domain.CertificateType;
import com.scm.system.mapper.CertificateConfigMapper;
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

    @Autowired
    private CertificateConfigMapper certificateConfigMapper;

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

    @Override
    public List<CertificateType> selectProductExtensionTypesForSnap()
    {
        return selectExtensionTypesForConfig("product_certificate", "product");
    }

    @Override
    public List<CertificateType> selectSupplierExtensionTypesForSnap()
    {
        return selectExtensionTypesForConfig("supplier_certificate", "supplier");
    }

    @Override
    public CertificateType selectByTypeCode(String typeCode)
    {
        if (StringUtils.isEmpty(typeCode))
        {
            return null;
        }
        return certificateTypeMapper.checkTypeCodeUnique(typeCode.trim());
    }

    /**
     * 展示用：以 scm_certificate_type 中该分类下全部启用类型为主；scm_certificate_config 仅用于把已配置的类型排到前面（兼容历史 PRODUCT_001 等编码）。
     */
    private List<CertificateType> selectExtensionTypesForConfig(String configType, String typeCategoryFallback)
    {
        List<CertificateType> all = certificateTypeMapper.selectActiveByTypeCategoryForSnap(typeCategoryFallback);
        if (all == null)
        {
            all = new ArrayList<>();
        }
        Map<String, CertificateType> byCode = new LinkedHashMap<>();
        for (CertificateType t : all)
        {
            if (t == null || StringUtils.isEmpty(t.getTypeCode()))
            {
                continue;
            }
            byCode.putIfAbsent(t.getTypeCode().trim(), t);
        }
        CertificateConfig q = new CertificateConfig();
        q.setConfigType(configType);
        q.setStatus("0");
        List<CertificateConfig> cfgs = certificateConfigMapper.selectCertificateConfigList(q);
        List<CertificateType> ordered = new ArrayList<>();
        Set<String> used = new HashSet<>();
        if (cfgs != null)
        {
            for (CertificateConfig c : cfgs)
            {
                if (c == null || StringUtils.isEmpty(c.getCertificateType()))
                {
                    continue;
                }
                String code = c.getCertificateType().trim();
                CertificateType t = byCode.get(code);
                if (t == null)
                {
                    t = certificateTypeMapper.checkTypeCodeUnique(code);
                }
                if (t != null && (t.getStatus() == null || "0".equals(t.getStatus())))
                {
                    if (!used.contains(t.getTypeCode().trim()))
                    {
                        ordered.add(t);
                        used.add(t.getTypeCode().trim());
                    }
                }
            }
        }
        List<CertificateType> rest = new ArrayList<>();
        for (CertificateType t : all)
        {
            if (t == null || StringUtils.isEmpty(t.getTypeCode()))
            {
                continue;
            }
            String c = t.getTypeCode().trim();
            if (!used.contains(c))
            {
                rest.add(t);
            }
        }
        rest.sort(Comparator.comparingInt(o -> o.getOrderNum() != null ? o.getOrderNum() : 0));
        ordered.addAll(rest);
        return ordered;
    }
}

