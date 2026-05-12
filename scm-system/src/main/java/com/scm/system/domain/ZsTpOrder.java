package com.scm.system.domain;

import java.math.BigDecimal;
import com.scm.common.core.domain.BaseEntity;

/**
 * 第三方推送订单主表 zs_tp_order（查询/选择用）
 */
public class ZsTpOrder extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private String id;
    private String thirdPartyPk;
    private String customer;
    private BigDecimal sheetJe;
    private String dh;
    private String supno;
    private String sup;
    private String ckno;
    private String ck;
    private String ksbh;
    private String ksmc;
    private String bz;
    /** 结算方式 JSFS */
    private String jsfs;
    /** 接收渠道：TENANT=我方推送 ZS=第三方客户推送 */
    private String receiveChannel;

    /** 接口 SCMSUPCODE：SCM 平台供应商编码（与第三方 supno 区分） */
    private String scmSupCode;

    /** 入参 NEWCUSTOMER：SCM 医院编码（scm_hospital.hospital_code） */
    private String scmHospitalCode;

    /** 由 scm_hospital_code 解析的 hospital_id（varchar 存数字） */
    private String scmHospitalId;

    /** 由 scm_sup_code 解析的 supplier_id（varchar 存数字） */
    private String scmSupplierId;

    /** 平台医院主键 zs_tp_order.hospital_id（与 scm_hospital 一致） */
    private Long hospitalId;

    /** 平台供应商主键 zs_tp_order.supplier_id（与 scm_supplier 一致） */
    private Long supplierId;

    /** 展示用：医院编码（档案优先，否则入参 scm_hospital_code） */
    private String hospitalCode;

    /** 展示用：医院名称 */
    private String hospitalName;

    /** 展示用：供应商编码（档案优先，否则 scm_sup_code） */
    private String supplierCode;

    /** 展示用：供应商名称 */
    private String supplierName;

    /** 列表查询：医院编码/名称/首拼简码合一关键字 */
    private String hospitalKeyword;

    /** 列表查询：供应商编码/名称/首拼简码合一关键字 */
    private String supplierKeyword;

    /** 资金来源 ZJLY */
    private String zjly;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getThirdPartyPk()
    {
        return thirdPartyPk;
    }

    public void setThirdPartyPk(String thirdPartyPk)
    {
        this.thirdPartyPk = thirdPartyPk;
    }

    public String getCustomer()
    {
        return customer;
    }

    public void setCustomer(String customer)
    {
        this.customer = customer;
    }

    public BigDecimal getSheetJe()
    {
        return sheetJe;
    }

    public void setSheetJe(BigDecimal sheetJe)
    {
        this.sheetJe = sheetJe;
    }

    public String getDh()
    {
        return dh;
    }

    public void setDh(String dh)
    {
        this.dh = dh;
    }

    public String getSupno()
    {
        return supno;
    }

    public void setSupno(String supno)
    {
        this.supno = supno;
    }

    public String getSup()
    {
        return sup;
    }

    public void setSup(String sup)
    {
        this.sup = sup;
    }

    public String getCkno()
    {
        return ckno;
    }

    public void setCkno(String ckno)
    {
        this.ckno = ckno;
    }

    public String getCk()
    {
        return ck;
    }

    public void setCk(String ck)
    {
        this.ck = ck;
    }

    public String getKsbh()
    {
        return ksbh;
    }

    public void setKsbh(String ksbh)
    {
        this.ksbh = ksbh;
    }

    public String getKsmc()
    {
        return ksmc;
    }

    public void setKsmc(String ksmc)
    {
        this.ksmc = ksmc;
    }

    public String getBz()
    {
        return bz;
    }

    public void setBz(String bz)
    {
        this.bz = bz;
    }

    public String getJsfs()
    {
        return jsfs;
    }

    public void setJsfs(String jsfs)
    {
        this.jsfs = jsfs;
    }

    public String getReceiveChannel()
    {
        return receiveChannel;
    }

    public void setReceiveChannel(String receiveChannel)
    {
        this.receiveChannel = receiveChannel;
    }

    public String getScmSupCode()
    {
        return scmSupCode;
    }

    public void setScmSupCode(String scmSupCode)
    {
        this.scmSupCode = scmSupCode;
    }

    public String getScmHospitalCode()
    {
        return scmHospitalCode;
    }

    public void setScmHospitalCode(String scmHospitalCode)
    {
        this.scmHospitalCode = scmHospitalCode;
    }

    public String getScmHospitalId()
    {
        return scmHospitalId;
    }

    public void setScmHospitalId(String scmHospitalId)
    {
        this.scmHospitalId = scmHospitalId;
    }

    public String getScmSupplierId()
    {
        return scmSupplierId;
    }

    public void setScmSupplierId(String scmSupplierId)
    {
        this.scmSupplierId = scmSupplierId;
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

    public String getHospitalCode()
    {
        return hospitalCode;
    }

    public void setHospitalCode(String hospitalCode)
    {
        this.hospitalCode = hospitalCode;
    }

    public String getHospitalName()
    {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName)
    {
        this.hospitalName = hospitalName;
    }

    public String getSupplierCode()
    {
        return supplierCode;
    }

    public void setSupplierCode(String supplierCode)
    {
        this.supplierCode = supplierCode;
    }

    public String getSupplierName()
    {
        return supplierName;
    }

    public void setSupplierName(String supplierName)
    {
        this.supplierName = supplierName;
    }

    public String getHospitalKeyword()
    {
        return hospitalKeyword;
    }

    public void setHospitalKeyword(String hospitalKeyword)
    {
        this.hospitalKeyword = hospitalKeyword;
    }

    public String getSupplierKeyword()
    {
        return supplierKeyword;
    }

    public void setSupplierKeyword(String supplierKeyword)
    {
        this.supplierKeyword = supplierKeyword;
    }

    public String getZjly()
    {
        return zjly;
    }

    public void setZjly(String zjly)
    {
        this.zjly = zjly;
    }
}
