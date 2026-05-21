package com.scm.system.service;

import com.scm.system.domain.vo.DashboardStatsVO;

/**
 * 首页仪表盘统计
 */
public interface IDashboardService
{
    /**
     * 按用户数据权限聚合首页统计
     *
     * @param userId 当前用户ID
     * @return 统计数据
     */
    DashboardStatsVO getDashboardStats(Long userId);
}
