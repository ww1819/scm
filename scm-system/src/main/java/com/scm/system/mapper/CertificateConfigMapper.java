package com.scm.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.scm.system.domain.CertificateConfig;

/**
 * 证件配置 数据层
 * 
 * @author scm
 */
public interface CertificateConfigMapper
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
    public CertificateConfig selectCertificateConfigByType(@Param("configType") String configType, @Param("certificateType") String certificateType);

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
     * 删除证件配置
     *
     * @param configId 证件配置主键
     * @return 结果
     */
    public int deleteCertificateConfigByConfigId(Long configId);

    /**
     * 批量删除证件配置
     *
     * @param configIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCertificateConfigByConfigIds(String[] configIds);
}

