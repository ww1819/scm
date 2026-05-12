package com.scm.system.mapper;

import java.util.List;
import com.scm.system.domain.ZsTpOrder;
import com.scm.system.domain.ZsTpOrderDetail;

/**
 * 第三方订单 zs_tp_order / zs_tp_order_detail
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
     * 主键查询主表（关联医院/供应商展示编码与名称）
     */
    ZsTpOrder selectZsTpOrderByIdJoined(String id);

    /**
     * 第三方订单查询列表（关联医院/供应商；由服务层施加医院/供应商数据范围）
     */
    List<ZsTpOrder> selectZsTpOrderQueryList(ZsTpOrder query);

    /**
     * 按主表 ID 查明细（未删除）
     */
    List<ZsTpOrderDetail> selectZsTpOrderDetailListByOrderId(String orderId);
}
