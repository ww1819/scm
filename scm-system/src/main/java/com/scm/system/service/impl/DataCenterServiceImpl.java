package com.scm.system.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.scm.common.utils.DateUtils;
import com.scm.system.domain.PurchaseStatistics;
import com.scm.system.mapper.PurchaseStatisticsMapper;
import com.scm.system.service.IDataCenterService;

/**
 * 数据中心 服务层实现
 * 
 * @author scm
 */
@Service
public class DataCenterServiceImpl implements IDataCenterService
{
    @Autowired
    private PurchaseStatisticsMapper purchaseStatisticsMapper;

    /**
     * 查询采购统计信息
     * 
     * @param statisticsId 统计ID
     * @return 统计信息
     */
    @Override
    public PurchaseStatistics selectPurchaseStatisticsById(Long statisticsId)
    {
        return purchaseStatisticsMapper.selectPurchaseStatisticsById(statisticsId);
    }

    /**
     * 查询采购统计列表
     * 
     * @param purchaseStatistics 统计信息
     * @return 统计集合
     */
    @Override
    public List<PurchaseStatistics> selectPurchaseStatisticsList(PurchaseStatistics purchaseStatistics)
    {
        return purchaseStatisticsMapper.selectPurchaseStatisticsList(purchaseStatistics);
    }

    /**
     * 查询月采购量统计
     * 
     * @param params 查询参数（年份、月份、医院ID、供应商ID）
     * @return 统计集合
     */
    @Override
    public List<Map<String, Object>> selectMonthlyPurchaseStatistics(Map<String, Object> params)
    {
        return purchaseStatisticsMapper.selectMonthlyPurchaseStatistics(params);
    }

    /**
     * 查询年采购量统计
     * 
     * @param params 查询参数（年份、医院ID、供应商ID）
     * @return 统计集合
     */
    @Override
    public List<Map<String, Object>> selectYearlyPurchaseStatistics(Map<String, Object> params)
    {
        return purchaseStatisticsMapper.selectYearlyPurchaseStatistics(params);
    }

    /**
     * 查询采购数据分析报表
     * 
     * @param params 查询参数
     * @return 统计集合
     */
    @Override
    public List<Map<String, Object>> selectPurchaseAnalysisReport(Map<String, Object> params)
    {
        return purchaseStatisticsMapper.selectPurchaseAnalysisReport(params);
    }

    /**
     * 生成采购统计数据
     * 
     * @param year 年份
     * @param month 月份（可选，如果为空则生成整年数据）
     * @return 结果
     */
    @Override
    @Transactional
    public int generatePurchaseStatistics(Integer year, Integer month)
    {
        int count = 0;
        if (month != null)
        {
            // 生成指定月份的数据
            Map<String, Object> params = new java.util.HashMap<>();
            params.put("year", year);
            params.put("month", month);
            List<Map<String, Object>> statistics = purchaseStatisticsMapper.selectMonthlyPurchaseStatistics(params);
            
            for (Map<String, Object> stat : statistics)
            {
                PurchaseStatistics purchaseStatistics = new PurchaseStatistics();
                purchaseStatistics.setStatisticsYear(year);
                purchaseStatistics.setStatisticsMonth(month);
                if (stat.get("hospital_id") != null)
                {
                    purchaseStatistics.setHospitalId(Long.parseLong(stat.get("hospital_id").toString()));
                }
                if (stat.get("supplier_id") != null)
                {
                    purchaseStatistics.setSupplierId(Long.parseLong(stat.get("supplier_id").toString()));
                }
                if (stat.get("total_quantity") != null)
                {
                    purchaseStatistics.setPurchaseQuantity(new BigDecimal(stat.get("total_quantity").toString()));
                }
                if (stat.get("total_amount") != null)
                {
                    purchaseStatistics.setPurchaseAmount(new BigDecimal(stat.get("total_amount").toString()));
                }
                if (stat.get("order_count") != null)
                {
                    purchaseStatistics.setOrderCount(Integer.parseInt(stat.get("order_count").toString()));
                }
                purchaseStatistics.setCreateTime(DateUtils.getNowDate());
                purchaseStatisticsMapper.insertPurchaseStatistics(purchaseStatistics);
                count++;
            }
        }
        else
        {
            // 生成整年的数据
            for (int m = 1; m <= 12; m++)
            {
                Map<String, Object> params = new java.util.HashMap<>();
                params.put("year", year);
                params.put("month", m);
                List<Map<String, Object>> statistics = purchaseStatisticsMapper.selectMonthlyPurchaseStatistics(params);
                
                for (Map<String, Object> stat : statistics)
                {
                    PurchaseStatistics purchaseStatistics = new PurchaseStatistics();
                    purchaseStatistics.setStatisticsYear(year);
                    purchaseStatistics.setStatisticsMonth(m);
                    if (stat.get("hospital_id") != null)
                    {
                        purchaseStatistics.setHospitalId(Long.parseLong(stat.get("hospital_id").toString()));
                    }
                    if (stat.get("supplier_id") != null)
                    {
                        purchaseStatistics.setSupplierId(Long.parseLong(stat.get("supplier_id").toString()));
                    }
                    if (stat.get("total_quantity") != null)
                    {
                        purchaseStatistics.setPurchaseQuantity(new BigDecimal(stat.get("total_quantity").toString()));
                    }
                    if (stat.get("total_amount") != null)
                    {
                        purchaseStatistics.setPurchaseAmount(new BigDecimal(stat.get("total_amount").toString()));
                    }
                    if (stat.get("order_count") != null)
                    {
                        purchaseStatistics.setOrderCount(Integer.parseInt(stat.get("order_count").toString()));
                    }
                    purchaseStatistics.setCreateTime(DateUtils.getNowDate());
                    purchaseStatisticsMapper.insertPurchaseStatistics(purchaseStatistics);
                    count++;
                }
            }
        }
        return count;
    }
}

