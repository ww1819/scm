package com.scm.system.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.scm.system.domain.vo.DashboardScopeQuery;

/**
 * 首页仪表盘统计
 */
public interface DashboardMapper
{
    Long countDelivery(DashboardScopeQuery scope);

    Long countOrder(DashboardScopeQuery scope);

    Long countSettlement(DashboardScopeQuery scope);

    BigDecimal sumSettlementAmountCurrentMonth(DashboardScopeQuery scope);

    List<Map<String, Object>> selectMonthlyOrderTrend(DashboardScopeQuery scope);

    List<Map<String, Object>> selectTopSuppliersByAmount(DashboardScopeQuery scope);

    List<Map<String, Object>> selectCategoryAmount(DashboardScopeQuery scope);
}
