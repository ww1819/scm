package com.scm.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.scm.common.annotation.Excel;
import com.scm.common.annotation.Excel.ColumnType;
import com.scm.common.core.domain.BaseEntity;

/**
 * 供应商用户表 scm_supplier_user
 * 
 * @author scm
 */
public class SupplierUser extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 供应商用户ID */
    @Excel(name = "供应商用户ID", cellType = ColumnType.NUMERIC)
    private Long supplierUserId;

    /** 供应商ID */
    @Excel(name = "供应商ID", cellType = ColumnType.NUMERIC)
    private Long supplierId;

    /** 用户ID */
    @Excel(name = "用户ID", cellType = ColumnType.NUMERIC)
    private Long userId;

    /** 是否主账号（0否 1是） */
    @Excel(name = "是否主账号", readConverterExp = "0=否,1=是")
    private String isMain;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 供应商名称（关联查询） */
    private String supplierName;

    /** 供应商编码（关联查询） */
    private String supplierCode;

    /** 用户名称（关联查询） */
    private String userName;

    /** 登录名称（关联查询） */
    private String loginName;

    /** 手机号码（关联查询） */
    private String phonenumber;

    /** 邮箱（关联查询） */
    private String email;

    public void setSupplierUserId(Long supplierUserId) 
    {
        this.supplierUserId = supplierUserId;
    }

    public Long getSupplierUserId() 
    {
        return supplierUserId;
    }

    public void setSupplierId(Long supplierId) 
    {
        this.supplierId = supplierId;
    }

    public Long getSupplierId() 
    {
        return supplierId;
    }

    public void setUserId(Long userId) 
    {
        this.userId = userId;
    }

    public Long getUserId() 
    {
        return userId;
    }

    public void setIsMain(String isMain) 
    {
        this.isMain = isMain;
    }

    public String getIsMain() 
    {
        return isMain;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public String getSupplierName() 
    {
        return supplierName;
    }

    public void setSupplierName(String supplierName) 
    {
        this.supplierName = supplierName;
    }

    public String getSupplierCode() 
    {
        return supplierCode;
    }

    public void setSupplierCode(String supplierCode) 
    {
        this.supplierCode = supplierCode;
    }

    public String getUserName() 
    {
        return userName;
    }

    public void setUserName(String userName) 
    {
        this.userName = userName;
    }

    public String getLoginName() 
    {
        return loginName;
    }

    public void setLoginName(String loginName) 
    {
        this.loginName = loginName;
    }

    public String getPhonenumber() 
    {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) 
    {
        this.phonenumber = phonenumber;
    }

    public String getEmail() 
    {
        return email;
    }

    public void setEmail(String email) 
    {
        this.email = email;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("supplierUserId", getSupplierUserId())
            .append("supplierId", getSupplierId())
            .append("userId", getUserId())
            .append("isMain", getIsMain())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}

