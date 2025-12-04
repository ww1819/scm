package com.scm.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.scm.common.core.text.Convert;
import com.scm.common.utils.DateUtils;
import com.scm.system.domain.CertificateConfig;
import com.scm.system.mapper.CertificateConfigMapper;
import com.scm.system.service.ICertificateConfigService;

/**
 * 证件配置 服务层实现
 * 
 * @author scm
 */
@Service
public class CertificateConfigServiceImpl implements ICertificateConfigService
{
    @Autowired
    private CertificateConfigMapper certificateConfigMapper;

    /**
     * 查询证件配置
     *
     * @param configId 证件配置主键
     * @return 证件配置
     */
    @Override
    public CertificateConfig selectCertificateConfigByConfigId(Long configId)
    {
        return certificateConfigMapper.selectCertificateConfigByConfigId(configId);
    }

    /**
     * 查询证件配置列表
     *
     * @param certificateConfig 证件配置
     * @return 证件配置
     */
    @Override
    public List<CertificateConfig> selectCertificateConfigList(CertificateConfig certificateConfig)
    {
        return certificateConfigMapper.selectCertificateConfigList(certificateConfig);
    }

    /**
     * 根据配置类型和证件类型查询配置
     *
     * @param configType 配置类型
     * @param certificateType 证件类型
     * @return 证件配置
     */
    @Override
    public CertificateConfig selectCertificateConfigByType(String configType, String certificateType)
    {
        return certificateConfigMapper.selectCertificateConfigByType(configType, certificateType);
    }

    /**
     * 获取预警天数（如果配置不存在，返回默认值30）
     *
     * @param configType 配置类型
     * @param certificateType 证件类型
     * @return 预警天数
     */
    @Override
    public Integer getWarningDays(String configType, String certificateType)
    {
        CertificateConfig config = certificateConfigMapper.selectCertificateConfigByType(configType, certificateType);
        if (config != null && config.getWarningDays() != null)
        {
            return config.getWarningDays();
        }
        return 30; // 默认30天
    }

    /**
     * 新增证件配置
     *
     * @param certificateConfig 证件配置
     * @return 结果
     */
    @Override
    public int insertCertificateConfig(CertificateConfig certificateConfig)
    {
        certificateConfig.setCreateTime(DateUtils.getNowDate());
        return certificateConfigMapper.insertCertificateConfig(certificateConfig);
    }

    /**
     * 修改证件配置
     *
     * @param certificateConfig 证件配置
     * @return 结果
     */
    @Override
    public int updateCertificateConfig(CertificateConfig certificateConfig)
    {
        certificateConfig.setUpdateTime(DateUtils.getNowDate());
        return certificateConfigMapper.updateCertificateConfig(certificateConfig);
    }

    /**
     * 批量删除证件配置
     *
     * @param configIds 需要删除的证件配置主键
     * @return 结果
     */
    @Override
    public int deleteCertificateConfigByConfigIds(String configIds)
    {
        return certificateConfigMapper.deleteCertificateConfigByConfigIds(Convert.toStrArray(configIds));
    }

    /**
     * 删除证件配置信息
     *
     * @param configId 证件配置主键
     * @return 结果
     */
    @Override
    public int deleteCertificateConfigByConfigId(Long configId)
    {
        return certificateConfigMapper.deleteCertificateConfigByConfigId(configId);
    }
}

