package com.scm.system.domain.vo;

import java.io.Serializable;

/**
 * 按医院维度聚合的产品档案（物资）摘要，用于医院产品档案页中间表。
 */
public class ProductMaterialArchiveVo implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long materialId;

    private String materialName;

    private String materialCode;

    /** 该院下该产品已有证件条数 */
    private Integer certCount;

    public Long getMaterialId()
    {
        return materialId;
    }

    public void setMaterialId(Long materialId)
    {
        this.materialId = materialId;
    }

    public String getMaterialName()
    {
        return materialName;
    }

    public void setMaterialName(String materialName)
    {
        this.materialName = materialName;
    }

    public String getMaterialCode()
    {
        return materialCode;
    }

    public void setMaterialCode(String materialCode)
    {
        this.materialCode = materialCode;
    }

    public Integer getCertCount()
    {
        return certCount;
    }

    public void setCertCount(Integer certCount)
    {
        this.certCount = certCount;
    }
}
