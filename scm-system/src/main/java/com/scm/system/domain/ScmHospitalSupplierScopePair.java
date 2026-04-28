package com.scm.system.domain;

import java.io.Serializable;

/**
 * 医院-供应商菜单数据范围联合键（与 scm_order / scm_delivery 等维度一致）
 */
public class ScmHospitalSupplierScopePair implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long hospitalId;
    private Long supplierId;

    public ScmHospitalSupplierScopePair()
    {
    }

    public ScmHospitalSupplierScopePair(Long hospitalId, Long supplierId)
    {
        this.hospitalId = hospitalId;
        this.supplierId = supplierId;
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
}
