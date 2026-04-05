package com.scm.system.domain;

import java.math.BigDecimal;
import com.scm.common.core.domain.BaseEntity;

/**
 * 中设第三方推送订单主表 zs_tp_order（查询/选择用）
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
    /** 接收渠道：TENANT=我方推送 ZS=中设客户推送 */
    private String receiveChannel;

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

    public String getZjly()
    {
        return zjly;
    }

    public void setZjly(String zjly)
    {
        this.zjly = zjly;
    }
}
