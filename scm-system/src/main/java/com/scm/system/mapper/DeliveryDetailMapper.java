package com.scm.system.mapper;

import java.util.List;
import com.scm.system.domain.DeliveryDetail;

/**
 * 配送明细 数据层
 * 
 * @author scm
 */
public interface DeliveryDetailMapper
{
    /**
     * 查询配送明细信息
     * 
     * @param detailId 明细ID
     * @return 明细信息
     */
    public DeliveryDetail selectDeliveryDetailById(Long detailId);

    /**
     * 查询配送明细列表
     * 
     * @param deliveryDetail 明细信息
     * @return 明细集合
     */
    public List<DeliveryDetail> selectDeliveryDetailList(DeliveryDetail deliveryDetail);

    /**
     * 根据配送单ID查询配送明细列表
     * 
     * @param deliveryId 配送单ID
     * @return 明细集合
     */
    public List<DeliveryDetail> selectDeliveryDetailListByDeliveryId(Long deliveryId);

    /**
     * 新增配送明细信息
     * 
     * @param deliveryDetail 明细信息
     * @return 结果
     */
    public int insertDeliveryDetail(DeliveryDetail deliveryDetail);

    /**
     * 批量新增配送明细信息
     * 
     * @param deliveryDetailList 明细信息列表
     * @return 结果
     */
    public int batchInsertDeliveryDetail(List<DeliveryDetail> deliveryDetailList);

    /**
     * 修改配送明细信息
     * 
     * @param deliveryDetail 明细信息
     * @return 结果
     */
    public int updateDeliveryDetail(DeliveryDetail deliveryDetail);

    /**
     * 删除配送明细信息
     * 
     * @param detailId 明细主键
     * @return 结果
     */
    public int deleteDeliveryDetailById(Long detailId);

    /**
     * 根据配送单ID删除配送明细信息
     * 
     * @param deliveryId 配送单ID
     * @return 结果
     */
    public int deleteDeliveryDetailByDeliveryId(Long deliveryId);

    /**
     * 批量删除配送明细信息
     * 
     * @param detailIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteDeliveryDetailByIds(String[] detailIds);
}

