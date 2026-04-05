package com.scm.common.constant;

import com.scm.common.utils.StringUtils;

/**
 * 中设订单结算方式 JSFS 与条码种子高低值（{@code scm_barcode_seed.high_low_flag}）对应关系。
 * <p>
 * 与接口侧 {@code ZsBarcodeSeedConstants#highLowFlagFromJsfs} 保持一致。
 */
public final class ZsJsfsHighLow
{
    /** 高值 */
    public static final String JSFS_HIGH_VALUE = "3";
    /** 低值 */
    public static final String JSFS_LOW_VALUE = "0";
    public static final String FLAG_HIGH = "H";
    public static final String FLAG_LOW = "L";

    /**
     * 中设条码种子暂不按仓库划分：{@code scm_barcode_seed.warehouse_id} 固定为空串，仅按高低值（及 T/Z 维度）区分。
     */
    public static final String ZS_SEED_WAREHOUSE_ID = "";

    private ZsJsfsHighLow()
    {
    }

    /**
     * @param jsfs 中设主表 JSFS：trim 后与 JSFS_HIGH_VALUE(3) 为高值，JSFS_LOW_VALUE(0) 为低值；其它或空默认低值
     */
    public static String highLowFlagFromJsfs(String jsfs)
    {
        String t = StringUtils.trimToEmpty(jsfs);
        if (JSFS_HIGH_VALUE.equals(t))
        {
            return FLAG_HIGH;
        }
        if (JSFS_LOW_VALUE.equals(t))
        {
            return FLAG_LOW;
        }
        return FLAG_LOW;
    }
}
