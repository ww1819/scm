package com.scm.system.mapper;

import java.util.List;
import com.scm.system.domain.ScmOrderDetailDeliveryRel;

/**
 * 我方订单明细与配送单明细关联
 */
public interface ScmOrderDetailDeliveryRelMapper
{
    int batchInsert(List<ScmOrderDetailDeliveryRel> list);

    int deleteByDeliveryId(String deliveryId);
}
