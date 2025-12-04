package com.scm.system.service;

import java.util.List;
import com.scm.system.domain.Delivery;
import com.scm.system.domain.DeliveryDetail;
import com.scm.system.domain.Order;

/**
 * 配送单 服务层
 * 
 * @author scm
 */
public interface IDeliveryService
{
    /**
     * 查询配送单信息
     * 
     * @param deliveryId 配送单ID
     * @return 配送单信息
     */
    public Delivery selectDeliveryById(Long deliveryId);

    /**
     * 查询配送单列表
     * 
     * @param delivery 配送单信息
     * @return 配送单集合
     */
    public List<Delivery> selectDeliveryList(Delivery delivery);

    /**
     * 新增配送单信息
     * 
     * @param delivery 配送单信息
     * @return 结果
     */
    public int insertDelivery(Delivery delivery);

    /**
     * 修改配送单信息
     * 
     * @param delivery 配送单信息
     * @return 结果
     */
    public int updateDelivery(Delivery delivery);

    /**
     * 批量删除配送单信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteDeliveryByIds(String ids);

    /**
     * 删除配送单信息
     * 
     * @param deliveryId 配送单ID
     * @return 结果
     */
    public int deleteDeliveryById(Long deliveryId);

    /**
     * 根据订单ID查询订单信息（用于引用订单）
     * 
     * @param orderId 订单ID
     * @return 订单信息
     */
    public Order selectOrderForDelivery(Long orderId);

    /**
     * 查询配送明细列表
     * 
     * @param deliveryId 配送单ID
     * @return 明细集合
     */
    public List<DeliveryDetail> selectDeliveryDetailListByDeliveryId(Long deliveryId);

    /**
     * 查询配送明细列表（支持条件查询）
     * 
     * @param deliveryDetail 明细信息
     * @return 明细集合
     */
    public List<DeliveryDetail> selectDeliveryDetailList(DeliveryDetail deliveryDetail);

    /**
     * 审核配送单
     * 
     * @param deliveryId 配送单ID
     * @return 结果
     */
    public int auditDelivery(Long deliveryId);
}

