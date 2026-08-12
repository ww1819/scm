package com.scm.system.service;

import java.util.List;
import com.scm.system.domain.CombinedDelivery;
import com.scm.system.domain.CombinedDeliveryDetail;
import com.scm.system.domain.vo.CombinedOrderProgressVo;

public interface ICombinedDeliveryService
{
    CombinedDelivery selectById(String combinedId);

    List<CombinedDelivery> selectList(CombinedDelivery query);

    int insert(CombinedDelivery row);

    int update(CombinedDelivery row);

    int deleteByIds(String ids);

    int audit(String combinedId, String auditBy);

    List<CombinedDeliveryDetail> selectCandidateLines(CombinedDelivery query);

    List<CombinedOrderProgressVo> selectOrderProgress(CombinedDelivery query);

    int countCombinedOccupancy(Long orderId, String excludeCombinedId);
}
