package com.scm.system.domain;

import javax.validation.constraints.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.scm.common.annotation.Excel;
import com.scm.common.annotation.Excel.ColumnType;
import com.scm.common.core.domain.BaseEntity;

/**
 * 证件配置表 scm_certificate_config
 * 
 * @author scm
 */
public class CertificateConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 配置ID */
    @Excel(name = "配置ID", cellType = ColumnType.NUMERIC)
    private Long configId;

    /** 配置类型（supplier_certificate供应商证件 product_certificate产品证件） */
    @Excel(name = "配置类型", readConverterExp = "supplier_certificate=供应商证件,product_certificate=产品证件")
    private String configType;

    /** 证件类型 */
    @Excel(name = "证件类型")
    private String certificateType;

    /** 预警天数 */
    @Excel(name = "预警天数", cellType = ColumnType.NUMERIC)
    private Integer warningDays;

    /** 近期证件天数 */
    @Excel(name = "近期证件天数", cellType = ColumnType.NUMERIC)
    private Integer recentDays;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public Long getConfigId()
    {
        return configId;
    }

    public void setConfigId(Long configId)
    {
        this.configId = configId;
    }

    @NotBlank(message = "配置类型不能为空")
    @Size(min = 0, max = 50, message = "配置类型不能超过50个字符")
    public String getConfigType()
    {
        return configType;
    }

    public void setConfigType(String configType)
    {
        this.configType = configType;
    }

    @Size(min = 0, max = 50, message = "证件类型不能超过50个字符")
    public String getCertificateType()
    {
        return certificateType;
    }

    public void setCertificateType(String certificateType)
    {
        this.certificateType = certificateType;
    }

    @NotNull(message = "预警天数不能为空")
    @Min(value = 1, message = "预警天数不能小于1")
    @Max(value = 365, message = "预警天数不能超过365")
    public Integer getWarningDays()
    {
        return warningDays;
    }

    public void setWarningDays(Integer warningDays)
    {
        this.warningDays = warningDays;
    }

    public Integer getRecentDays()
    {
        return recentDays;
    }

    public void setRecentDays(Integer recentDays)
    {
        this.recentDays = recentDays;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("configId", getConfigId())
            .append("configType", getConfigType())
            .append("certificateType", getCertificateType())
            .append("warningDays", getWarningDays())
            .append("recentDays", getRecentDays())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}

