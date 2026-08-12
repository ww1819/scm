package com.scm.system.mapper;

import java.math.BigDecimal;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.scm.system.domain.CombinedDelivery;
import com.scm.system.domain.CombinedDeliveryDetail;
import com.scm.system.domain.vo.CombinedOrderProgressVo;
import com.scm.system.domain.vo.OrderLineDeliveryQtyVo;

public interface CombinedDeliveryMapper
{
    CombinedDelivery selectCombinedDeliveryById(String combinedId);

    CombinedDelivery selectCombinedDeliveryByNo(String combinedNo);

    List<CombinedDelivery> selectCombinedDeliveryList(CombinedDelivery query);

    int insertCombinedDelivery(CombinedDelivery row);

    int updateCombinedDelivery(CombinedDelivery row);

    int deleteCombinedDeliveryById(@Param("combinedId") String combinedId, @Param("delBy") String delBy);

    int deleteCombinedDeliveryByIds(@Param("ids") String[] ids, @Param("delBy") String delBy);

    CombinedDeliveryDetail selectDetailById(String detailId);

    List<CombinedDeliveryDetail> selectDetailsByCombinedId(String combinedId);

    CombinedDeliveryDetail selectDetailByDeliveryDetailId(Long deliveryDetailId);

    int insertCombinedDeliveryDetail(CombinedDeliveryDetail row);

    int batchInsertCombinedDeliveryDetail(@Param("list") List<CombinedDeliveryDetail> list);

    int updateCombinedDeliveryDetail(CombinedDeliveryDetail row);

    int deleteDetailById(@Param("detailId") String detailId, @Param("delBy") String delBy);

    int deleteDetailsByCombinedId(@Param("combinedId") String combinedId, @Param("delBy") String delBy);

    int deleteDetailsByDeliveryId(@Param("deliveryId") Long deliveryId, @Param("delBy") String delBy);

    int countCombinedDetailByOrderId(@Param("orderId") Long orderId, @Param("excludeCombinedId") String excludeCombinedId);

    List<OrderLineDeliveryQtyVo> selectCombinedPendingQtyByOrderId(@Param("orderId") Long orderId,
            @Param("excludeCombinedId") String excludeCombinedId);

    BigDecimal selectCombinedPendingQtyByOrderDetailId(@Param("orderDetailId") Long orderDetailId,
            @Param("excludeCombinedId") String excludeCombinedId);

    List<CombinedOrderProgressVo> selectCombinedOrderProgress(@Param("supplierId") Long supplierId,
            @Param("hospitalId") Long hospitalId);

    List<CombinedDeliveryDetail> selectCandidateOrderLines(CombinedDelivery query);
}
