package com.scm.system.domain;

import java.math.BigDecimal;
import com.scm.common.core.domain.BaseEntity;

/**
 * 中设第三方推送订单明细 zs_tp_order_detail
 */
public class ZsTpOrderDetail extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private String id;
    private String orderId;
    private String thirdPartyPk;
    private String dh;
    private String code;
    private String name;
    private String gg;
    private String dw;
    private String bzl;
    private String sccj;
    private String zcz;
    private String phflag;
    private BigDecimal sl;
    private BigDecimal dj;
    private BigDecimal je;
    private String jm;
    private String cgj;
    private String bz;
    private String bz1;
    private String bz2;
    private BigDecimal dsb;

    /** 已审核配送数量 */
    private BigDecimal deliveredAuditedQty;

    /** 待审核配送数量 */
    private BigDecimal deliveredPendingAuditQty;

    /** 未配送数量 */
    private BigDecimal undeliveredQty;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getOrderId()
    {
        return orderId;
    }

    public void setOrderId(String orderId)
    {
        this.orderId = orderId;
    }

    public String getThirdPartyPk()
    {
        return thirdPartyPk;
    }

    public void setThirdPartyPk(String thirdPartyPk)
    {
        this.thirdPartyPk = thirdPartyPk;
    }

    public String getDh()
    {
        return dh;
    }

    public void setDh(String dh)
    {
        this.dh = dh;
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getGg()
    {
        return gg;
    }

    public void setGg(String gg)
    {
        this.gg = gg;
    }

    public String getDw()
    {
        return dw;
    }

    public void setDw(String dw)
    {
        this.dw = dw;
    }

    public String getBzl()
    {
        return bzl;
    }

    public void setBzl(String bzl)
    {
        this.bzl = bzl;
    }

    public String getSccj()
    {
        return sccj;
    }

    public void setSccj(String sccj)
    {
        this.sccj = sccj;
    }

    public String getZcz()
    {
        return zcz;
    }

    public void setZcz(String zcz)
    {
        this.zcz = zcz;
    }

    public String getPhflag()
    {
        return phflag;
    }

    public void setPhflag(String phflag)
    {
        this.phflag = phflag;
    }

    public BigDecimal getSl()
    {
        return sl;
    }

    public void setSl(BigDecimal sl)
    {
        this.sl = sl;
    }

    public BigDecimal getDj()
    {
        return dj;
    }

    public void setDj(BigDecimal dj)
    {
        this.dj = dj;
    }

    public BigDecimal getJe()
    {
        return je;
    }

    public void setJe(BigDecimal je)
    {
        this.je = je;
    }

    public String getJm()
    {
        return jm;
    }

    public void setJm(String jm)
    {
        this.jm = jm;
    }

    public String getCgj()
    {
        return cgj;
    }

    public void setCgj(String cgj)
    {
        this.cgj = cgj;
    }

    public String getBz()
    {
        return bz;
    }

    public void setBz(String bz)
    {
        this.bz = bz;
    }

    public String getBz1()
    {
        return bz1;
    }

    public void setBz1(String bz1)
    {
        this.bz1 = bz1;
    }

    public String getBz2()
    {
        return bz2;
    }

    public void setBz2(String bz2)
    {
        this.bz2 = bz2;
    }

    public BigDecimal getDsb()
    {
        return dsb;
    }

    public void setDsb(BigDecimal dsb)
    {
        this.dsb = dsb;
    }

    public BigDecimal getDeliveredAuditedQty()
    {
        return deliveredAuditedQty;
    }

    public void setDeliveredAuditedQty(BigDecimal deliveredAuditedQty)
    {
        this.deliveredAuditedQty = deliveredAuditedQty;
    }

    public BigDecimal getDeliveredPendingAuditQty()
    {
        return deliveredPendingAuditQty;
    }

    public void setDeliveredPendingAuditQty(BigDecimal deliveredPendingAuditQty)
    {
        this.deliveredPendingAuditQty = deliveredPendingAuditQty;
    }

    public BigDecimal getUndeliveredQty()
    {
        return undeliveredQty;
    }

    public void setUndeliveredQty(BigDecimal undeliveredQty)
    {
        this.undeliveredQty = undeliveredQty;
    }
}
