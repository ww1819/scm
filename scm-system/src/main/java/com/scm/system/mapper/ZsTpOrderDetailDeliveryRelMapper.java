package com.scm.system.mapper;

import java.util.List;
import com.scm.system.domain.ZsTpOrderDetailDeliveryRel;

/**
 * 中设订单明细与配送单明细关联
 */
public interface ZsTpOrderDetailDeliveryRelMapper
{
    int batchInsert(List<ZsTpOrderDetailDeliveryRel> list);

    int deleteByDeliveryId(String deliveryId);
}
