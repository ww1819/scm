package com.scm.system.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.scm.common.core.domain.entity.SysRole;
import com.scm.common.utils.PageUtils;
import com.scm.system.domain.vo.DashboardScopeQuery;
import com.scm.system.domain.vo.DashboardStatsVO;
import com.scm.system.mapper.DashboardMapper;
import com.scm.system.mapper.SysRoleMapper;
import com.scm.system.service.IDashboardService;
import com.scm.system.service.IScmHospitalContextService;
import com.scm.system.service.IScmHospitalSupplierMenuScopeService;
import com.scm.system.service.IScmHospitalSupplierPermissionService;
import com.scm.system.service.IScmSupplierContextService;

/**
 * 首页仪表盘统计
 */
@Service
public class DashboardServiceImpl implements IDashboardService
{
    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private DashboardMapper dashboardMapper;

    @Autowired
    private IScmHospitalContextService scmHospitalContextService;

    @Autowired
    private IScmSupplierContextService scmSupplierContextService;

    @Autowired
    private IScmHospitalSupplierPermissionService hospitalSupplierPermissionService;

    @Autowired
    private IScmHospitalSupplierMenuScopeService scmHospitalSupplierMenuScopeService;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Override
    public DashboardStatsVO getDashboardStats(Long userId)
    {
        DashboardScopeQuery scope = buildScopeQuery(userId);
        DashboardStatsVO vo = new DashboardStatsVO();
        if (scope.isScopeBlocked())
        {
            fillEmptyTrend(vo);
            return vo;
        }

        Long deliveryCount = dashboardMapper.countDelivery(scope);
        Long orderCount = dashboardMapper.countOrder(scope);
        Long settlementCount = dashboardMapper.countSettlement(scope);
        BigDecimal monthAmount = dashboardMapper.sumSettlementAmountCurrentMonth(scope);

        vo.setDeliveryCount(deliveryCount != null ? deliveryCount : 0L);
        vo.setOrderCount(orderCount != null ? orderCount : 0L);
        vo.setSettlementCount(settlementCount != null ? settlementCount : 0L);
        vo.setMonthSettlementAmount(monthAmount != null ? monthAmount : BigDecimal.ZERO);

        fillMonthlyTrend(vo, scope, dashboardMapper.selectMonthlyOrderTrend(scope));
        fillSupplierRank(vo, dashboardMapper.selectTopSuppliersByAmount(scope));
        fillCategoryStats(vo, dashboardMapper.selectCategoryAmount(scope));
        return vo;
    }

    private DashboardScopeQuery buildScopeQuery(Long userId)
    {
        DashboardScopeQuery scope = new DashboardScopeQuery();
        Map<String, Object> params = new HashMap<>();
        scope.setParams(params);

        LocalDate today = LocalDate.now();
        YearMonth current = YearMonth.from(today);
        YearMonth trendStart = current.minusMonths(11);
        scope.setTrendStartMonth(trendStart.format(MONTH_FMT));
        scope.setMonthStart(current.atDay(1).format(DATE_FMT));
        scope.setMonthEnd(current.plusMonths(1).atDay(1).format(DATE_FMT));

        Long hospitalCtx = scmHospitalContextService.resolveHospitalIdForUser(userId);
        if (hospitalCtx != null)
        {
            scope.setHospitalId(hospitalCtx);
        }
        Long supplierCtx = scmSupplierContextService.resolveSupplierIdForUser(userId);
        if (supplierCtx == null)
        {
            scmHospitalSupplierMenuScopeService.applyMenuPairScopeToParams(params, userId);
            return scope;
        }
        List<Long> roleSupplierIds = resolveUserRoleSupplierIds(userId);
        if (!roleSupplierIds.isEmpty())
        {
            if (!roleSupplierIds.contains(supplierCtx))
            {
                params.put("scopePairBlock", Boolean.TRUE);
                return scope;
            }
            params.put("roleSupplierIds", roleSupplierIds);
        }
        scope.setSupplierId(supplierCtx);
        List<Long> forbid = hospitalSupplierPermissionService.listForbidSubmitHospitalIds(supplierCtx);
        if (forbid != null && !forbid.isEmpty())
        {
            params.put("excludeHospitalIds", forbid);
        }
        scmHospitalSupplierMenuScopeService.applyMenuPairScopeToParams(params, userId);
        return scope;
    }

    private List<Long> resolveUserRoleSupplierIds(Long userId)
    {
        if (userId == null)
        {
            return new ArrayList<>();
        }
        return PageUtils.callWithoutPaging(() -> {
            List<SysRole> roles = sysRoleMapper.selectRolesByUserId(userId);
            if (roles == null || roles.isEmpty())
            {
                return new ArrayList<Long>();
            }
            Set<Long> out = new HashSet<>();
            for (SysRole role : roles)
            {
                if (role != null && role.getSupplierId() != null)
                {
                    out.add(role.getSupplierId());
                }
            }
            return new ArrayList<>(out);
        });
    }

    private void fillEmptyTrend(DashboardStatsVO vo)
    {
        fillMonthlyTrend(vo, null, new ArrayList<>());
    }

    private void fillMonthlyTrend(DashboardStatsVO vo, DashboardScopeQuery scope, List<Map<String, Object>> rows)
    {
        List<String> labels = new ArrayList<>();
        List<BigDecimal> salesWan = new ArrayList<>();
        List<Long> orderCounts = new ArrayList<>();

        Map<String, BigDecimal> salesByMonth = new HashMap<>();
        Map<String, Long> countByMonth = new HashMap<>();
        if (rows != null)
        {
            for (Map<String, Object> row : rows)
            {
                String month = stringVal(row.get("monthKey"));
                if (month == null)
                {
                    continue;
                }
                BigDecimal sales = toBigDecimal(row.get("salesAmount"));
                Long cnt = toLong(row.get("orderCount"));
                salesByMonth.put(month, sales);
                countByMonth.put(month, cnt);
            }
        }

        YearMonth start = scope != null && scope.getTrendStartMonth() != null
            ? YearMonth.parse(scope.getTrendStartMonth(), MONTH_FMT)
            : YearMonth.now().minusMonths(11);
        for (int i = 0; i < 12; i++)
        {
            YearMonth ym = start.plusMonths(i);
            String key = ym.format(MONTH_FMT);
            labels.add(key);
            BigDecimal sales = salesByMonth.getOrDefault(key, BigDecimal.ZERO);
            salesWan.add(sales.divide(new BigDecimal("10000"), 2, RoundingMode.HALF_UP));
            orderCounts.add(countByMonth.getOrDefault(key, 0L));
        }
        vo.setMonthLabels(labels);
        vo.setMonthlySalesWan(salesWan);
        vo.setMonthlyOrderCounts(orderCounts);
    }

    private void fillSupplierRank(DashboardStatsVO vo, List<Map<String, Object>> rows)
    {
        List<String> names = new ArrayList<>();
        List<BigDecimal> amounts = new ArrayList<>();
        if (rows != null)
        {
            for (Map<String, Object> row : rows)
            {
                names.add(stringVal(row.get("supplierName")));
                BigDecimal amt = toBigDecimal(row.get("totalAmount"));
                amounts.add(amt.divide(new BigDecimal("10000"), 2, RoundingMode.HALF_UP));
            }
        }
        vo.setSupplierNames(names);
        vo.setSupplierAmounts(amounts);
    }

    private void fillCategoryStats(DashboardStatsVO vo, List<Map<String, Object>> rows)
    {
        List<String> names = new ArrayList<>();
        List<BigDecimal> amounts = new ArrayList<>();
        if (rows != null)
        {
            for (Map<String, Object> row : rows)
            {
                names.add(stringVal(row.get("categoryName")));
                BigDecimal amt = toBigDecimal(row.get("totalAmount"));
                amounts.add(amt.divide(new BigDecimal("10000"), 2, RoundingMode.HALF_UP));
            }
        }
        vo.setCategoryNames(names);
        vo.setCategoryAmounts(amounts);
    }

    private static String stringVal(Object o)
    {
        return o == null ? "" : String.valueOf(o);
    }

    private static BigDecimal toBigDecimal(Object o)
    {
        if (o == null)
        {
            return BigDecimal.ZERO;
        }
        if (o instanceof BigDecimal)
        {
            return (BigDecimal) o;
        }
        if (o instanceof Number)
        {
            return BigDecimal.valueOf(((Number) o).doubleValue());
        }
        try
        {
            return new BigDecimal(String.valueOf(o));
        }
        catch (NumberFormatException e)
        {
            return BigDecimal.ZERO;
        }
    }

    private static Long toLong(Object o)
    {
        if (o == null)
        {
            return 0L;
        }
        if (o instanceof Number)
        {
            return ((Number) o).longValue();
        }
        try
        {
            return Long.parseLong(String.valueOf(o));
        }
        catch (NumberFormatException e)
        {
            return 0L;
        }
    }
}
