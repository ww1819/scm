package com.scm.system.service;

import java.util.List;
import com.scm.system.domain.CombinedDeliveryDetail;
import com.scm.system.domain.Delivery;
import com.scm.system.domain.DeliveryDetail;
import com.scm.system.domain.DeliveryLineOpLog;

public interface IDeliveryLineOpLogService
{
    void logDeliveryLine(String action, Delivery bill, DeliveryDetail before, DeliveryDetail after, String operName);

    void logCombinedLine(String action, String combinedId, String combinedNo, CombinedDeliveryDetail before,
            CombinedDeliveryDetail after, Long relatedDeliveryId, String relatedDeliveryNo, String operName);

    void logDeliveryPrint(Delivery bill, List<DeliveryDetail> details, String operName);

    List<DeliveryLineOpLog> selectByBill(String billKind, String billId);
}
