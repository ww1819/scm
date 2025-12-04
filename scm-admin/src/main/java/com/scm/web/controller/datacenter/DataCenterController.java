package com.scm.web.controller.datacenter;

import java.util.List;
import java.util.Map;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.scm.common.annotation.Log;
import com.scm.common.core.controller.BaseController;
import com.scm.common.core.domain.AjaxResult;
import com.scm.common.core.page.TableDataInfo;
import com.scm.common.enums.BusinessType;
import com.scm.system.service.IDataCenterService;

/**
 * 数据中心
 * 
 * @author scm
 */
@Controller
@RequestMapping("/datacenter/datacenter")
public class DataCenterController extends BaseController
{
    private String prefix = "datacenter";

    @Autowired
    private IDataCenterService dataCenterService;

    @RequiresPermissions("datacenter:datacenter:view")
    @GetMapping()
    public String datacenter()
    {
        return prefix + "/datacenter";
    }

    /**
     * 月采购量统计页面
     */
    @RequiresPermissions("datacenter:datacenter:view")
    @GetMapping("/monthly")
    public String monthly()
    {
        return prefix + "/monthly";
    }

    /**
     * 年采购量统计页面
     */
    @RequiresPermissions("datacenter:datacenter:view")
    @GetMapping("/yearly")
    public String yearly()
    {
        return prefix + "/yearly";
    }

    /**
     * 数据分析报表页面
     */
    @RequiresPermissions("datacenter:datacenter:view")
    @GetMapping("/analysis")
    public String analysis()
    {
        return prefix + "/analysis";
    }

    /**
     * 查询月采购量统计
     */
    @RequiresPermissions("datacenter:datacenter:list")
    @PostMapping("/monthly/list")
    @ResponseBody
    public TableDataInfo monthlyList(Integer year, Integer month, Long hospitalId, Long supplierId)
    {
        startPage();
        Map<String, Object> params = new java.util.HashMap<>();
        params.put("year", year);
        params.put("month", month);
        params.put("hospitalId", hospitalId);
        params.put("supplierId", supplierId);
        List<Map<String, Object>> list = dataCenterService.selectMonthlyPurchaseStatistics(params);
        return getDataTable(list);
    }

    /**
     * 查询年采购量统计
     */
    @RequiresPermissions("datacenter:datacenter:list")
    @PostMapping("/yearly/list")
    @ResponseBody
    public TableDataInfo yearlyList(Integer year, Long hospitalId, Long supplierId)
    {
        startPage();
        Map<String, Object> params = new java.util.HashMap<>();
        params.put("year", year);
        params.put("hospitalId", hospitalId);
        params.put("supplierId", supplierId);
        List<Map<String, Object>> list = dataCenterService.selectYearlyPurchaseStatistics(params);
        return getDataTable(list);
    }

    /**
     * 查询采购数据分析报表
     */
    @RequiresPermissions("datacenter:datacenter:list")
    @PostMapping("/analysis/list")
    @ResponseBody
    public TableDataInfo analysisList(String beginTime, String endTime, Long hospitalId, Long supplierId, Long materialId)
    {
        startPage();
        Map<String, Object> params = new java.util.HashMap<>();
        params.put("beginTime", beginTime);
        params.put("endTime", endTime);
        params.put("hospitalId", hospitalId);
        params.put("supplierId", supplierId);
        params.put("materialId", materialId);
        List<Map<String, Object>> list = dataCenterService.selectPurchaseAnalysisReport(params);
        return getDataTable(list);
    }

    /**
     * 导出月采购量统计
     */
    @RequiresPermissions("datacenter:datacenter:export")
    @Log(title = "月采购量统计", businessType = BusinessType.EXPORT)
    @PostMapping("/monthly/export")
    @ResponseBody
    public AjaxResult exportMonthly(Integer year, Integer month, Long hospitalId, Long supplierId)
    {
        Map<String, Object> params = new java.util.HashMap<>();
        params.put("year", year);
        params.put("month", month);
        params.put("hospitalId", hospitalId);
        params.put("supplierId", supplierId);
        dataCenterService.selectMonthlyPurchaseStatistics(params);
        // 这里可以根据需要转换为Excel导出
        return AjaxResult.success("导出成功");
    }

    /**
     * 导出年采购量统计
     */
    @RequiresPermissions("datacenter:datacenter:export")
    @Log(title = "年采购量统计", businessType = BusinessType.EXPORT)
    @PostMapping("/yearly/export")
    @ResponseBody
    public AjaxResult exportYearly(Integer year, Long hospitalId, Long supplierId)
    {
        Map<String, Object> params = new java.util.HashMap<>();
        params.put("year", year);
        params.put("hospitalId", hospitalId);
        params.put("supplierId", supplierId);
        dataCenterService.selectYearlyPurchaseStatistics(params);
        // 这里可以根据需要转换为Excel导出
        return AjaxResult.success("导出成功");
    }

    /**
     * 导出采购数据分析报表
     */
    @RequiresPermissions("datacenter:datacenter:export")
    @Log(title = "采购数据分析报表", businessType = BusinessType.EXPORT)
    @PostMapping("/analysis/export")
    @ResponseBody
    public AjaxResult exportAnalysis(String beginTime, String endTime, Long hospitalId, Long supplierId, Long materialId)
    {
        Map<String, Object> params = new java.util.HashMap<>();
        params.put("beginTime", beginTime);
        params.put("endTime", endTime);
        params.put("hospitalId", hospitalId);
        params.put("supplierId", supplierId);
        params.put("materialId", materialId);
        dataCenterService.selectPurchaseAnalysisReport(params);
        // 这里可以根据需要转换为Excel导出
        return AjaxResult.success("导出成功");
    }

    /**
     * 生成采购统计数据
     */
    @RequiresPermissions("datacenter:datacenter:edit")
    @Log(title = "生成采购统计数据", businessType = BusinessType.INSERT)
    @PostMapping("/generate")
    @ResponseBody
    public AjaxResult generate(Integer year, Integer month)
    {
        int count = dataCenterService.generatePurchaseStatistics(year, month);
        return success("成功生成 " + count + " 条统计数据");
    }
}

