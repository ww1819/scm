package com.scm.system.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.scm.common.constant.DeliveryRefOrderSource;
import com.scm.common.core.text.Convert;
import com.scm.common.exception.ServiceException;
import com.scm.common.utils.DateUtils;
import com.scm.common.utils.MoneyPrecisionUtils;
import com.scm.common.utils.ShiroUtils;
import com.scm.common.utils.StringUtils;
import com.scm.common.utils.uuid.IdUtils;
import com.scm.system.domain.CombinedDelivery;
import com.scm.system.domain.CombinedDeliveryDetail;
import com.scm.system.domain.Delivery;
import com.scm.system.domain.DeliveryDetail;
import com.scm.system.domain.DeliveryLineOpLog;
import com.scm.system.domain.Hospital;
import com.scm.system.domain.Order;
import com.scm.system.domain.OrderDetail;
import com.scm.system.domain.vo.CombinedOrderProgressVo;
import com.scm.system.domain.vo.OrderLineDeliveryQtyVo;
import com.scm.system.mapper.CombinedDeliveryMapper;
import com.scm.system.mapper.DeliveryMapper;
import com.scm.system.mapper.OrderDetailMapper;
import com.scm.system.mapper.OrderMapper;
import com.scm.system.service.ICombinedDeliveryService;
import com.scm.system.service.IDeliveryLineOpLogService;
import com.scm.system.service.IDeliveryService;
import com.scm.system.service.IHospitalService;
import com.scm.system.service.IScmHospitalContextService;
import com.scm.system.service.IScmSupplierContextService;

@Service
public class CombinedDeliveryServiceImpl implements ICombinedDeliveryService
{
    @Autowired
    private CombinedDeliveryMapper combinedDeliveryMapper;
    @Autowired
    private DeliveryMapper deliveryMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private com.scm.system.mapper.OrderDeliveryTraceMapper orderDeliveryTraceMapper;
    @Autowired
    private IDeliveryService deliveryService;
    @Autowired
    private IDeliveryLineOpLogService deliveryLineOpLogService;
    @Autowired
    private IScmSupplierContextService scmSupplierContextService;
    @Autowired
    private IScmHospitalContextService scmHospitalContextService;
    @Autowired
    private IHospitalService hospitalService;

    @Override
    public CombinedDelivery selectById(String combinedId)
    {
        CombinedDelivery row = combinedDeliveryMapper.selectCombinedDeliveryById(combinedId);
        if (row == null)
        {
            return null;
        }
        assertScope(row);
        row.setDetails(combinedDeliveryMapper.selectDetailsByCombinedId(combinedId));
        return row;
    }

    @Override
    public List<CombinedDelivery> selectList(CombinedDelivery query)
    {
        applyScope(query);
        return combinedDeliveryMapper.selectCombinedDeliveryList(query);
    }

    @Override
    @Transactional
    public int insert(CombinedDelivery row)
    {
        applyScope(row);
        validateHeaderAndLines(row, null);
        if (StringUtils.isEmpty(row.getCombinedNo()))
        {
            row.setCombinedNo(generateCombinedNo());
        }
        if (StringUtils.isEmpty(row.getCombinedId()))
        {
            row.setCombinedId(IdUtils.simpleUuid7());
        }
        row.setAuditStatus("0");
        row.setDeliveryStatus("0");
        row.setCreateBy(ShiroUtils.getLoginName());
        row.setCreateTime(DateUtils.getNowDate());
        recalcAmount(row);
        int n = combinedDeliveryMapper.insertCombinedDelivery(row);
        persistDetails(row, true);
        recalcAmount(row);
        combinedDeliveryMapper.updateCombinedDelivery(row);
        return n;
    }

    @Override
    @Transactional
    public int update(CombinedDelivery row)
    {
        CombinedDelivery existing = combinedDeliveryMapper.selectCombinedDeliveryById(row.getCombinedId());
        if (existing == null)
        {
            throw new ServiceException("合单不存在");
        }
        if ("1".equals(existing.getAuditStatus()))
        {
            throw new ServiceException("已审核合单不可修改");
        }
        assertScope(existing);
        row.setHospitalId(existing.getHospitalId());
        row.setWarehouse(existing.getWarehouse());
        row.setDeliveryAddress(existing.getDeliveryAddress());
        row.setSupplierId(existing.getSupplierId());
        validateHeaderAndLines(row, row.getCombinedId());
        row.setUpdateBy(ShiroUtils.getLoginName());
        recalcAmount(row);
        int n = combinedDeliveryMapper.updateCombinedDelivery(row);
        persistDetails(row, false);
        recalcAmount(row);
        combinedDeliveryMapper.updateCombinedDelivery(row);
        return n;
    }

    @Override
    @Transactional
    public int deleteByIds(String ids)
    {
        String[] arr = Convert.toStrArray(ids);
        String delBy = ShiroUtils.getLoginName();
        for (String id : arr)
        {
            CombinedDelivery existing = combinedDeliveryMapper.selectCombinedDeliveryById(id);
            if (existing == null)
            {
                continue;
            }
            if ("1".equals(existing.getAuditStatus()))
            {
                throw new ServiceException("已审核合单不可删除：" + existing.getCombinedNo());
            }
            assertScope(existing);
            List<CombinedDeliveryDetail> details = combinedDeliveryMapper.selectDetailsByCombinedId(existing.getCombinedId());
            for (CombinedDeliveryDetail d : details)
            {
                deliveryLineOpLogService.logCombinedLine(DeliveryLineOpLog.ACTION_DELETE, existing.getCombinedId(),
                    existing.getCombinedNo(), d, null, null, null, delBy);
            }
            combinedDeliveryMapper.deleteDetailsByCombinedId(existing.getCombinedId(), delBy);
        }
        return combinedDeliveryMapper.deleteCombinedDeliveryByIds(arr, delBy);
    }

    @Override
    @Transactional
    public int audit(String combinedId, String auditBy)
    {
        CombinedDelivery bill = selectById(combinedId);
        if (bill == null)
        {
            throw new ServiceException("合单不存在");
        }
        if ("1".equals(bill.getAuditStatus()))
        {
            throw new ServiceException("合单已审核");
        }
        List<CombinedDeliveryDetail> details = bill.getDetails();
        if (details == null || details.isEmpty())
        {
            throw new ServiceException("合单明细不能为空");
        }
        validateHeaderAndLines(bill, combinedId);
        Map<Long, List<CombinedDeliveryDetail>> byOrder = new LinkedHashMap<>();
        for (CombinedDeliveryDetail d : details)
        {
            byOrder.computeIfAbsent(d.getOrderId(), k -> new ArrayList<>()).add(d);
        }
        for (Map.Entry<Long, List<CombinedDeliveryDetail>> e : byOrder.entrySet())
        {
            splitOneOrder(bill, e.getKey(), e.getValue(), auditBy);
        }
        bill.setAuditStatus("1");
        bill.setDeliveryStatus("1");
        bill.setAuditBy(auditBy);
        bill.setAuditTime(DateUtils.getNowDate());
        bill.setUpdateBy(auditBy);
        return combinedDeliveryMapper.updateCombinedDelivery(bill);
    }

    @Override
    public List<CombinedDeliveryDetail> selectCandidateLines(CombinedDelivery query)
    {
        applyScope(query);
        return combinedDeliveryMapper.selectCandidateOrderLines(query);
    }

    @Override
    public List<CombinedOrderProgressVo> selectOrderProgress(CombinedDelivery query)
    {
        applyScope(query);
        List<CombinedOrderProgressVo> list = combinedDeliveryMapper.selectCombinedOrderProgress(query.getSupplierId(),
            query.getHospitalId());
        if (list == null)
        {
            return new ArrayList<>();
        }
        for (CombinedOrderProgressVo vo : list)
        {
            BigDecimal oq = vo.getOrderQty() != null ? vo.getOrderQty() : BigDecimal.ZERO;
            BigDecimal dq = vo.getDeliveredQty() != null ? vo.getDeliveredQty() : BigDecimal.ZERO;
            if (dq.compareTo(BigDecimal.ZERO) <= 0)
            {
                vo.setProgressStatus("未配送");
            }
            else if (dq.compareTo(oq) >= 0)
            {
                vo.setProgressStatus("已配完");
            }
            else
            {
                vo.setProgressStatus("部分配送");
            }
        }
        return list;
    }

    @Override
    public int countCombinedOccupancy(Long orderId, String excludeCombinedId)
    {
        if (orderId == null)
        {
            return 0;
        }
        return combinedDeliveryMapper.countCombinedDetailByOrderId(orderId, excludeCombinedId);
    }

    private void splitOneOrder(CombinedDelivery bill, Long orderId, List<CombinedDeliveryDetail> lines, String auditBy)
    {
        Order order = orderMapper.selectOrderById(orderId);
        if (order == null)
        {
            throw new ServiceException("订单不存在：" + orderId);
        }
        Delivery delivery = new Delivery();
        delivery.setCombinedId(bill.getCombinedId());
        delivery.setCombinedNo(bill.getCombinedNo());
        delivery.setHospitalId(bill.getHospitalId());
        delivery.setWarehouse(bill.getWarehouse());
        delivery.setDeliveryAddress(bill.getDeliveryAddress());
        delivery.setSupplierId(bill.getSupplierId());
        delivery.setSpdSupplierId(bill.getSpdSupplierId());
        delivery.setSpdTenantId(bill.getSpdTenantId());
        delivery.setOrderId(orderId);
        delivery.setOrderNo(order.getOrderNo());
        delivery.setRefOrderSource(DeliveryRefOrderSource.SCM);
        delivery.setDeliveryPerson(bill.getDeliveryPerson());
        delivery.setExpectedDeliveryDate(bill.getExpectedDeliveryDate());
        delivery.setCreateBy(auditBy);
        List<DeliveryDetail> dds = new ArrayList<>();
        for (CombinedDeliveryDetail line : lines)
        {
            DeliveryDetail dd = new DeliveryDetail();
            dd.setCombinedId(bill.getCombinedId());
            dd.setCombinedNo(bill.getCombinedNo());
            dd.setCombinedDetailId(line.getDetailId());
            dd.setOrderDetailId(line.getOrderDetailId());
            dd.setMaterialId(line.getMaterialId());
            dd.setMaterialCode(line.getMaterialCode());
            dd.setMaterialName(line.getMaterialName());
            dd.setSpecification(line.getSpecification());
            dd.setModel(line.getModel());
            dd.setUnit(line.getUnit());
            dd.setDeliveryQuantity(line.getDeliveryQuantity());
            dd.setPrice(line.getPrice());
            dd.setAmount(line.getAmount());
            dd.setPackCoefficient(line.getPackCoefficient());
            dd.setBatchNo(line.getBatchNo());
            dd.setProductionDate(line.getProductionDate());
            dd.setExpireDate(line.getExpireDate());
            dd.setManufacturer(line.getManufacturer());
            dd.setRegisterNo(line.getRegisterNo());
            dds.add(dd);
        }
        delivery.setDeliveryDetails(dds);
        deliveryService.insertDelivery(delivery);
        List<DeliveryDetail> saved = delivery.getDeliveryDetails();
        if (saved == null || saved.isEmpty())
        {
            Delivery persisted = deliveryService.selectDeliveryById(delivery.getDeliveryId());
            saved = persisted != null ? persisted.getDeliveryDetails() : dds;
        }
        Map<String, DeliveryDetail> byCombinedDetail = new LinkedHashMap<>();
        if (saved != null)
        {
            for (DeliveryDetail s : saved)
            {
                if (StringUtils.isNotEmpty(s.getCombinedDetailId()))
                {
                    byCombinedDetail.put(s.getCombinedDetailId(), s);
                }
            }
        }
        for (CombinedDeliveryDetail line : lines)
        {
            DeliveryDetail s = byCombinedDetail.get(line.getDetailId());
            if (s == null)
            {
                continue;
            }
            CombinedDeliveryDetail upd = new CombinedDeliveryDetail();
            upd.setDetailId(line.getDetailId());
            upd.setDeliveryId(delivery.getDeliveryId());
            upd.setDeliveryNo(delivery.getDeliveryNo());
            upd.setDeliveryDetailId(s.getDetailId());
            upd.setUpdateBy(auditBy);
            combinedDeliveryMapper.updateCombinedDeliveryDetail(upd);
        }
    }

    private void persistDetails(CombinedDelivery row, boolean isNew)
    {
        String oper = ShiroUtils.getLoginName();
        List<CombinedDeliveryDetail> incoming = row.getDetails();
        if (incoming == null || incoming.isEmpty())
        {
            throw new ServiceException("合单明细不能为空");
        }
        List<CombinedDeliveryDetail> olds = isNew ? new ArrayList<>()
            : combinedDeliveryMapper.selectDetailsByCombinedId(row.getCombinedId());
        Map<String, CombinedDeliveryDetail> oldById = new LinkedHashMap<>();
        for (CombinedDeliveryDetail o : olds)
        {
            oldById.put(o.getDetailId(), o);
        }
        java.util.Set<String> kept = new java.util.HashSet<>();
        for (CombinedDeliveryDetail n : incoming)
        {
            n.setCombinedId(row.getCombinedId());
            n.setCreateBy(oper);
            n.setTenantId(row.getTenantId());
            fillLineFromOrder(n);
            calcLineAmount(n);
            if (StringUtils.isNotEmpty(n.getDetailId()) && oldById.containsKey(n.getDetailId()))
            {
                kept.add(n.getDetailId());
                CombinedDeliveryDetail before = oldById.get(n.getDetailId());
                n.setUpdateBy(oper);
                combinedDeliveryMapper.updateCombinedDeliveryDetail(n);
                deliveryLineOpLogService.logCombinedLine(DeliveryLineOpLog.ACTION_UPDATE, row.getCombinedId(),
                    row.getCombinedNo(), before, n, null, null, oper);
            }
            else
            {
                n.setDetailId(IdUtils.simpleUuid7());
                combinedDeliveryMapper.insertCombinedDeliveryDetail(n);
                deliveryLineOpLogService.logCombinedLine(DeliveryLineOpLog.ACTION_INSERT, row.getCombinedId(),
                    row.getCombinedNo(), null, n, null, null, oper);
            }
        }
        for (CombinedDeliveryDetail o : olds)
        {
            if (!kept.contains(o.getDetailId()))
            {
                combinedDeliveryMapper.deleteDetailById(o.getDetailId(), oper);
                deliveryLineOpLogService.logCombinedLine(DeliveryLineOpLog.ACTION_DELETE, row.getCombinedId(),
                    row.getCombinedNo(), o, null, null, null, oper);
            }
        }
    }

    private void validateHeaderAndLines(CombinedDelivery row, String excludeCombinedId)
    {
        if (row.getDetails() == null || row.getDetails().isEmpty())
        {
            throw new ServiceException("请选择未配送或部分配送的订单明细");
        }
        CombinedDeliveryDetail first = row.getDetails().get(0);
        fillLineFromOrder(first);
        Order firstOrder = orderMapper.selectOrderById(first.getOrderId());
        if (firstOrder == null)
        {
            throw new ServiceException("订单不存在");
        }
        if (row.getHospitalId() == null)
        {
            row.setHospitalId(firstOrder.getHospitalId());
        }
        if (StringUtils.isEmpty(row.getWarehouse()))
        {
            row.setWarehouse(firstOrder.getWarehouse());
        }
        if (StringUtils.isEmpty(row.getDeliveryAddress()) && row.getHospitalId() != null)
        {
            Hospital h = hospitalService.selectHospitalById(row.getHospitalId());
            if (h != null)
            {
                row.setDeliveryAddress(StringUtils.trimToEmpty(h.getAddress()));
            }
        }
        if (row.getSupplierId() == null)
        {
            row.setSupplierId(firstOrder.getSupplierId());
        }
        if (StringUtils.isEmpty(row.getSpdSupplierId()))
        {
            row.setSpdSupplierId(firstOrder.getSpdSupplierId());
        }
        Map<Long, BigDecimal> sumByOrderDetail = new HashMap<>();
        for (CombinedDeliveryDetail line : row.getDetails())
        {
            fillLineFromOrder(line);
            Order od = orderMapper.selectOrderById(line.getOrderId());
            if (od == null)
            {
                throw new ServiceException("订单不存在：" + line.getOrderId());
            }
            if (!row.getHospitalId().equals(od.getHospitalId()))
            {
                throw new ServiceException("不能混入其他医院订单：" + od.getOrderNo());
            }
            if (!StringUtils.trimToEmpty(row.getWarehouse()).equals(StringUtils.trimToEmpty(od.getWarehouse())))
            {
                throw new ServiceException("不能混入其他仓库订单：" + od.getOrderNo());
            }
            assertNotRegularOccupied(line.getOrderId());
            if (combinedDeliveryMapper.countCombinedDetailByOrderId(line.getOrderId(), excludeCombinedId) > 0)
            {
                throw new ServiceException("订单已在其他合单中占用：" + od.getOrderNo());
            }
            if (line.getDeliveryQuantity() == null || line.getDeliveryQuantity().compareTo(BigDecimal.ZERO) <= 0)
            {
                throw new ServiceException("配送数量必须大于0：" + line.getMaterialName());
            }
            sumByOrderDetail.merge(line.getOrderDetailId(), line.getDeliveryQuantity(), BigDecimal::add);
        }
        for (Map.Entry<Long, BigDecimal> e : sumByOrderDetail.entrySet())
        {
            BigDecimal cap = remainingCap(e.getKey(), excludeCombinedId);
            if (e.getValue().compareTo(cap) > 0)
            {
                OrderDetail od = orderDetailMapper.selectOrderDetailById(e.getKey());
                String name = od != null ? StringUtils.trimToEmpty(od.getMaterialName()) : String.valueOf(e.getKey());
                throw new ServiceException(String.format("可配送数量超限【%s】：本单合计 %s，最多 %s",
                    name,
                    e.getValue().stripTrailingZeros().toPlainString(),
                    cap.stripTrailingZeros().toPlainString()));
            }
        }
    }

    private void assertNotRegularOccupied(Long orderId)
    {
        int n = deliveryMapper.countRegularDeliveryByOrderId(orderId);
        if (n > 0)
        {
            throw new ServiceException("订单已走普通配送，不能进入合单：" + orderId);
        }
    }

    private BigDecimal remainingCap(Long orderDetailId, String excludeCombinedId)
    {
        OrderDetail od = orderDetailMapper.selectOrderDetailById(orderDetailId);
        if (od == null)
        {
            throw new ServiceException("订单明细不存在：" + orderDetailId);
        }
        BigDecimal oq = od.getOrderQuantity() == null ? BigDecimal.ZERO : BigDecimal.valueOf(od.getOrderQuantity().longValue());
        List<OrderLineDeliveryQtyVo> agg = combinedDeliveryMapper.selectCombinedPendingQtyByOrderId(od.getOrderId(),
            excludeCombinedId);
        BigDecimal pendingCombined = BigDecimal.ZERO;
        if (agg != null)
        {
            for (OrderLineDeliveryQtyVo v : agg)
            {
                if (v != null && String.valueOf(orderDetailId).equals(v.getLineKey()) && v.getPendingQty() != null)
                {
                    pendingCombined = v.getPendingQty();
                }
            }
        }
        BigDecimal usedDelivery = BigDecimal.ZERO;
        List<OrderLineDeliveryQtyVo> delAgg = orderDeliveryTraceMapper.selectScmOrderLineDeliveryQtyByOrderId(od.getOrderId());
        if (delAgg != null)
        {
            for (OrderLineDeliveryQtyVo v : delAgg)
            {
                if (v != null && String.valueOf(orderDetailId).equals(v.getLineKey()))
                {
                    BigDecimal a = v.getAuditedQty() != null ? v.getAuditedQty() : BigDecimal.ZERO;
                    BigDecimal p = v.getPendingQty() != null ? v.getPendingQty() : BigDecimal.ZERO;
                    BigDecimal r = v.getRejectedQty() != null ? v.getRejectedQty() : BigDecimal.ZERO;
                    usedDelivery = a.add(p).add(r);
                }
            }
        }
        BigDecimal cap = oq.subtract(usedDelivery).subtract(pendingCombined);
        return cap.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : cap;
    }

    private void fillLineFromOrder(CombinedDeliveryDetail line)
    {
        if (line.getOrderDetailId() == null)
        {
            throw new ServiceException("订单明细ID不能为空");
        }
        OrderDetail od = orderDetailMapper.selectOrderDetailById(line.getOrderDetailId());
        if (od == null)
        {
            throw new ServiceException("订单明细不存在：" + line.getOrderDetailId());
        }
        line.setOrderId(od.getOrderId());
        Order order = orderMapper.selectOrderById(od.getOrderId());
        if (order != null)
        {
            line.setOrderNo(order.getOrderNo());
        }
        if (line.getMaterialId() == null)
        {
            line.setMaterialId(od.getMaterialId());
        }
        if (StringUtils.isEmpty(line.getMaterialCode()))
        {
            line.setMaterialCode(od.getMaterialCode());
        }
        if (StringUtils.isEmpty(line.getMaterialName()))
        {
            line.setMaterialName(od.getMaterialName());
        }
        if (StringUtils.isEmpty(line.getSpecification()))
        {
            line.setSpecification(od.getSpecification());
        }
        if (StringUtils.isEmpty(line.getModel()))
        {
            line.setModel(od.getModel());
        }
        if (StringUtils.isEmpty(line.getUnit()))
        {
            line.setUnit(od.getUnit());
        }
        if (line.getPrice() == null)
        {
            line.setPrice(od.getPurchasePrice());
        }
        if (line.getPackCoefficient() == null)
        {
            line.setPackCoefficient(od.getPackCoefficient());
        }
        if (StringUtils.isEmpty(line.getManufacturer()))
        {
            line.setManufacturer(od.getManufacturer());
        }
        if (StringUtils.isEmpty(line.getRegisterNo()))
        {
            line.setRegisterNo(od.getRegisterNo());
        }
    }

    private void calcLineAmount(CombinedDeliveryDetail n)
    {
        if (n.getPrice() != null && n.getDeliveryQuantity() != null)
        {
            n.setPrice(MoneyPrecisionUtils.preserve(n.getPrice()));
            n.setAmount(MoneyPrecisionUtils.multiplyPreserve(n.getPrice(), n.getDeliveryQuantity()));
        }
    }

    private void recalcAmount(CombinedDelivery row)
    {
        BigDecimal total = BigDecimal.ZERO;
        if (row.getDetails() != null)
        {
            for (CombinedDeliveryDetail d : row.getDetails())
            {
                calcLineAmount(d);
                if (d.getAmount() != null)
                {
                    total = total.add(d.getAmount());
                }
            }
        }
        row.setDeliveryAmount(MoneyPrecisionUtils.preserve(total));
    }

    private void applyScope(CombinedDelivery row)
    {
        if (row == null)
        {
            return;
        }
        Long hospitalCtx = scmHospitalContextService.resolveHospitalIdForUser(ShiroUtils.getUserId());
        if (hospitalCtx != null)
        {
            row.setHospitalId(hospitalCtx);
        }
        Long supplierCtx = scmSupplierContextService.resolveSupplierIdForUser(ShiroUtils.getUserId());
        if (supplierCtx != null)
        {
            row.setSupplierId(supplierCtx);
        }
    }

    private void assertScope(CombinedDelivery row)
    {
        Long hospitalCtx = scmHospitalContextService.resolveHospitalIdForUser(ShiroUtils.getUserId());
        if (hospitalCtx != null && row.getHospitalId() != null && !hospitalCtx.equals(row.getHospitalId()))
        {
            throw new ServiceException("无权查看其他医院合单");
        }
        Long supplierCtx = scmSupplierContextService.resolveSupplierIdForUser(ShiroUtils.getUserId());
        if (supplierCtx != null && row.getSupplierId() != null && !supplierCtx.equals(row.getSupplierId()))
        {
            throw new ServiceException("无权查看其他供应商合单");
        }
    }

    private String generateCombinedNo()
    {
        String code;
        int attempt = 0;
        do
        {
            code = "CB" + System.currentTimeMillis() + (int) (Math.random() * 1000);
            attempt++;
        }
        while (combinedDeliveryMapper.selectCombinedDeliveryByNo(code) != null && attempt < 10);
        if (attempt >= 10)
        {
            code = "CB" + UUID.randomUUID().toString().replace("-", "").substring(0, 18);
        }
        return code;
    }
}
