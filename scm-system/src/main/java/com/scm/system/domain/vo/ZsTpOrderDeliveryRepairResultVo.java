package com.scm.system.domain.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * 第三方订单配送关联修复结果
 */
public class ZsTpOrderDeliveryRepairResultVo
{
    /** 回填第三方订单关联的配送单数 */
    private int deliveryHeaderBackfilled;

    /** 补写 zs_tp_order_detail_delivery_rel 的行数 */
    private int deliveryRelInserted;

    /** 回填第一方订单关联的配送单数 */
    private int scmDeliveryHeaderBackfilled;

    /** 补写 scm_order_detail_delivery_rel 的行数 */
    private int scmDeliveryRelInserted;

    /** 显示已配送但查不到任何有效配送明细的订单数（异常，仅报告） */
    private int anomalyDoneWithoutDelivery;

    private List<String> anomalySamples = new ArrayList<>();

    public int getDeliveryHeaderBackfilled()
    {
        return deliveryHeaderBackfilled;
    }

    public void setDeliveryHeaderBackfilled(int deliveryHeaderBackfilled)
    {
        this.deliveryHeaderBackfilled = deliveryHeaderBackfilled;
    }

    public int getDeliveryRelInserted()
    {
        return deliveryRelInserted;
    }

    public void setDeliveryRelInserted(int deliveryRelInserted)
    {
        this.deliveryRelInserted = deliveryRelInserted;
    }

    public int getScmDeliveryHeaderBackfilled()
    {
        return scmDeliveryHeaderBackfilled;
    }

    public void setScmDeliveryHeaderBackfilled(int scmDeliveryHeaderBackfilled)
    {
        this.scmDeliveryHeaderBackfilled = scmDeliveryHeaderBackfilled;
    }

    public int getScmDeliveryRelInserted()
    {
        return scmDeliveryRelInserted;
    }

    public void setScmDeliveryRelInserted(int scmDeliveryRelInserted)
    {
        this.scmDeliveryRelInserted = scmDeliveryRelInserted;
    }

    public int getAnomalyDoneWithoutDelivery()
    {
        return anomalyDoneWithoutDelivery;
    }

    public void setAnomalyDoneWithoutDelivery(int anomalyDoneWithoutDelivery)
    {
        this.anomalyDoneWithoutDelivery = anomalyDoneWithoutDelivery;
    }

    public List<String> getAnomalySamples()
    {
        return anomalySamples;
    }

    public void setAnomalySamples(List<String> anomalySamples)
    {
        this.anomalySamples = anomalySamples;
    }
}
