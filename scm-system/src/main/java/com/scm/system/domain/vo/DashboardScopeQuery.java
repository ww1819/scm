package com.scm.system.domain.vo;

import java.util.HashMap;
import java.util.Map;

/**
 * 首页统计查询条件（含与列表页一致的数据权限 params）
 */
public class DashboardScopeQuery
{
    private Long hospitalId;
    private Long supplierId;
    /** 最近 12 个月趋势起点 yyyy-MM */
    private String trendStartMonth;
    /** 本月结算统计起始日期 yyyy-MM-dd */
    private String monthStart;
    /** 下月首日 yyyy-MM-dd（不含） */
    private String monthEnd;
    private Map<String, Object> params = new HashMap<>();

    public Long getHospitalId()
    {
        return hospitalId;
    }

    public void setHospitalId(Long hospitalId)
    {
        this.hospitalId = hospitalId;
    }

    public Long getSupplierId()
    {
        return supplierId;
    }

    public void setSupplierId(Long supplierId)
    {
        this.supplierId = supplierId;
    }

    public String getTrendStartMonth()
    {
        return trendStartMonth;
    }

    public void setTrendStartMonth(String trendStartMonth)
    {
        this.trendStartMonth = trendStartMonth;
    }

    public String getMonthStart()
    {
        return monthStart;
    }

    public void setMonthStart(String monthStart)
    {
        this.monthStart = monthStart;
    }

    public String getMonthEnd()
    {
        return monthEnd;
    }

    public void setMonthEnd(String monthEnd)
    {
        this.monthEnd = monthEnd;
    }

    public Map<String, Object> getParams()
    {
        return params;
    }

    public void setParams(Map<String, Object> params)
    {
        this.params = params != null ? params : new HashMap<>();
    }

    public boolean isScopeBlocked()
    {
        Object block = params.get("scopePairBlock");
        return block != null && (Boolean.TRUE.equals(block) || "true".equals(String.valueOf(block)));
    }
}
