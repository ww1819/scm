package com.scm.system.service;

import java.util.List;
import java.util.Map;
import com.scm.system.domain.ReconciliationYearMonthRow;

/**
 * 对账表 服务层
 */
public interface IScmReconciliationService
{
    /**
     * 按医院、年份、可选供应商返回 12 个月配送/结算汇总
     */
    List<ReconciliationYearMonthRow> selectYearSummary(Long hospitalId, int year, Long supplierId);

    /**
     * 对账表供应商下拉（可按医院过滤）
     */
    List<Map<String, Object>> selectSupplierOptions(Long hospitalId, Long bindSupplierId);
}