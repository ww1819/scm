package com.scm.system.mapper;

import java.util.List;
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
     * 根据订单编号查询订单
     * 
     * @param orderNo 订单编号
     * @return 订单信息
     */
    public Order selectOrderByOrderNo(String orderNo);

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

