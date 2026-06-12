package com.scm.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.scm.system.domain.ScmOrderDetailDeliveryRel;
import com.scm.system.domain.ZsTpOrderDetailDeliveryRel;

/**
 * 订单与配送单关联修复（第三方 + 第一方）
 */
public interface ZsTpOrderDeliveryRepairMapper
{
    /**
     * 从配送明细 zs_order_detail_id 回填配送主表 zs_order_id / order_no / ref_order_source
     */
    int backfillDeliveryHeaderFromDetails(@Param("operator") String operator);

    /**
     * 从配送明细 order_detail_id 回填配送主表 order_id / hospital_id / supplier_id 等
     */
    int backfillScmDeliveryHeaderFromDetails(@Param("operator") String operator);

    /**
     * 待补写的关联表行（配送明细已挂第三方行，但 rel 缺失）
     */
    List<ZsTpOrderDetailDeliveryRel> selectMissingDeliveryRelRows();

    List<ScmOrderDetailDeliveryRel> selectMissingScmDeliveryRelRows();

    /**
     * 已配送进度但无任何有效配送明细的第三方订单（异常样本，最多 limit 条）
     */
    List<String> selectDoneOrdersWithoutDeliverySample(@Param("limit") int limit);

    int countDoneOrdersWithoutDelivery();
}
