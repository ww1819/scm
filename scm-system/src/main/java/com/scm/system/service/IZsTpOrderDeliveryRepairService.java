package com.scm.system.service;

import com.scm.system.domain.vo.ZsTpOrderDeliveryRepairResultVo;

/**
 * 第三方订单配送关联修复：回填配送主表订单引用、补写明细关联表
 */
public interface IZsTpOrderDeliveryRepairService
{
    /**
     * 修复第三方订单与配送单的关联（可重复执行，仅补缺）
     */
    ZsTpOrderDeliveryRepairResultVo repairDeliveryLinks(String operator);
}
