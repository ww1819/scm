package com.scm.system.service;

import java.util.List;
import com.scm.system.domain.Order;
import com.scm.system.domain.OrderDetail;

/**
 * 订单 服务层
 * 
 * @author scm
 */
public interface IOrderService
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
     * 批量删除订单信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteOrderByIds(String ids);

    /**
     * 删除订单信息
     * 
     * @param orderId 订单ID
     * @return 结果
     */
    public int deleteOrderById(Long orderId);

    /**
     * 接收订单
     * 
     * @param orderId 订单ID
     * @return 结果
     */
    public int receiveOrder(Long orderId);

    /**
     * 查询订单明细列表
     * 
     * @param orderId 订单ID
     * @return 明细集合
     */
    public List<OrderDetail> selectOrderDetailListByOrderId(Long orderId);
}

