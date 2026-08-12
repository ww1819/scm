package com.scm.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.scm.system.domain.DeliveryLineOpLog;

public interface DeliveryLineOpLogMapper
{
    int insertDeliveryLineOpLog(DeliveryLineOpLog log);

    List<DeliveryLineOpLog> selectByBill(@Param("billKind") String billKind, @Param("billId") String billId);
}
