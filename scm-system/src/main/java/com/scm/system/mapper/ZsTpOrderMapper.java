package com.scm.system.mapper;

import java.util.List;
import com.scm.system.domain.ZsTpOrder;
import com.scm.system.domain.ZsTpOrderDetail;

/**
 * 中设订单 zs_tp_order / zs_tp_order_detail
 */
public interface ZsTpOrderMapper
{
    /**
     * 分页列表（未删除）
     */
    List<ZsTpOrder> selectZsTpOrderList(ZsTpOrder query);

    /**
     * 主键查询主表
     */
    ZsTpOrder selectZsTpOrderById(String id);

    /**
     * 按主表 ID 查明细（未删除）
     */
    List<ZsTpOrderDetail> selectZsTpOrderDetailListByOrderId(String orderId);
}
