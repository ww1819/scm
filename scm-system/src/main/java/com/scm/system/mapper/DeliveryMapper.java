package com.scm.system.mapper;

import java.util.List;
import com.scm.system.domain.Delivery;

/**
 * 配送单 数据层
 * 
 * @author scm
 */
public interface DeliveryMapper
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
     * 根据配送单号查询配送单
     * 
     * @param deliveryNo 配送单号
     * @return 配送单信息
     */
    public Delivery selectDeliveryByDeliveryNo(String deliveryNo);

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
     * 删除配送单信息
     * 
     * @param deliveryId 配送单主键
     * @return 结果
     */
    public int deleteDeliveryById(Long deliveryId);

    /**
     * 批量删除配送单信息
     * 
     * @param deliveryIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteDeliveryByIds(String[] deliveryIds);
}

