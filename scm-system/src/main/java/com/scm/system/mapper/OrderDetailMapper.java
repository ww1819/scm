package com.scm.system.mapper;

import java.util.List;
import com.scm.system.domain.OrderDetail;

/**
 * 订单明细 数据层
 * 
 * @author scm
 */
public interface OrderDetailMapper
{
    /**
     * 查询订单明细信息
     * 
     * @param detailId 明细ID
     * @return 明细信息
     */
    public OrderDetail selectOrderDetailById(Long detailId);

    /**
     * 查询订单明细列表
     * 
     * @param orderDetail 明细信息
     * @return 明细集合
     */
    public List<OrderDetail> selectOrderDetailList(OrderDetail orderDetail);

    /**
     * 根据订单ID查询订单明细列表
     * 
     * @param orderId 订单ID
     * @return 明细集合
     */
    public List<OrderDetail> selectOrderDetailListByOrderId(Long orderId);

    /**
     * 新增订单明细信息
     * 
     * @param orderDetail 明细信息
     * @return 结果
     */
    public int insertOrderDetail(OrderDetail orderDetail);

    /**
     * 批量新增订单明细信息
     * 
     * @param orderDetailList 明细信息列表
     * @return 结果
     */
    public int batchInsertOrderDetail(List<OrderDetail> orderDetailList);

    /**
     * 修改订单明细信息
     * 
     * @param orderDetail 明细信息
     * @return 结果
     */
    public int updateOrderDetail(OrderDetail orderDetail);

    /**
     * 删除订单明细信息
     * 
     * @param detailId 明细主键
     * @return 结果
     */
    public int deleteOrderDetailById(Long detailId);

    /**
     * 根据订单ID删除订单明细信息
     * 
     * @param orderId 订单ID
     * @return 结果
     */
    public int deleteOrderDetailByOrderId(Long orderId);

    /**
     * 批量删除订单明细信息
     * 
     * @param detailIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteOrderDetailByIds(String[] detailIds);
}

