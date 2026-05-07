package com.scm.common.constant;

/**
 * 配送单关联订单来源（scm_delivery.ref_order_source）
 */
public final class DeliveryRefOrderSource
{
    /** 本系统第一方订单 scm_order */
    public static final String SCM = "SCM";

    /** 第三方推送订单 zs_tp_order */
    public static final String ZS = "ZS";

    private DeliveryRefOrderSource()
    {
    }
}
