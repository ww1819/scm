package com.scm.system.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.scm.common.core.text.Convert;
import com.scm.common.exception.ServiceException;
import com.scm.common.utils.DateUtils;
import com.scm.common.utils.StringUtils;
import com.scm.common.utils.uuid.IdUtils;
import com.scm.system.domain.Delivery;
import com.scm.system.domain.DeliveryDetail;
import com.scm.system.domain.Order;
import com.scm.system.domain.OrderDetail;
import com.scm.system.domain.ScmOrderDetailDeliveryRel;
import com.scm.system.domain.ZsTpOrder;
import com.scm.system.domain.ZsTpOrderDetail;
import com.scm.system.domain.ZsTpOrderDetailDeliveryRel;
import com.scm.system.domain.vo.OrderDetailDeliveryTraceVo;
import com.scm.system.domain.vo.OrderLineDeliveryQtyVo;
import com.scm.system.domain.vo.ZsTpOrderForDeliveryVo;
import com.scm.system.mapper.DeliveryDetailMapper;
import com.scm.system.mapper.DeliveryMapper;
import com.scm.system.mapper.OrderDeliveryTraceMapper;
import com.scm.system.mapper.OrderDetailMapper;
import com.scm.system.mapper.OrderMapper;
import com.scm.system.mapper.ScmOrderDetailDeliveryRelMapper;
import com.scm.system.mapper.ZsTpOrderDetailDeliveryRelMapper;
import com.scm.system.mapper.ZsTpOrderMapper;
import com.scm.system.service.IDeliveryService;
import com.scm.system.service.ScmBarcodeSeedService;

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

    @Autowired
    private ScmOrderDetailDeliveryRelMapper scmOrderDetailDeliveryRelMapper;

    @Autowired
    private ZsTpOrderDetailDeliveryRelMapper zsTpOrderDetailDeliveryRelMapper;

    @Autowired
    private OrderDeliveryTraceMapper orderDeliveryTraceMapper;

    @Autowired
    private ScmBarcodeSeedService scmBarcodeSeedService;

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
            enrichDetailLineApplyQty(details);
            scmBarcodeSeedService.attachDetailBarcodes(details, deliveryId);
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
        enrichDeliverySnapshot(delivery);
        enrichDeliveryDetailPackCoefficients(delivery);
        validateDeliveryDetailPackQuantities(delivery.getDeliveryDetails());
        if (StringUtils.isEmpty(delivery.getDeliveryStatus()))
        {
            delivery.setDeliveryStatus("0"); // 默认未审核
        }
        if (StringUtils.isEmpty(delivery.getAuditStatus()))
        {
            delivery.setAuditStatus("0"); // 待审核
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
            insertOrderDeliveryDetailRelations(delivery);
            List<DeliveryDetail> savedForBarcode = deliveryDetailMapper.selectDeliveryDetailListByDeliveryId(delivery.getDeliveryId());
            scmBarcodeSeedService.createZsDeliveryDetailBarcodesIfNeeded(delivery, savedForBarcode);
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
    public void assertDeliveryEditable(Long deliveryId)
    {
        if (deliveryId == null)
        {
            throw new ServiceException("配送单ID不能为空");
        }
        Delivery existing = deliveryMapper.selectDeliveryById(deliveryId);
        assertDeliveryNotAudited(existing, "修改");
    }

    @Override
    @Transactional
    public int updateDelivery(Delivery delivery)
    {
        assertDeliveryEditable(delivery.getDeliveryId());

        delivery.setUpdateTime(DateUtils.getNowDate());
        enrichDeliverySnapshot(delivery);
        enrichDeliveryDetailPackCoefficients(delivery);
        validateDeliveryDetailPackQuantities(delivery.getDeliveryDetails());
        
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
            Delivery d = deliveryMapper.selectDeliveryById(Long.parseLong(deliveryId));
            assertDeliveryDeletable(d);
        }
        for (String deliveryId : deliveryIds)
        {
            scmOrderDetailDeliveryRelMapper.deleteByDeliveryId(deliveryId);
            zsTpOrderDetailDeliveryRelMapper.deleteByDeliveryId(deliveryId);
            scmBarcodeSeedService.deleteBarcodesByDeliveryId(Long.parseLong(deliveryId));
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
        Delivery d = deliveryMapper.selectDeliveryById(deliveryId);
        assertDeliveryDeletable(d);
        String did = String.valueOf(deliveryId);
        scmOrderDetailDeliveryRelMapper.deleteByDeliveryId(did);
        zsTpOrderDetailDeliveryRelMapper.deleteByDeliveryId(did);
        scmBarcodeSeedService.deleteBarcodesByDeliveryId(deliveryId);
        deliveryDetailMapper.deleteDeliveryDetailByDeliveryId(deliveryId);
        return deliveryMapper.deleteDeliveryById(deliveryId);
    }

    private void assertDeliveryDeletable(Delivery d)
    {
        assertDeliveryNotAudited(d, "删除");
    }

    /**
     * 前端按订单行/中设行校验「申请数量」上限：补全 lineApplyQty（不落库，仅展示与校验用）
     */
    private void enrichDetailLineApplyQty(List<DeliveryDetail> details)
    {
        if (details == null || details.isEmpty())
        {
            return;
        }
        for (DeliveryDetail dd : details)
        {
            if (dd == null || dd.getLineApplyQty() != null)
            {
                continue;
            }
            if (dd.getRefOrderLineQty() != null)
            {
                dd.setLineApplyQty(dd.getRefOrderLineQty());
            }
            else if (dd.getRefZsLineQty() != null)
            {
                dd.setLineApplyQty(dd.getRefZsLineQty());
            }
        }
    }

    /**
     * 已审核（含兼容旧数据：仅单据状态为已审核）则不允许变更。
     */
    private void assertDeliveryNotAudited(Delivery d, String actionLabel)
    {
        if (d == null)
        {
            throw new ServiceException("配送单不存在");
        }
        if ("1".equals(d.getAuditStatus()))
        {
            throw new ServiceException("已审核的配送单不允许" + actionLabel + "，配送单号：" + StringUtils.trimToEmpty(d.getDeliveryNo()));
        }
        if (StringUtils.isEmpty(d.getAuditStatus()) && "1".equals(d.getDeliveryStatus()))
        {
            throw new ServiceException("已审核的配送单不允许" + actionLabel + "，配送单号：" + StringUtils.trimToEmpty(d.getDeliveryNo()));
        }
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
    public ZsTpOrder selectZsTpOrderById(String id)
    {
        return zsTpOrderMapper.selectZsTpOrderById(id);
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
        vo.setZsCustomerId(StringUtils.trimToEmpty(head.getCustomer()));
        vo.setSrcOrderSupplierId(StringUtils.trimToEmpty(head.getSupno()));
        vo.setSrcOrderSupplierName(StringUtils.trimToEmpty(head.getSup()));
        vo.setSrcOrderWarehouseId(StringUtils.trimToEmpty(head.getCkno()));
        vo.setSrcOrderWarehouseName(StringUtils.trimToEmpty(head.getCk()));
        vo.setSrcOrderDeptId(StringUtils.trimToEmpty(head.getKsbh()));
        vo.setSrcOrderDeptName(StringUtils.trimToEmpty(head.getKsmc()));

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
        if (line.getDsb() != null && line.getDsb().compareTo(BigDecimal.ZERO) > 0)
        {
            d.setPackCoefficient(line.getDsb());
        }
        d.setLineApplyQty(sl);
        return d;
    }

    /**
     * 引用本系统订单时，若前端未带打包系数，则按订单明细行补全。
     */
    private void enrichDeliveryDetailPackCoefficients(Delivery delivery)
    {
        if (delivery == null || delivery.getDeliveryDetails() == null || delivery.getDeliveryDetails().isEmpty())
        {
            return;
        }
        if (StringUtils.isNotEmpty(delivery.getZsOrderId()))
        {
            List<ZsTpOrderDetail> lines = zsTpOrderMapper.selectZsTpOrderDetailListByOrderId(delivery.getZsOrderId());
            Map<String, ZsTpOrderDetail> byId = new HashMap<>();
            if (lines != null)
            {
                for (ZsTpOrderDetail line : lines)
                {
                    if (line != null && StringUtils.isNotEmpty(line.getId()))
                    {
                        byId.put(line.getId(), line);
                    }
                }
            }
            for (DeliveryDetail d : delivery.getDeliveryDetails())
            {
                if (d.getPackCoefficient() != null)
                {
                    continue;
                }
                if (StringUtils.isEmpty(d.getZsOrderDetailId()))
                {
                    continue;
                }
                ZsTpOrderDetail line = byId.get(d.getZsOrderDetailId());
                if (line != null && line.getDsb() != null && line.getDsb().compareTo(BigDecimal.ZERO) > 0)
                {
                    d.setPackCoefficient(line.getDsb());
                }
            }
            return;
        }
        if (delivery.getOrderId() == null)
        {
            return;
        }
        for (DeliveryDetail d : delivery.getDeliveryDetails())
        {
            if (d.getPackCoefficient() != null)
            {
                continue;
            }
            if (d.getOrderDetailId() == null)
            {
                continue;
            }
            OrderDetail od = orderDetailMapper.selectOrderDetailById(d.getOrderDetailId());
            if (od != null && od.getPackCoefficient() != null)
            {
                d.setPackCoefficient(od.getPackCoefficient());
            }
        }
    }

    /**
     * 打包系数为正数时，配送数量须为其整数倍。
     */
    private void validateDeliveryDetailPackQuantities(List<DeliveryDetail> details)
    {
        if (details == null || details.isEmpty())
        {
            return;
        }
        int row = 1;
        for (DeliveryDetail d : details)
        {
            BigDecimal coeff = d.getPackCoefficient();
            if (coeff == null || coeff.compareTo(BigDecimal.ZERO) <= 0)
            {
                row++;
                continue;
            }
            BigDecimal qty = d.getDeliveryQuantity();
            if (qty == null)
            {
                row++;
                continue;
            }
            BigDecimal rem = qty.remainder(coeff);
            if (rem.compareTo(BigDecimal.ZERO) != 0)
            {
                throw new ServiceException(String.format("第%d行：配送数量必须是打包系数(%s)的整数倍，当前数量：%s",
                    row,
                    coeff.stripTrailingZeros().toPlainString(),
                    qty.stripTrailingZeros().toPlainString()));
            }
            row++;
        }
    }

    /**
     * 保存前从关联的中设订单或本系统订单补全订单侧快照字段（字符串），便于客户端按配送单入库引用。
     */
    private void enrichDeliverySnapshot(Delivery d)
    {
        if (d == null)
        {
            return;
        }
        if (StringUtils.isEmpty(d.getZsOrderId()))
        {
            d.setZsJsfs(null);
        }
        if (StringUtils.isNotEmpty(d.getZsOrderId()))
        {
            ZsTpOrder z = zsTpOrderMapper.selectZsTpOrderById(d.getZsOrderId());
            if (z != null)
            {
                if (StringUtils.isEmpty(d.getZsCustomerId()))
                {
                    d.setZsCustomerId(StringUtils.trimToEmpty(z.getCustomer()));
                }
                if (StringUtils.isEmpty(d.getSrcOrderSupplierId()))
                {
                    d.setSrcOrderSupplierId(StringUtils.trimToEmpty(z.getSupno()));
                }
                if (StringUtils.isEmpty(d.getSrcOrderSupplierName()))
                {
                    d.setSrcOrderSupplierName(StringUtils.trimToEmpty(z.getSup()));
                }
                if (StringUtils.isEmpty(d.getSrcOrderWarehouseId()))
                {
                    d.setSrcOrderWarehouseId(StringUtils.trimToEmpty(z.getCkno()));
                }
                if (StringUtils.isEmpty(d.getSrcOrderWarehouseName()))
                {
                    d.setSrcOrderWarehouseName(StringUtils.trimToEmpty(z.getCk()));
                }
                if (StringUtils.isEmpty(d.getSrcOrderDeptId()))
                {
                    d.setSrcOrderDeptId(StringUtils.trimToEmpty(z.getKsbh()));
                }
                if (StringUtils.isEmpty(d.getSrcOrderDeptName()))
                {
                    d.setSrcOrderDeptName(StringUtils.trimToEmpty(z.getKsmc()));
                }
                if (d.getOrderDate() == null && z.getCreateTime() != null)
                {
                    d.setOrderDate(z.getCreateTime());
                }
                if (StringUtils.isEmpty(d.getWarehouse()) && StringUtils.isNotEmpty(z.getCk()))
                {
                    d.setWarehouse(StringUtils.trimToEmpty(z.getCk()));
                }
                d.setZsJsfs(StringUtils.trimToEmpty(z.getJsfs()));
            }
        }
        else if (d.getOrderId() != null)
        {
            d.setZsJsfs(null);
            Order o = orderMapper.selectOrderById(d.getOrderId());
            if (o != null)
            {
                if (StringUtils.isEmpty(d.getZsCustomerId()))
                {
                    d.setZsCustomerId("");
                }
                if (StringUtils.isEmpty(d.getSrcOrderSupplierId()) && o.getSupplierId() != null)
                {
                    d.setSrcOrderSupplierId(String.valueOf(o.getSupplierId()));
                }
                if (StringUtils.isEmpty(d.getSrcOrderSupplierName()))
                {
                    String name = StringUtils.isNotEmpty(o.getOrderSupplierName()) ? o.getOrderSupplierName()
                        : o.getSupplierName();
                    d.setSrcOrderSupplierName(StringUtils.trimToEmpty(name));
                }
                if (StringUtils.isEmpty(d.getSrcOrderWarehouseId()) && o.getWarehouseId() != null)
                {
                    d.setSrcOrderWarehouseId(String.valueOf(o.getWarehouseId()));
                }
                if (StringUtils.isEmpty(d.getSrcOrderWarehouseName()))
                {
                    d.setSrcOrderWarehouseName(StringUtils.trimToEmpty(o.getWarehouse()));
                }
                if (StringUtils.isEmpty(d.getSrcOrderDeptId()) && o.getOrderDeptId() != null)
                {
                    d.setSrcOrderDeptId(String.valueOf(o.getOrderDeptId()));
                }
                if (StringUtils.isEmpty(d.getSrcOrderDeptName()))
                {
                    String dn = StringUtils.isNotEmpty(o.getOrderDeptName()) ? o.getOrderDeptName()
                        : o.getDepartment();
                    d.setSrcOrderDeptName(StringUtils.trimToEmpty(dn));
                }
                if (d.getOrderDate() == null && o.getOrderDate() != null)
                {
                    d.setOrderDate(o.getOrderDate());
                }
                if (StringUtils.isEmpty(d.getWarehouse()) && StringUtils.isNotEmpty(o.getWarehouse()))
                {
                    d.setWarehouse(StringUtils.trimToEmpty(o.getWarehouse()));
                }
            }
        }
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
    public int auditDelivery(Long deliveryId, String auditBy)
    {
        Delivery delivery = deliveryMapper.selectDeliveryById(deliveryId);
        if (delivery == null)
        {
            return 0;
        }
        if ("1".equals(delivery.getAuditStatus()))
        {
            throw new ServiceException("配送单已审核，请勿重复审核");
        }
        delivery.setAuditStatus("1");
        delivery.setAuditBy(StringUtils.trimToEmpty(auditBy));
        delivery.setAuditTime(DateUtils.getNowDate());
        delivery.setDeliveryStatus("1"); // 单据状态：已审核
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

    /**
     * 写入订单明细与配送单明细关联（支持同一订单多次配送）
     */
    private void insertOrderDeliveryDetailRelations(Delivery delivery)
    {
        if (delivery.getDeliveryId() == null)
        {
            return;
        }
        List<DeliveryDetail> saved = deliveryDetailMapper.selectDeliveryDetailListByDeliveryId(delivery.getDeliveryId());
        if (saved == null || saved.isEmpty())
        {
            return;
        }
        String timeStr = DateUtils.getTime();
        String createBy = StringUtils.trimToEmpty(delivery.getCreateBy());
        String tenantId = delivery.getTenantId();
        String deliveryIdStr = String.valueOf(delivery.getDeliveryId());
        String deliveryNo = StringUtils.trimToEmpty(delivery.getDeliveryNo());

        if (StringUtils.isNotEmpty(delivery.getZsOrderId()))
        {
            List<ZsTpOrderDetailDeliveryRel> rels = new ArrayList<>();
            String zsOrderId = delivery.getZsOrderId();
            String orderNo = StringUtils.trimToEmpty(delivery.getOrderNo());
            for (DeliveryDetail dd : saved)
            {
                if (StringUtils.isEmpty(dd.getZsOrderDetailId()) || dd.getDetailId() == null)
                {
                    continue;
                }
                ZsTpOrderDetailDeliveryRel r = new ZsTpOrderDetailDeliveryRel();
                r.setId(IdUtils.simpleUuid7());
                r.setOrderDetailId(dd.getZsOrderDetailId());
                r.setOrderId(zsOrderId);
                r.setOrderNo(orderNo);
                r.setDeliveryId(deliveryIdStr);
                r.setDeliveryNo(deliveryNo);
                r.setDeliveryDetailId(String.valueOf(dd.getDetailId()));
                r.setCreateTime(timeStr);
                r.setCreateBy(createBy);
                r.setTenantId(tenantId);
                rels.add(r);
            }
            if (!rels.isEmpty())
            {
                zsTpOrderDetailDeliveryRelMapper.batchInsert(rels);
            }
            return;
        }

        if (delivery.getOrderId() != null)
        {
            List<ScmOrderDetailDeliveryRel> rels = new ArrayList<>();
            String oid = String.valueOf(delivery.getOrderId());
            String orderNo = StringUtils.trimToEmpty(delivery.getOrderNo());
            for (DeliveryDetail dd : saved)
            {
                if (dd.getOrderDetailId() == null || dd.getDetailId() == null)
                {
                    continue;
                }
                ScmOrderDetailDeliveryRel r = new ScmOrderDetailDeliveryRel();
                r.setId(IdUtils.simpleUuid7());
                r.setOrderDetailId(String.valueOf(dd.getOrderDetailId()));
                r.setOrderId(oid);
                r.setOrderNo(orderNo);
                r.setDeliveryId(deliveryIdStr);
                r.setDeliveryNo(deliveryNo);
                r.setDeliveryDetailId(String.valueOf(dd.getDetailId()));
                r.setCreateTime(timeStr);
                r.setCreateBy(createBy);
                r.setTenantId(tenantId);
                rels.add(r);
            }
            if (!rels.isEmpty())
            {
                scmOrderDetailDeliveryRelMapper.batchInsert(rels);
            }
        }
    }

    @Override
    public List<ZsTpOrderDetail> selectZsTpOrderDetailListForView(String zsOrderId)
    {
        if (StringUtils.isEmpty(zsOrderId))
        {
            return new ArrayList<>();
        }
        List<ZsTpOrderDetail> list = zsTpOrderMapper.selectZsTpOrderDetailListByOrderId(zsOrderId);
        enrichZsOrderDetailsDeliveryQty(list, zsOrderId);
        return list;
    }

    private void enrichZsOrderDetailsDeliveryQty(List<ZsTpOrderDetail> list, String zsOrderId)
    {
        if (list == null || list.isEmpty() || StringUtils.isEmpty(zsOrderId))
        {
            return;
        }
        List<OrderLineDeliveryQtyVo> agg = orderDeliveryTraceMapper.selectZsOrderLineDeliveryQtyByZsOrderId(zsOrderId);
        Map<String, OrderLineDeliveryQtyVo> map = new HashMap<>();
        for (OrderLineDeliveryQtyVo row : agg)
        {
            if (row != null && StringUtils.isNotEmpty(row.getLineKey()))
            {
                map.put(row.getLineKey(), row);
            }
        }
        for (ZsTpOrderDetail od : list)
        {
            String key = od.getId();
            OrderLineDeliveryQtyVo q = key == null ? null : map.get(key);
            BigDecimal oq = od.getSl() != null ? od.getSl() : BigDecimal.ZERO;
            BigDecimal a = (q != null && q.getAuditedQty() != null) ? q.getAuditedQty() : BigDecimal.ZERO;
            BigDecimal p = (q != null && q.getPendingQty() != null) ? q.getPendingQty() : BigDecimal.ZERO;
            BigDecimal rj = (q != null && q.getRejectedQty() != null) ? q.getRejectedQty() : BigDecimal.ZERO;
            od.setDeliveredAuditedQty(a);
            od.setDeliveredPendingAuditQty(p);
            BigDecimal und = oq.subtract(a).subtract(p).subtract(rj);
            if (und.compareTo(BigDecimal.ZERO) < 0)
            {
                und = BigDecimal.ZERO;
            }
            od.setUndeliveredQty(und);
        }
    }

    @Override
    public List<Delivery> selectDeliveriesByOrderId(Long orderId)
    {
        if (orderId == null)
        {
            return new ArrayList<>();
        }
        return orderDeliveryTraceMapper.selectDeliveriesByOrderId(orderId);
    }

    @Override
    public List<Delivery> selectDeliveriesByZsOrderId(String zsOrderId)
    {
        if (StringUtils.isEmpty(zsOrderId))
        {
            return new ArrayList<>();
        }
        return orderDeliveryTraceMapper.selectDeliveriesByZsOrderId(zsOrderId);
    }

    @Override
    public List<OrderDetailDeliveryTraceVo> selectTracesByScmOrderDetailId(Long orderDetailId)
    {
        if (orderDetailId == null)
        {
            return new ArrayList<>();
        }
        return orderDeliveryTraceMapper.selectTracesByScmOrderDetailId(orderDetailId);
    }

    @Override
    public List<OrderDetailDeliveryTraceVo> selectTracesByZsOrderDetailId(String zsOrderDetailId)
    {
        if (StringUtils.isEmpty(zsOrderDetailId))
        {
            return new ArrayList<>();
        }
        return orderDeliveryTraceMapper.selectTracesByZsOrderDetailId(zsOrderDetailId);
    }
}

