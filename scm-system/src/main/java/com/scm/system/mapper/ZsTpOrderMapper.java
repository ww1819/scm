package com.scm.system.mapper;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
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

    /**
     * 按明细主键查询所属第三方订单主键（去重，用于配送单保存时反推 zs_order_id）
     */
    List<String> selectDistinctOrderIdsByDetailIds(@Param("detailIds") List<String> detailIds);

    /**
     * 供应商确认（未确认、未作废、未删除）
     */
    int updateZsTpOrderConfirm(@Param("id") String id, @Param("confirmBy") String confirmBy,
        @Param("confirmByNameSnapshot") String confirmByNameSnapshot, @Param("confirmTime") Date confirmTime);

    /**
     * 医院作废（未作废、未删除）
     */
    int updateZsTpOrderVoid(@Param("id") String id, @Param("voidBy") String voidBy,
        @Param("voidByNameSnapshot") String voidByNameSnapshot, @Param("voidTime") Date voidTime);
}
