package com.scm.system.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.scm.common.core.text.Convert;
import com.scm.common.exception.ServiceException;
import com.scm.common.utils.DateUtils;
import com.scm.common.utils.StringUtils;
import com.scm.system.domain.Delivery;
import com.scm.system.domain.DeliveryDetail;
import com.scm.system.domain.Order;
import com.scm.system.domain.OrderDetail;
import com.scm.system.domain.ZsTpOrder;
import com.scm.system.domain.ZsTpOrderDetail;
import com.scm.system.domain.vo.ZsTpOrderForDeliveryVo;
import com.scm.system.mapper.DeliveryDetailMapper;
import com.scm.system.mapper.DeliveryMapper;
import com.scm.system.mapper.OrderDetailMapper;
import com.scm.system.mapper.OrderMapper;
import com.scm.system.mapper.ZsTpOrderMapper;
import com.scm.system.service.IDeliveryService;

/**
 * 配送单 服务层实现
 * 
 * @author scm
 */
@Service
public class DeliveryServiceImpl implements IDeliveryService
{
    @Autowired
    private DeliveryMapper deliveryMapper;

    @Autowired
    private DeliveryDetailMapper deliveryDetailMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private ZsTpOrderMapper zsTpOrderMapper;

    /**
     * 查询配送单信息
     * 
     * @param deliveryId 配送单ID
     * @return 配送单信息
     */
    @Override
    public Delivery selectDeliveryById(Long deliveryId)
    {
        Delivery delivery = deliveryMapper.selectDeliveryById(deliveryId);
        if (delivery != null)
        {
            List<DeliveryDetail> details = deliveryDetailMapper.selectDeliveryDetailListByDeliveryId(deliveryId);
            delivery.setDeliveryDetails(details);
        }
        return delivery;
    }

    /**
     * 查询配送单列表
     * 
     * @param delivery 配送单信息
     * @return 配送单集合
     */
    @Override
    public List<Delivery> selectDeliveryList(Delivery delivery)
    {
        return deliveryMapper.selectDeliveryList(delivery);
    }

    /**
     * 新增配送单信息
     * 
     * @param delivery 配送单信息
     * @return 结果
     */
    @Override
    @Transactional
    public int insertDelivery(Delivery delivery)
    {
        if (StringUtils.isNotEmpty(delivery.getZsOrderId()))
        {
            if (deliveryMapper.countDeliveryByZsOrderId(delivery.getZsOrderId()) > 0)
            {
                throw new ServiceException("该中设订单已生成配送单，请勿重复生成");
            }
        }
        if (StringUtils.isEmpty(delivery.getDeliveryStatus()))
        {
            delivery.setDeliveryStatus("0"); // 默认未审核
        }
        // 如果配送单号为空，自动生成唯一编号
        if (StringUtils.isEmpty(delivery.getDeliveryNo()))
        {
            delivery.setDeliveryNo(generateDeliveryNo());
        }
        delivery.setCreateTime(DateUtils.getNowDate());
        
        // 计算配送金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (delivery.getDeliveryDetails() != null && !delivery.getDeliveryDetails().isEmpty())
        {
            for (DeliveryDetail detail : delivery.getDeliveryDetails())
            {
                if (detail.getPrice() != null && detail.getDeliveryQuantity() != null)
                {
                    BigDecimal amount = detail.getPrice().multiply(detail.getDeliveryQuantity());
                    detail.setAmount(amount);
                    totalAmount = totalAmount.add(amount);
                }
            }
        }
        delivery.setDeliveryAmount(totalAmount);
        
        int rows = deliveryMapper.insertDelivery(delivery);
        
        // 保存配送明细
        if (delivery.getDeliveryDetails() != null && !delivery.getDeliveryDetails().isEmpty())
        {
            for (DeliveryDetail detail : delivery.getDeliveryDetails())
            {
                detail.setDeliveryId(delivery.getDeliveryId());
            }
            deliveryDetailMapper.batchInsertDeliveryDetail(delivery.getDeliveryDetails());
            
            // 更新订单明细的剩余待配送数
            if (delivery.getOrderId() != null)
            {
                updateOrderRemainingQuantity(delivery);
            }
        }
        
        return rows;
    }

    /**
     * 修改配送单信息
     * 
     * @param delivery 配送单信息
     * @return 结果
     */
    @Override
    @Transactional
    public int updateDelivery(Delivery delivery)
    {
        delivery.setUpdateTime(DateUtils.getNowDate());
        
        // 如果修改了明细，重新计算配送金额
        if (delivery.getDeliveryDetails() != null && !delivery.getDeliveryDetails().isEmpty())
        {
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (DeliveryDetail detail : delivery.getDeliveryDetails())
            {
                if (detail.getPrice() != null && detail.getDeliveryQuantity() != null)
                {
                    BigDecimal amount = detail.getPrice().multiply(detail.getDeliveryQuantity());
                    detail.setAmount(amount);
                    totalAmount = totalAmount.add(amount);
                }
            }
            delivery.setDeliveryAmount(totalAmount);
        }
        
        return deliveryMapper.updateDelivery(delivery);
    }

    /**
     * 批量删除配送单信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteDeliveryByIds(String ids)
    {
        String[] deliveryIds = Convert.toStrArray(ids);
        for (String deliveryId : deliveryIds)
        {
            // 删除配送明细
            deliveryDetailMapper.deleteDeliveryDetailByDeliveryId(Long.parseLong(deliveryId));
        }
        return deliveryMapper.deleteDeliveryByIds(deliveryIds);
    }

    /**
     * 删除配送单信息
     * 
     * @param deliveryId 配送单ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteDeliveryById(Long deliveryId)
    {
        // 删除配送明细
        deliveryDetailMapper.deleteDeliveryDetailByDeliveryId(deliveryId);
        return deliveryMapper.deleteDeliveryById(deliveryId);
    }

    /**
     * 根据订单ID查询订单信息（用于引用订单）
     * 
     * @param orderId 订单ID
     * @return 订单信息
     */
    @Override
    public Order selectOrderForDelivery(Long orderId)
    {
        Order order = orderMapper.selectOrderById(orderId);
        if (order != null)
        {
            List<OrderDetail> details = orderDetailMapper.selectOrderDetailListByOrderId(orderId);
            order.setOrderDetails(details);
        }
        return order;
    }

    @Override
    public List<ZsTpOrder> selectZsTpOrderList(ZsTpOrder query)
    {
        return zsTpOrderMapper.selectZsTpOrderList(query);
    }

    @Override
    public ZsTpOrderForDeliveryVo selectZsTpOrderForDelivery(String zsOrderId)
    {
        if (StringUtils.isEmpty(zsOrderId))
        {
            throw new ServiceException("中设订单主键不能为空");
        }
        ZsTpOrder head = zsTpOrderMapper.selectZsTpOrderById(zsOrderId);
        if (head == null)
        {
            throw new ServiceException("中设订单不存在或已删除");
        }
        List<ZsTpOrderDetail> lines = zsTpOrderMapper.selectZsTpOrderDetailListByOrderId(zsOrderId);
        ZsTpOrderForDeliveryVo vo = new ZsTpOrderForDeliveryVo();
        vo.setZsOrderId(head.getId());
        vo.setOrderNo(StringUtils.trimToEmpty(head.getDh()));
        vo.setWarehouse(StringUtils.trimToEmpty(head.getCk()));
        vo.setOrderAmount(head.getSheetJe() != null
            ? head.getSheetJe().setScale(2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO);
        vo.setOrderDate(head.getCreateTime());
        StringBuilder remark = new StringBuilder();
        if (StringUtils.isNotEmpty(head.getKsmc()))
        {
            remark.append("科室:").append(head.getKsmc()).append("；");
        }
        if (StringUtils.isNotEmpty(head.getCk()))
        {
            remark.append("仓库:").append(head.getCk()).append("；");
        }
        if (StringUtils.isNotEmpty(head.getSup()))
        {
            remark.append("供应商:").append(head.getSup()).append("；");
        }
        if (StringUtils.isNotEmpty(head.getBz()))
        {
            remark.append(head.getBz());
        }
        vo.setRemark(remark.toString());

        List<DeliveryDetail> details = new ArrayList<>();
        if (lines != null)
        {
            for (ZsTpOrderDetail line : lines)
            {
                details.add(mapZsDetailLine(line));
            }
        }
        vo.setDeliveryDetails(details);
        return vo;
    }

    private DeliveryDetail mapZsDetailLine(ZsTpOrderDetail line)
    {
        DeliveryDetail d = new DeliveryDetail();
        d.setMaterialId(0L);
        d.setZsOrderDetailId(line.getId());
        d.setOrderDetailId(null);
        d.setMaterialCode(StringUtils.trimToEmpty(line.getCode()));
        d.setMaterialName(StringUtils.trimToEmpty(line.getName()));
        d.setSpecification(StringUtils.trimToEmpty(line.getGg()));
        d.setModel(StringUtils.trimToEmpty(line.getBzl()));
        d.setUnit(StringUtils.trimToEmpty(line.getDw()));
        BigDecimal sl = line.getSl() != null ? line.getSl() : BigDecimal.ZERO;
        BigDecimal dj = line.getDj() != null ? line.getDj() : BigDecimal.ZERO;
        BigDecimal je = line.getJe();
        if (je == null)
        {
            je = sl.multiply(dj).setScale(2, RoundingMode.HALF_UP);
        }
        else
        {
            je = je.setScale(2, RoundingMode.HALF_UP);
        }
        d.setDeliveryQuantity(sl);
        d.setRemainingQuantity(sl);
        d.setPrice(dj.setScale(4, RoundingMode.HALF_UP));
        d.setAmount(je);
        d.setManufacturer(StringUtils.trimToEmpty(line.getSccj()));
        d.setRegisterNo(StringUtils.trimToEmpty(line.getZcz()));
        d.setBatchNo("");
        d.setMainBarcode("");
        d.setAuxBarcode("");
        return d;
    }

    /**
     * 查询配送明细列表
     * 
     * @param deliveryId 配送单ID
     * @return 明细集合
     */
    @Override
    public List<DeliveryDetail> selectDeliveryDetailListByDeliveryId(Long deliveryId)
    {
        return deliveryDetailMapper.selectDeliveryDetailListByDeliveryId(deliveryId);
    }

    @Override
    public List<DeliveryDetail> selectDeliveryDetailList(DeliveryDetail deliveryDetail)
    {
        return deliveryDetailMapper.selectDeliveryDetailList(deliveryDetail);
    }

    /**
     * 审核配送单
     * 
     * @param deliveryId 配送单ID
     * @return 结果
     */
    @Override
    public int auditDelivery(Long deliveryId)
    {
        Delivery delivery = deliveryMapper.selectDeliveryById(deliveryId);
        if (delivery == null)
        {
            return 0;
        }
        delivery.setDeliveryStatus("1"); // 已审核
        delivery.setUpdateTime(DateUtils.getNowDate());
        return deliveryMapper.updateDelivery(delivery);
    }

    /**
     * 生成唯一的配送单号
     * 
     * @return 配送单号
     */
    private String generateDeliveryNo()
    {
        String code;
        int maxAttempts = 10;
        int attempt = 0;
        do
        {
            // 使用时间戳+随机数生成编号
            code = "DEL" + System.currentTimeMillis() + (int)(Math.random() * 1000);
            if (code.length() > 50)
            {
                code = code.substring(0, 50);
            }
            attempt++;
        }
        while (deliveryMapper.selectDeliveryByDeliveryNo(code) != null && attempt < maxAttempts);
        
        if (attempt >= maxAttempts)
        {
            // 如果10次尝试都失败，使用UUID
            String uuid = java.util.UUID.randomUUID().toString().replace("-", "");
            code = "DEL" + uuid.substring(0, Math.min(20, uuid.length()));
        }
        
        return code;
    }

    /**
     * 更新订单明细的剩余待配送数
     * 
     * @param delivery 配送单信息
     */
    private void updateOrderRemainingQuantity(Delivery delivery)
    {
        if (delivery.getOrderId() == null || delivery.getDeliveryDetails() == null)
        {
            return;
        }
        
        for (DeliveryDetail deliveryDetail : delivery.getDeliveryDetails())
        {
            if (deliveryDetail.getOrderDetailId() != null)
            {
                OrderDetail orderDetail = orderDetailMapper.selectOrderDetailById(deliveryDetail.getOrderDetailId());
                if (orderDetail != null && orderDetail.getRemainingQuantity() != null)
                {
                    BigDecimal remaining = new BigDecimal(orderDetail.getRemainingQuantity()).subtract(deliveryDetail.getDeliveryQuantity());
                    if (remaining.compareTo(BigDecimal.ZERO) < 0)
                    {
                        remaining = BigDecimal.ZERO;
                    }
                    orderDetail.setRemainingQuantity(remaining.intValue());
                    orderDetailMapper.updateOrderDetail(orderDetail);
                }
            }
        }
    }
}

