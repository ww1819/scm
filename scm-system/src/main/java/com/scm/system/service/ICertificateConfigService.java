package com.scm.system.service;

import java.util.List;
import com.scm.system.domain.CertificateConfig;

/**
 * 证件配置 服务层
 * 
 * @author scm
 */
public interface ICertificateConfigService
{
    /**
     * 查询证件配置
     *
     * @param configId 证件配置主键
     * @return 证件配置
     */
    public CertificateConfig selectCertificateConfigByConfigId(Long configId);

    /**
     * 查询证件配置列表
     *
     * @param certificateConfig 证件配置
     * @return 证件配置集合
     */
    public List<CertificateConfig> selectCertificateConfigList(CertificateConfig certificateConfig);

    /**
     * 根据配置类型和证件类型查询配置
     *
     * @param configType 配置类型
     * @param certificateType 证件类型
     * @return 证件配置
     */
    public CertificateConfig selectCertificateConfigByType(String configType, String certificateType);

    /**
     * 获取预警天数（如果配置不存在，返回默认值30）
     *
     * @param configType 配置类型
     * @param certificateType 证件类型
     * @return 预警天数
     */
    public Integer getWarningDays(String configType, String certificateType);

    /**
     * 新增证件配置
     *
     * @param certificateConfig 证件配置
     * @return 结果
     */
    public int insertCertificateConfig(CertificateConfig certificateConfig);

    /**
     * 修改证件配置
     *
     * @param certificateConfig 证件配置
     * @return 结果
     */
    public int updateCertificateConfig(CertificateConfig certificateConfig);

    /**
     * 批量删除证件配置
     *
     * @param configIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCertificateConfigByConfigIds(String ids);

    /**
     * 删除证件配置信息
     *
     * @param configId 证件配置主键
     * @return 结果
     */
    public int deleteCertificateConfigByConfigId(Long configId);
}

