package com.scm.system.service.impl;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.scm.common.core.text.Convert;
import com.scm.common.utils.DateUtils;
import com.scm.common.utils.StringUtils;
import com.scm.system.domain.Order;
import com.scm.system.domain.OrderDetail;
import com.scm.system.mapper.OrderDetailMapper;
import com.scm.system.mapper.OrderMapper;
import com.scm.system.service.IOrderService;

/**
 * 订单 服务层实现
 * 
 * @author scm
 */
@Service
public class OrderServiceImpl implements IOrderService
{
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    /**
     * 查询订单信息
     * 
     * @param orderId 订单ID
     * @return 订单信息
     */
    @Override
    public Order selectOrderById(Long orderId)
    {
        Order order = orderMapper.selectOrderById(orderId);
        if (order != null)
        {
            List<OrderDetail> details = orderDetailMapper.selectOrderDetailListByOrderId(orderId);
            order.setOrderDetails(details);
        }
        return order;
    }

    /**
     * 查询订单列表
     * 
     * @param order 订单信息
     * @return 订单集合
     */
    @Override
    public List<Order> selectOrderList(Order order)
    {
        return orderMapper.selectOrderList(order);
    }

    /**
     * 新增订单信息
     * 
     * @param order 订单信息
     * @return 结果
     */
    @Override
    @Transactional
    public int insertOrder(Order order)
    {
        if (StringUtils.isEmpty(order.getOrderStatus()))
        {
            order.setOrderStatus("0"); // 默认待接收
        }
        if (order.getOrderDate() == null)
        {
            order.setOrderDate(DateUtils.getNowDate());
        }
        // 如果订单编号为空，自动生成唯一编号
        if (StringUtils.isEmpty(order.getOrderNo()))
        {
            order.setOrderNo(generateOrderNo());
        }
        order.setCreateTime(DateUtils.getNowDate());
        
        // 计算订单金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (order.getOrderDetails() != null && !order.getOrderDetails().isEmpty())
        {
            for (OrderDetail detail : order.getOrderDetails())
            {
                if (detail.getPurchasePrice() != null && detail.getOrderQuantity() != null)
                {
                    BigDecimal amount = detail.getPurchasePrice().multiply(new BigDecimal(detail.getOrderQuantity()));
                    detail.setAmount(amount);
                    totalAmount = totalAmount.add(amount);
                    detail.setRemainingQuantity(detail.getOrderQuantity());
                }
            }
        }
        order.setOrderAmount(totalAmount);
        
        int rows = orderMapper.insertOrder(order);
        
        // 保存订单明细
        if (order.getOrderDetails() != null && !order.getOrderDetails().isEmpty())
        {
            for (OrderDetail detail : order.getOrderDetails())
            {
                detail.setOrderId(order.getOrderId());
            }
            orderDetailMapper.batchInsertOrderDetail(order.getOrderDetails());
        }
        
        return rows;
    }

    /**
     * 修改订单信息
     * 
     * @param order 订单信息
     * @return 结果
     */
    @Override
    @Transactional
    public int updateOrder(Order order)
    {
        order.setUpdateTime(DateUtils.getNowDate());
        
        // 如果修改了明细，重新计算订单金额并保存明细
        if (order.getOrderDetails() != null && !order.getOrderDetails().isEmpty())
        {
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (OrderDetail detail : order.getOrderDetails())
            {
                if (detail.getPurchasePrice() != null && detail.getOrderQuantity() != null)
                {
                    BigDecimal amount = detail.getPurchasePrice().multiply(new BigDecimal(detail.getOrderQuantity()));
                    detail.setAmount(amount);
                    totalAmount = totalAmount.add(amount);
                    // 设置订单ID
                    detail.setOrderId(order.getOrderId());
                    // 如果剩余待配送数未设置，默认等于订货数量
                    if (detail.getRemainingQuantity() == null)
                    {
                        detail.setRemainingQuantity(detail.getOrderQuantity());
                    }
                }
            }
            order.setOrderAmount(totalAmount);
            
            // 删除旧的明细
            orderDetailMapper.deleteOrderDetailByOrderId(order.getOrderId());
            
            // 插入新的明细
            orderDetailMapper.batchInsertOrderDetail(order.getOrderDetails());
        }
        else
        {
            // 如果没有明细，删除所有旧明细
            orderDetailMapper.deleteOrderDetailByOrderId(order.getOrderId());
            order.setOrderAmount(BigDecimal.ZERO);
        }
        
        return orderMapper.updateOrder(order);
    }

    /**
     * 批量删除订单信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteOrderByIds(String ids)
    {
        String[] orderIds = Convert.toStrArray(ids);
        for (String orderId : orderIds)
        {
            // 删除订单明细
            orderDetailMapper.deleteOrderDetailByOrderId(Long.parseLong(orderId));
        }
        return orderMapper.deleteOrderByIds(orderIds);
    }

    /**
     * 删除订单信息
     * 
     * @param orderId 订单ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteOrderById(Long orderId)
    {
        // 删除订单明细
        orderDetailMapper.deleteOrderDetailByOrderId(orderId);
        return orderMapper.deleteOrderById(orderId);
    }

    /**
     * 接收订单
     * 
     * @param orderId 订单ID
     * @return 结果
     */
    @Override
    public int receiveOrder(Long orderId)
    {
        Order order = orderMapper.selectOrderById(orderId);
        if (order == null)
        {
            return 0;
        }
        order.setOrderStatus("1"); // 已接收
        order.setUpdateTime(DateUtils.getNowDate());
        return orderMapper.updateOrder(order);
    }

    /**
     * 查询订单明细列表
     * 
     * @param orderId 订单ID
     * @return 明细集合
     */
    @Override
    public List<OrderDetail> selectOrderDetailListByOrderId(Long orderId)
    {
        return orderDetailMapper.selectOrderDetailListByOrderId(orderId);
    }

    /**
     * 生成唯一的订单编号
     * 
     * @return 订单编号
     */
    private String generateOrderNo()
    {
        String code;
        int maxAttempts = 10;
        int attempt = 0;
        do
        {
            // 使用时间戳+随机数生成编号
            code = "ORD" + System.currentTimeMillis() + (int)(Math.random() * 1000);
            if (code.length() > 50)
            {
                code = code.substring(0, 50);
            }
            attempt++;
        }
        while (orderMapper.selectOrderByOrderNo(code) != null && attempt < maxAttempts);
        
        if (attempt >= maxAttempts)
        {
            // 如果10次尝试都失败，使用UUID
            String uuid = java.util.UUID.randomUUID().toString().replace("-", "");
            code = "ORD" + uuid.substring(0, Math.min(20, uuid.length()));
        }
        
        return code;
    }
}

