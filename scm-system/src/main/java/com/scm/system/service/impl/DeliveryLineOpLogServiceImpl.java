package com.scm.system.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSON;
import com.scm.common.utils.DateUtils;
import com.scm.common.utils.ShiroUtils;
import com.scm.common.utils.StringUtils;
import com.scm.common.utils.uuid.IdUtils;
import com.scm.system.domain.CombinedDeliveryDetail;
import com.scm.system.domain.Delivery;
import com.scm.system.domain.DeliveryDetail;
import com.scm.system.domain.DeliveryLineOpLog;
import com.scm.system.mapper.DeliveryLineOpLogMapper;
import com.scm.system.service.IDeliveryLineOpLogService;

@Service
public class DeliveryLineOpLogServiceImpl implements IDeliveryLineOpLogService
{
    private static final SimpleDateFormat DAY = new SimpleDateFormat("yyyy-MM-dd");

    @Autowired
    private DeliveryLineOpLogMapper deliveryLineOpLogMapper;

    @Override
    public void logDeliveryLine(String action, Delivery bill, DeliveryDetail before, DeliveryDetail after, String operName)
    {
        if (bill == null || bill.getDeliveryId() == null)
        {
            return;
        }
        DeliveryDetail src = after != null ? after : before;
        DeliveryLineOpLog log = baseLog(DeliveryLineOpLog.KIND_DELIVERY, String.valueOf(bill.getDeliveryId()),
            bill.getDeliveryNo(), operName);
        log.setAction(action);
        if (src != null)
        {
            log.setDetailId(src.getDetailId() == null ? null : String.valueOf(src.getDetailId()));
            log.setOrderId(bill.getOrderId() == null ? null : String.valueOf(bill.getOrderId()));
            log.setOrderNo(bill.getOrderNo());
            log.setOrderDetailId(src.getOrderDetailId() == null ? null : String.valueOf(src.getOrderDetailId()));
        }
        log.setBeforeJson(before == null ? null : JSON.toJSONString(snapshotDeliveryDetail(before)));
        log.setAfterJson(after == null ? null : JSON.toJSONString(snapshotDeliveryDetail(after)));
        if (StringUtils.isNotEmpty(bill.getCombinedId()))
        {
            log.setRelatedBillId(bill.getCombinedId());
            log.setRelatedBillNo(bill.getCombinedNo());
        }
        deliveryLineOpLogMapper.insertDeliveryLineOpLog(log);
    }

    @Override
    public void logCombinedLine(String action, String combinedId, String combinedNo, CombinedDeliveryDetail before,
            CombinedDeliveryDetail after, Long relatedDeliveryId, String relatedDeliveryNo, String operName)
    {
        if (StringUtils.isEmpty(combinedId))
        {
            return;
        }
        CombinedDeliveryDetail src = after != null ? after : before;
        DeliveryLineOpLog log = baseLog(DeliveryLineOpLog.KIND_COMBINED, combinedId, combinedNo, operName);
        log.setAction(action);
        if (src != null)
        {
            log.setDetailId(src.getDetailId());
            log.setOrderId(src.getOrderId() == null ? null : String.valueOf(src.getOrderId()));
            log.setOrderNo(src.getOrderNo());
            log.setOrderDetailId(src.getOrderDetailId() == null ? null : String.valueOf(src.getOrderDetailId()));
        }
        log.setBeforeJson(before == null ? null : JSON.toJSONString(snapshotCombinedDetail(before)));
        log.setAfterJson(after == null ? null : JSON.toJSONString(snapshotCombinedDetail(after)));
        log.setRelatedBillId(relatedDeliveryId == null ? null : String.valueOf(relatedDeliveryId));
        log.setRelatedBillNo(relatedDeliveryNo);
        deliveryLineOpLogMapper.insertDeliveryLineOpLog(log);
    }

    @Override
    public void logDeliveryPrint(Delivery bill, List<DeliveryDetail> details, String operName)
    {
        if (bill == null || bill.getDeliveryId() == null)
        {
            return;
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        if (details != null)
        {
            for (DeliveryDetail d : details)
            {
                if (d != null)
                {
                    rows.add(snapshotDeliveryDetail(d));
                }
            }
        }
        DeliveryLineOpLog log = baseLog(DeliveryLineOpLog.KIND_DELIVERY, String.valueOf(bill.getDeliveryId()),
            bill.getDeliveryNo(), operName);
        log.setAction(DeliveryLineOpLog.ACTION_PRINT);
        log.setOrderId(bill.getOrderId() == null ? null : String.valueOf(bill.getOrderId()));
        log.setOrderNo(bill.getOrderNo());
        log.setAfterJson(JSON.toJSONString(rows));
        if (StringUtils.isNotEmpty(bill.getCombinedId()))
        {
            log.setRelatedBillId(bill.getCombinedId());
            log.setRelatedBillNo(bill.getCombinedNo());
        }
        deliveryLineOpLogMapper.insertDeliveryLineOpLog(log);
    }

    @Override
    public List<DeliveryLineOpLog> selectByBill(String billKind, String billId)
    {
        return deliveryLineOpLogMapper.selectByBill(billKind, billId);
    }

    private DeliveryLineOpLog baseLog(String kind, String billId, String billNo, String operName)
    {
        DeliveryLineOpLog log = new DeliveryLineOpLog();
        log.setLogId(IdUtils.simpleUuid7());
        log.setBillKind(kind);
        log.setBillId(billId);
        log.setBillNo(billNo);
        String name = StringUtils.trimToNull(operName);
        if (name == null)
        {
            name = ShiroUtils.getLoginName();
        }
        log.setOperName(name);
        Date now = DateUtils.getNowDate();
        log.setOperTime(now);
        return log;
    }

    private Map<String, Object> snapshotDeliveryDetail(DeliveryDetail d)
    {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("detailId", d.getDetailId());
        m.put("orderDetailId", d.getOrderDetailId());
        m.put("materialCode", d.getMaterialCode());
        m.put("materialName", d.getMaterialName());
        m.put("deliveryQuantity", d.getDeliveryQuantity());
        m.put("price", d.getPrice());
        m.put("amount", d.getAmount());
        m.put("batchNo", d.getBatchNo());
        m.put("productionDate", fmtDay(d.getProductionDate()));
        m.put("expireDate", fmtDay(d.getExpireDate()));
        return m;
    }

    private Map<String, Object> snapshotCombinedDetail(CombinedDeliveryDetail d)
    {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("detailId", d.getDetailId());
        m.put("orderId", d.getOrderId());
        m.put("orderNo", d.getOrderNo());
        m.put("orderDetailId", d.getOrderDetailId());
        m.put("materialCode", d.getMaterialCode());
        m.put("materialName", d.getMaterialName());
        m.put("deliveryQuantity", d.getDeliveryQuantity());
        m.put("price", d.getPrice());
        m.put("amount", d.getAmount());
        m.put("batchNo", d.getBatchNo());
        m.put("productionDate", fmtDay(d.getProductionDate()));
        m.put("expireDate", fmtDay(d.getExpireDate()));
        return m;
    }

    private String fmtDay(Date d)
    {
        return d == null ? null : DAY.format(d);
    }
}
