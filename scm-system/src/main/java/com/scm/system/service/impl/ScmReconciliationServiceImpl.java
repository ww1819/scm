package com.scm.system.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.scm.common.utils.StringUtils;
import com.scm.system.domain.ReconciliationSupplierOption;
import com.scm.system.domain.ReconciliationYearMonthRow;
import com.scm.system.domain.Supplier;
import com.scm.system.mapper.DeliveryMapper;
import com.scm.system.mapper.SettlementMapper;
import com.scm.system.service.IScmReconciliationService;
import com.scm.system.service.ISupplierService;
/**
 * 对账表 服务层实现
 */
@Service
public class ScmReconciliationServiceImpl implements IScmReconciliationService
{
    private static final String[] MONTH_LABELS = {
        "一月", "二月", "三月", "四月", "五月", "六月",
        "七月", "八月", "九月", "十月", "十一月", "十二月"
    };

    @Autowired
    private DeliveryMapper deliveryMapper;

    @Autowired
    private SettlementMapper settlementMapper;

    @Autowired
    private ISupplierService supplierService;

    @Override
    public List<ReconciliationYearMonthRow> selectYearSummary(Long hospitalId, int year, Long supplierId)
    {
        String yearBegin = year + "-01-01";
        String yearEnd = year + "-12-31";
        Map<Integer, MonthAggregate> deliveryByMonth = toMonthAggregateMap(
            deliveryMapper.sumDeliveryAmountGroupByMonth(hospitalId, supplierId, yearBegin, yearEnd));
        Map<Integer, MonthAggregate> settlementByMonth = toMonthAggregateMap(
            settlementMapper.sumSettlementAmountGroupByMonth(hospitalId, supplierId, yearBegin, yearEnd));

        List<ReconciliationYearMonthRow> rows = new ArrayList<>(12);
        for (int m = 1; m <= 12; m++)
        {
            MonthAggregate delivery = deliveryByMonth.getOrDefault(m, MonthAggregate.ZERO);
            MonthAggregate settlement = settlementByMonth.getOrDefault(m, MonthAggregate.ZERO);
            ReconciliationYearMonthRow row = new ReconciliationYearMonthRow();
            row.setMonth(m);
            row.setMonthLabel(MONTH_LABELS[m - 1]);
            row.setDeliveryAmount(delivery.amount);
            row.setDeliveryQuantity(delivery.quantity);
            row.setSettlementAmount(settlement.amount);
            row.setSettlementQuantity(settlement.quantity);
            rows.add(row);
        }
        return rows;
    }

    @Override
    public List<Map<String, Object>> selectSupplierOptions(Long hospitalId, Long bindSupplierId)
    {
        if (bindSupplierId != null)
        {
            Supplier bound = supplierService.selectSupplierById(bindSupplierId);
            List<Map<String, Object>> options = new ArrayList<>();
            if (bound != null && bound.getSupplierId() != null)
            {
                options.add(toSupplierOption(bound));
            }
            return options;
        }
        if (hospitalId == null)
        {
            Supplier query = new Supplier();
            query.setStatus("0");
            List<Map<String, Object>> options = new ArrayList<>();
            for (Supplier s : supplierService.selectSupplierList(query))
            {
                if (s != null && s.getSupplierId() != null)
                {
                    options.add(toSupplierOption(s));
                }
            }
            return options;
        }
        List<ReconciliationSupplierOption> rows = deliveryMapper.selectReconciliationSuppliersByHospital(hospitalId);
        if (rows == null || rows.isEmpty())
        {
            return new ArrayList<>();
        }
        List<Map<String, Object>> options = new ArrayList<>();
        for (ReconciliationSupplierOption row : rows)
        {
            if (row == null || row.getSupplierId() == null)
            {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("supplierId", row.getSupplierId());
            item.put("supplierName", StringUtils.isNotEmpty(row.getSupplierName()) ? row.getSupplierName() : "-");
            item.put("supplierCode", row.getSupplierCode());
            item.put("pinyinCode", row.getPinyinCode());
            options.add(item);
        }
        return options;
    }

    private static Map<String, Object> toSupplierOption(Supplier s)
    {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("supplierId", s.getSupplierId());
        row.put("supplierName", StringUtils.isNotEmpty(s.getCompanyName()) ? s.getCompanyName() : "-");
        row.put("supplierCode", s.getSupplierCode());
        row.put("pinyinCode", s.getPinyinCode());
        return row;
    }

    private static Map<Integer, MonthAggregate> toMonthAggregateMap(List<Map<String, Object>> list)
    {
        Map<Integer, MonthAggregate> map = new HashMap<>();
        if (list == null)
        {
            return map;
        }
        for (Map<String, Object> item : list)
        {
            if (item == null)
            {
                continue;
            }
            Object monthObj = item.get("monthNum");
            if (monthObj == null)
            {
                continue;
            }
            int month = Integer.parseInt(String.valueOf(monthObj));
            map.put(month, new MonthAggregate(
                toBigDecimal(item.get("totalAmount")),
                toBigDecimal(item.get("totalQuantity"))));
        }
        return map;
    }

    private static BigDecimal toBigDecimal(Object value)
    {
        if (value == null)
        {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal)
        {
            return (BigDecimal) value;
        }
        return new BigDecimal(String.valueOf(value));
    }

    private static final class MonthAggregate
    {
        private static final MonthAggregate ZERO = new MonthAggregate(BigDecimal.ZERO, BigDecimal.ZERO);

        private final BigDecimal amount;
        private final BigDecimal quantity;

        private MonthAggregate(BigDecimal amount, BigDecimal quantity)
        {
            this.amount = amount != null ? amount : BigDecimal.ZERO;
            this.quantity = quantity != null ? quantity : BigDecimal.ZERO;
        }
    }
}
