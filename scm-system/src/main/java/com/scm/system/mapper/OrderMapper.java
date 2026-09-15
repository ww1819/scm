package com.scm.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.scm.system.domain.Order;

/**
 * 订单 数据层
 * 
 * @author scm
 */
public interface OrderMapper
{
    /**
     * 查询订单信息
     * 
     * @param orderId 订单ID
     * @return 订单信息
     */
    public Order selectOrderById(Long orderId);

    /**
     * 查询订单列表
     *
     * @param order 订单信息
     * @return 订单集合
     */
    public List<Order> selectOrderList(Order order);

    /**
     * 按供应商 ID 列表查询最近订单（微信端，无数据范围）
     */
    public List<Order> selectOrderListBySupplierIds(@Param("supplierIds") List<Long> supplierIds, @Param("limit") int limit);

    /**
     * 根据订单编号查询订单（仅单号，多租户下同号可能不唯一；优先使用 {@link #selectOrderByTenantAndOrderNo}）
     *
     * @param orderNo 订单编号
     * @return 订单信息
     */
    public Order selectOrderByOrderNo(String orderNo);

    /**
     * 按 SPD 租户 + 订单编号查询（第一方推送对账推荐）
     */
    public Order selectOrderByTenantAndOrderNo(
        @Param("spdTenantId") String spdTenantId,
        @Param("orderNo") String orderNo);

    /**
     * 新增订单信息
     * 
     * @param order 订单信息
     * @return 结果
     */
    public int insertOrder(Order order);

    /**
     * 修改订单信息
     * 
     * @param order 订单信息
     * @return 结果
     */
    public int updateOrder(Order order);

    /**
     * 删除订单信息
     * 
     * @param orderId 订单主键
     * @return 结果
     */
    public int deleteOrderById(Long orderId);

    /**
     * 批量删除订单信息
     * 
     * @param orderIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteOrderByIds(String[] orderIds);
}

