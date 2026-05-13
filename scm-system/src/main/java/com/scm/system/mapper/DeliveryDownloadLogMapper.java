package com.scm.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.scm.system.domain.DeliveryDownloadLog;

/**
 * 配送单接口下载记录
 */
public interface DeliveryDownloadLogMapper
{
    int countByDeliveryId(@Param("deliveryId") String deliveryId);

    List<DeliveryDownloadLog> selectByDeliveryId(@Param("deliveryId") String deliveryId);
}
