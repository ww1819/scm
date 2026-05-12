package com.scm.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.scm.system.domain.Delivery;
import com.scm.system.domain.vo.OrderDetailDeliveryTraceVo;
import com.scm.system.domain.vo.OrderLineDeliveryQtyVo;

/**
 * 订单与配送单相互回查
 */
public interface OrderDeliveryTraceMapper
{
    List<OrderLineDeliveryQtyVo> selectScmOrderLineDeliveryQtyByOrderId(Long orderId);

    /**
     * 汇总本单各明细行配送占用，排除指定配送单（用于修改未审核配送单时重算可配上限）
     */
    List<OrderLineDeliveryQtyVo> selectScmOrderLineDeliveryQtyByOrderIdExcludeDelivery(@Param("orderId") Long orderId,
        @Param("excludeDeliveryId") Long excludeDeliveryId);

    List<OrderLineDeliveryQtyVo> selectZsOrderLineDeliveryQtyByZsOrderId(String zsOrderId);

    List<OrderLineDeliveryQtyVo> selectZsOrderLineDeliveryQtyByZsOrderIdExcludeDelivery(@Param("zsOrderId") String zsOrderId,
        @Param("excludeDeliveryId") Long excludeDeliveryId);

    List<Delivery> selectDeliveriesByOrderId(Long orderId);

    List<Delivery> selectDeliveriesByZsOrderId(String zsOrderId);

    List<OrderDetailDeliveryTraceVo> selectTracesByScmOrderDetailId(Long orderDetailId);

    List<OrderDetailDeliveryTraceVo> selectTracesByZsOrderDetailId(String zsOrderDetailId);
}
