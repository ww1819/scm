package com.scm.system.domain;

import javax.validation.constraints.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.scm.common.annotation.Excel;
import com.scm.common.annotation.Excel.ColumnType;
import com.scm.common.core.domain.BaseEntity;

/**
 * 医院信息表 scm_hospital
 * 
 * @author scm
 */
public class Hospital extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 医院ID */
    @Excel(name = "医院ID", cellType = ColumnType.NUMERIC)
    private Long hospitalId;

    /** 医院编码 */
    @Excel(name = "医院编码")
    private String hospitalCode;

    /** 医院名称 */
    @Excel(name = "医院名称")
    private String hospitalName;

    /** 医院简称 */
    @Excel(name = "医院简称")
    private String hospitalShortName;

    /** 医院等级 */
    @Excel(name = "医院等级")
    private String hospitalLevel;

    /** 省份/直辖市 */
    @Excel(name = "省份/直辖市")
    private String province;

    /** 城市 */
    @Excel(name = "城市")
    private String city;

    /** 县级/区 */
    @Excel(name = "县级/区")
    private String district;

    /** 详细地址 */
    @Excel(name = "详细地址")
    private String address;

    /** 联系人 */
    @Excel(name = "联系人")
    private String contactPerson;

    /** 联系电话 */
    @Excel(name = "联系电话")
    private String contactPhone;

    /** 邮箱 */
    @Excel(name = "邮箱")
    private String email;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    public Long getHospitalId()
    {
        return hospitalId;
    }

    public void setHospitalId(Long hospitalId)
    {
        this.hospitalId = hospitalId;
    }

    public String getHospitalCode()
    {
        return hospitalCode;
    }

    public void setHospitalCode(String hospitalCode)
    {
        this.hospitalCode = hospitalCode;
    }

    @NotBlank(message = "医院名称不能为空")
    @Size(min = 0, max = 200, message = "医院名称不能超过200个字符")
    public String getHospitalName()
    {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName)
    {
        this.hospitalName = hospitalName;
    }

    @Size(min = 0, max = 100, message = "医院简称不能超过100个字符")
    public String getHospitalShortName()
    {
        return hospitalShortName;
    }

    public void setHospitalShortName(String hospitalShortName)
    {
        this.hospitalShortName = hospitalShortName;
    }

    @Size(min = 0, max = 50, message = "医院等级不能超过50个字符")
    public String getHospitalLevel()
    {
        return hospitalLevel;
    }

    public void setHospitalLevel(String hospitalLevel)
    {
        this.hospitalLevel = hospitalLevel;
    }

    @Size(min = 0, max = 50, message = "省份/直辖市不能超过50个字符")
    public String getProvince()
    {
        return province;
    }

    public void setProvince(String province)
    {
        this.province = province;
    }

    @Size(min = 0, max = 50, message = "城市不能超过50个字符")
    public String getCity()
    {
        return city;
    }

    public void setCity(String city)
    {
        this.city = city;
    }

    @Size(min = 0, max = 50, message = "县级/区不能超过50个字符")
    public String getDistrict()
    {
        return district;
    }

    public void setDistrict(String district)
    {
        this.district = district;
    }

    @Size(min = 0, max = 500, message = "详细地址不能超过500个字符")
    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    @Size(min = 0, max = 50, message = "联系人不能超过50个字符")
    public String getContactPerson()
    {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson)
    {
        this.contactPerson = contactPerson;
    }

    @Size(min = 0, max = 20, message = "联系电话不能超过20个字符")
    public String getContactPhone()
    {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone)
    {
        this.contactPhone = contactPhone;
    }

    @Email(message = "邮箱格式不正确")
    @Size(min = 0, max = 100, message = "邮箱不能超过100个字符")
    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("hospitalId", getHospitalId())
            .append("hospitalCode", getHospitalCode())
            .append("hospitalName", getHospitalName())
            .append("hospitalShortName", getHospitalShortName())
            .append("hospitalLevel", getHospitalLevel())
            .append("province", getProvince())
            .append("city", getCity())
            .append("district", getDistrict())
            .append("address", getAddress())
            .append("contactPerson", getContactPerson())
            .append("contactPhone", getContactPhone())
            .append("email", getEmail())
            .append("status", getStatus())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}

