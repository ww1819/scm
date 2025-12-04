package com.scm.system.service;

import java.util.List;
import java.util.Map;
import com.scm.system.domain.PurchaseStatistics;

/**
 * 数据中心 服务层
 * 
 * @author scm
 */
public interface IDataCenterService
{
    /**
     * 查询采购统计信息
     * 
     * @param statisticsId 统计ID
     * @return 统计信息
     */
    public PurchaseStatistics selectPurchaseStatisticsById(Long statisticsId);

    /**
     * 查询采购统计列表
     * 
     * @param purchaseStatistics 统计信息
     * @return 统计集合
     */
    public List<PurchaseStatistics> selectPurchaseStatisticsList(PurchaseStatistics purchaseStatistics);

    /**
     * 查询月采购量统计
     * 
     * @param params 查询参数（年份、月份、医院ID、供应商ID）
     * @return 统计集合
     */
    public List<Map<String, Object>> selectMonthlyPurchaseStatistics(Map<String, Object> params);

    /**
     * 查询年采购量统计
     * 
     * @param params 查询参数（年份、医院ID、供应商ID）
     * @return 统计集合
     */
    public List<Map<String, Object>> selectYearlyPurchaseStatistics(Map<String, Object> params);

    /**
     * 查询采购数据分析报表
     * 
     * @param params 查询参数
     * @return 统计集合
     */
    public List<Map<String, Object>> selectPurchaseAnalysisReport(Map<String, Object> params);

    /**
     * 生成采购统计数据
     * 
     * @param year 年份
     * @param month 月份（可选，如果为空则生成整年数据）
     * @return 结果
     */
    public int generatePurchaseStatistics(Integer year, Integer month);
}

