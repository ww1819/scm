package com.scm.system.mapper;

import java.util.List;
import com.scm.system.domain.Delivery;
import com.scm.system.domain.vo.OrderDetailDeliveryTraceVo;
import com.scm.system.domain.vo.OrderLineDeliveryQtyVo;

/**
 * 订单与配送单相互回查
 */
public interface OrderDeliveryTraceMapper
{
    List<OrderLineDeliveryQtyVo> selectScmOrderLineDeliveryQtyByOrderId(Long orderId);

    List<OrderLineDeliveryQtyVo> selectZsOrderLineDeliveryQtyByZsOrderId(String zsOrderId);

    List<Delivery> selectDeliveriesByOrderId(Long orderId);

    List<Delivery> selectDeliveriesByZsOrderId(String zsOrderId);

    List<OrderDetailDeliveryTraceVo> selectTracesByScmOrderDetailId(Long orderDetailId);

    List<OrderDetailDeliveryTraceVo> selectTracesByZsOrderDetailId(String zsOrderDetailId);
}
