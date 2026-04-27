package com.scm.system.domain;

import com.scm.common.core.domain.BaseEntity;

/**
 * 医院-供应商数据权限黑名单 scm_hospital_supplier_permission
 */
public class ScmHospitalSupplierPermission extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long hospitalId;
    private Long supplierId;
    private String forbidSubmitFlag;
    private String forbidBindFlag;
    private String delFlag;
    /** 列表展示 */
    private String hospitalName;
    private String supplierCompanyName;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getHospitalId()
    {
        return hospitalId;
    }

    public void setHospitalId(Long hospitalId)
    {
        this.hospitalId = hospitalId;
    }

    public Long getSupplierId()
    {
        return supplierId;
    }

    public void setSupplierId(Long supplierId)
    {
        this.supplierId = supplierId;
    }

    public String getForbidSubmitFlag()
    {
        return forbidSubmitFlag;
    }

    public void setForbidSubmitFlag(String forbidSubmitFlag)
    {
        this.forbidSubmitFlag = forbidSubmitFlag;
    }

    public String getForbidBindFlag()
    {
        return forbidBindFlag;
    }

    public void setForbidBindFlag(String forbidBindFlag)
    {
        this.forbidBindFlag = forbidBindFlag;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public String getHospitalName()
    {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName)
    {
        this.hospitalName = hospitalName;
    }

    public String getSupplierCompanyName()
    {
        return supplierCompanyName;
    }

    public void setSupplierCompanyName(String supplierCompanyName)
    {
        this.supplierCompanyName = supplierCompanyName;
    }
}
