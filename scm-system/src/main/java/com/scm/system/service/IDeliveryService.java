package com.scm.system.service;

import java.util.List;
import com.scm.system.domain.Delivery;
import com.scm.system.domain.DeliveryDetail;
import com.scm.system.domain.DeliveryDownloadLog;
import com.scm.system.domain.Order;
import com.scm.system.domain.ZsTpOrder;
import com.scm.system.domain.ZsTpOrderDetail;
import com.scm.system.domain.vo.OrderDetailDeliveryTraceVo;
import com.scm.system.domain.vo.ZsTpOrderForDeliveryVo;

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
     * 校验配送单是否允许编辑；已审核（含旧数据仅单据状态为已审核的兼容）则抛业务异常
     *
     * @param deliveryId 配送单ID
     */
    public void assertDeliveryEditable(Long deliveryId);

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
     * 第三方订单列表（zs_tp_order，未删除；关联医院/供应商展示字段，并按当前用户医院/供应商数据范围过滤）
     */
    public List<ZsTpOrder> selectZsTpOrderList(ZsTpOrder query);

    /**
     * 第三方订单查询列表（关联医院/供应商；按当前用户医院/供应商数据范围过滤）
     */
    public List<ZsTpOrder> selectZsTpOrderQueryList(ZsTpOrder query);

    /**
     * 第三方订单主表（查看页）
     */
    public ZsTpOrder selectZsTpOrderById(String id);

    /**
     * 第三方订单主表（查看页，带医院/供应商展示字段，并校验当前用户数据范围）
     */
    public ZsTpOrder selectZsTpOrderHeadForView(String zsOrderId);

    /**
     * 校验当前用户是否可查看该第三方订单（明细、配送回查等接口调用）
     */
    public void assertZsTpOrderViewScope(String zsOrderId);

    /**
     * 按主键加载第三方订单并映射为配送单草稿数据
     */
    public ZsTpOrderForDeliveryVo selectZsTpOrderForDelivery(String zsOrderId);

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
     * @param auditBy 审核人（登录名）
     * @return 结果
     */
    public int auditDelivery(Long deliveryId, String auditBy);

    /**
     * 反审核配送单（已产生接口下载记录时不允许）
     */
    public int unAuditDelivery(Long deliveryId, String updateBy);

    /**
     * 配送单接口下载记录（按时间倒序）
     */
    public List<DeliveryDownloadLog> selectDeliveryDownloadLogList(Long deliveryId);

    /**
     * 供应商确认第三方订单（记录确认人、确认时间）
     */
    void confirmZsTpOrder(String zsOrderId);

    /**
     * 医院作废第三方订单（记录作废人、作废时间；作废后不可再引用配送）
     */
    void voidZsTpOrder(String zsOrderId);

    /**
     * 第三方订单明细分页/查看：带配送数量汇总
     */
    public List<ZsTpOrderDetail> selectZsTpOrderDetailListForView(String zsOrderId);

    /**
     * 本系统订单关联的配送单列表
     */
    public List<Delivery> selectDeliveriesByOrderId(Long orderId);

    /**
     * 第三方订单关联的配送单列表
     */
    public List<Delivery> selectDeliveriesByZsOrderId(String zsOrderId);

    /**
     * 我方订单明细行关联的配送明细
     */
    public List<OrderDetailDeliveryTraceVo> selectTracesByScmOrderDetailId(Long orderDetailId);

    /**
     * 第三方订单明细行关联的配送明细
     */
    public List<OrderDetailDeliveryTraceVo> selectTracesByZsOrderDetailId(String zsOrderDetailId);
}

