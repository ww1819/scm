package com.scm.system.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.scm.system.domain.Delivery;
import com.scm.system.domain.ReconciliationSupplierOption;

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
     * 统计未删除配送单中已关联某第三方订单的数量
     */
    public int countDeliveryByZsOrderId(String zsOrderId);

    /**
     * 统计未删除配送单中已关联某本系统订单的数量
     */
    int countDeliveryByOrderId(@Param("orderId") Long orderId);

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
     * 反审核：清空审核信息并将单据状态置为未审核
     */
    int unAuditDelivery(@Param("deliveryId") Long deliveryId, @Param("updateBy") String updateBy,
        @Param("unauditByNameSnapshot") String unauditByNameSnapshot);

    /**
     * 逻辑删除配送单主表（del_flag=2）
     *
     * @param deliveryId 配送单主键
     * @param delBy 删除人
     * @return 结果
     */
    public int deleteDeliveryById(@Param("deliveryId") Long deliveryId, @Param("delBy") String delBy);

    /**
     * 批量逻辑删除配送单主表
     *
     * @param deliveryIds 需要删除的数据ID
     * @param delBy 删除人
     * @return 结果
     */
    public int deleteDeliveryByIds(@Param("ids") String[] deliveryIds, @Param("delBy") String delBy);

    /**
     * 按月份汇总配送金额（对账表年度视图）
     */
    List<Map<String, Object>> sumDeliveryAmountGroupByMonth(@Param("hospitalId") Long hospitalId,
            @Param("supplierId") Long supplierId, @Param("yearBegin") String yearBegin,
            @Param("yearEnd") String yearEnd);

    /**
     * 对账表：某医院在配送/结算中出现的供应商ID（去重）
     */
    List<Long> selectReconciliationSupplierIdsByHospital(@Param("hospitalId") Long hospitalId);

    /**
     * 对账表：医院绑定及业务往来供应商（含名称、拼音）
     */
    List<ReconciliationSupplierOption> selectReconciliationSuppliersByHospital(@Param("hospitalId") Long hospitalId);
}

