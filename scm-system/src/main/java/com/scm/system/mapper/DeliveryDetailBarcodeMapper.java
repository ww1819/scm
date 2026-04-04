package com.scm.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.scm.system.domain.DeliveryDetailBarcode;

/**
 * 配送单明细条码从表
 */
public interface DeliveryDetailBarcodeMapper
{
    int batchInsert(List<DeliveryDetailBarcode> list);

    int deleteByDeliveryId(@Param("deliveryId") Long deliveryId);

    List<DeliveryDetailBarcode> selectListByDeliveryId(@Param("deliveryId") Long deliveryId);
}
