package com.scm.system.domain;

import java.util.Date;
import com.scm.common.core.domain.BaseEntity;

/**
 * 医院关联申请时供应商资质证照 JSON 快照 scm_supplier_cert_apply_bundle
 */
public class ScmSupplierCertApplyBundle extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private String id;
    private String applyId;
    private String hospitalId;
    private String supplierId;
    private String certBundleJson;
    private Date createTime;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getApplyId()
    {
        return applyId;
    }

    public void setApplyId(String applyId)
    {
        this.applyId = applyId;
    }

    public String getHospitalId()
    {
        return hospitalId;
    }

    public void setHospitalId(String hospitalId)
    {
        this.hospitalId = hospitalId;
    }

    public String getSupplierId()
    {
        return supplierId;
    }

    public void setSupplierId(String supplierId)
    {
        this.supplierId = supplierId;
    }

    public String getCertBundleJson()
    {
        return certBundleJson;
    }

    public void setCertBundleJson(String certBundleJson)
    {
        this.certBundleJson = certBundleJson;
    }

    @Override
    public Date getCreateTime()
    {
        return createTime;
    }

    @Override
    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }
}
