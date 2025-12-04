package com.scm.system.mapper;

import java.util.List;
import java.util.Map;
import com.scm.system.domain.PurchaseStatistics;

/**
 * 采购统计 数据层
 * 
 * @author scm
 */
public interface PurchaseStatisticsMapper
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
     * 新增采购统计信息
     * 
     * @param purchaseStatistics 统计信息
     * @return 结果
     */
    public int insertPurchaseStatistics(PurchaseStatistics purchaseStatistics);

    /**
     * 修改采购统计信息
     * 
     * @param purchaseStatistics 统计信息
     * @return 结果
     */
    public int updatePurchaseStatistics(PurchaseStatistics purchaseStatistics);

    /**
     * 删除采购统计信息
     * 
     * @param statisticsId 统计主键
     * @return 结果
     */
    public int deletePurchaseStatisticsById(Long statisticsId);

    /**
     * 批量删除采购统计信息
     * 
     * @param statisticsIds 需要删除的数据ID
     * @return 结果
     */
    public int deletePurchaseStatisticsByIds(String[] statisticsIds);
}

