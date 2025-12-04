package com.scm.system.domain;

import javax.validation.constraints.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.scm.common.annotation.Excel;
import com.scm.common.annotation.Excel.ColumnType;
import com.scm.common.core.domain.BaseEntity;

/**
 * 证件类型表 scm_certificate_type
 * 
 * @author scm
 */
public class CertificateType extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 类型ID */
    @Excel(name = "类型ID", cellType = ColumnType.NUMERIC)
    private Long typeId;

    /** 类型编码 */
    @Excel(name = "类型编码")
    private String typeCode;

    /** 类型名称 */
    @Excel(name = "类型名称")
    private String typeName;

    /** 类型分类（supplier供应商证件 product产品证件） */
    @Excel(name = "类型分类", readConverterExp = "supplier=供应商证件,product=产品证件")
    private String typeCategory;

    /** 类型描述 */
    @Excel(name = "类型描述")
    private String description;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 显示顺序 */
    @Excel(name = "显示顺序", cellType = ColumnType.NUMERIC)
    private Integer orderNum;

    public Long getTypeId()
    {
        return typeId;
    }

    public void setTypeId(Long typeId)
    {
        this.typeId = typeId;
    }

    @Size(min = 0, max = 50, message = "类型编码不能超过50个字符")
    public String getTypeCode()
    {
        return typeCode;
    }

    public void setTypeCode(String typeCode)
    {
        this.typeCode = typeCode;
    }

    @NotBlank(message = "类型名称不能为空")
    @Size(min = 0, max = 100, message = "类型名称不能超过100个字符")
    public String getTypeName()
    {
        return typeName;
    }

    public void setTypeName(String typeName)
    {
        this.typeName = typeName;
    }

    @Size(min = 0, max = 50, message = "类型分类不能超过50个字符")
    public String getTypeCategory()
    {
        return typeCategory;
    }

    public void setTypeCategory(String typeCategory)
    {
        this.typeCategory = typeCategory;
    }

    @Size(min = 0, max = 500, message = "类型描述不能超过500个字符")
    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Integer getOrderNum()
    {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum)
    {
        this.orderNum = orderNum;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("typeId", getTypeId())
            .append("typeCode", getTypeCode())
            .append("typeName", getTypeName())
            .append("typeCategory", getTypeCategory())
            .append("description", getDescription())
            .append("status", getStatus())
            .append("orderNum", getOrderNum())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}

