package com.scm.system.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.scm.common.constant.ZsJsfsHighLow;
import com.scm.common.exception.ServiceException;
import com.scm.common.utils.DateUtils;
import com.scm.common.utils.StringUtils;
import com.scm.common.utils.uuid.IdUtils;
import com.scm.system.domain.Delivery;
import com.scm.system.domain.DeliveryDetail;
import com.scm.system.domain.DeliveryDetailBarcode;
import com.scm.system.domain.ScmBarcodeSeed;
import com.scm.system.domain.ZsTpOrder;
import com.scm.system.mapper.DeliveryDetailBarcodeMapper;
import com.scm.system.mapper.ScmBarcodeSeedMapper;
import com.scm.system.mapper.ZsTpOrderMapper;

/**
 * 中设条码种子与配送明细条码生成。
 * <p>
 * 中设订单种子划分仅按高低值（及 T/Z 渠道维度），{@code warehouse_id} 固定 {@link ZsJsfsHighLow#ZS_SEED_WAREHOUSE_ID}，不按仓库拆分。
 */
@Service
public class ScmBarcodeSeedService
{
    public static final String CHANNEL_TENANT = "TENANT";
    public static final String CHANNEL_ZS = "ZS";
    private static final String COUNTER_T = "T";
    private static final String COUNTER_Z = "Z";
    private static final String HIGH_LOW_DEFAULT = "L";

    @Autowired
    private ScmBarcodeSeedMapper scmBarcodeSeedMapper;

    @Autowired
    private DeliveryDetailBarcodeMapper deliveryDetailBarcodeMapper;

    @Autowired
    private ZsTpOrderMapper zsTpOrderMapper;

    /**
     * 我方订单落库后：若租户维度尚无种子行则插入（租户ID+仓库ID+高低值）。
     */
    @Transactional(rollbackFor = Exception.class)
    public void ensureTenantSeedRowIfAbsent(String tenantId, String warehouseIdStr)
    {
        if (StringUtils.isEmpty(tenantId))
        {
            return;
        }
        String wid = StringUtils.trimToEmpty(warehouseIdStr);
        scmBarcodeSeedMapper.ensureTenantSeed(IdUtils.simpleUuid7(), tenantId, wid, HIGH_LOW_DEFAULT);
    }

    /**
     * 中设订单 jsfs=3 时生成配送明细条码并落库。
     */
    @Transactional(rollbackFor = Exception.class)
    public void createZsDeliveryDetailBarcodesIfNeeded(Delivery delivery, List<DeliveryDetail> savedDetails)
    {
        if (delivery == null || StringUtils.isEmpty(delivery.getZsOrderId()) || savedDetails == null || savedDetails.isEmpty())
        {
            return;
        }
        if (!"3".equals(StringUtils.trimToNull(delivery.getZsJsfs())))
        {
            return;
        }
        ZsTpOrder z = zsTpOrderMapper.selectZsTpOrderById(delivery.getZsOrderId());
        if (z == null)
        {
            throw new ServiceException("中设订单不存在，无法生成条码");
        }
        String customerRaw = StringUtils.trimToEmpty(z.getCustomer());
        String customerCode = stripKFromCustomerCode(customerRaw);
        if (StringUtils.isEmpty(customerCode))
        {
            throw new ServiceException("中设客户编码为空，无法生成条码");
        }
        String yymmdd = new SimpleDateFormat("yyMMdd").format(DateUtils.getNowDate());
        String tenantId = StringUtils.trimToEmpty(delivery.getTenantId());
        boolean tenantChannel = CHANNEL_TENANT.equalsIgnoreCase(StringUtils.trimToEmpty(z.getReceiveChannel()));
        String zsCust = StringUtils.trimToEmpty(delivery.getZsCustomerId());

        List<DeliveryDetailBarcode> rows = new ArrayList<>();
        String deliveryNo = StringUtils.trimToEmpty(delivery.getDeliveryNo());

        for (DeliveryDetail dd : savedDetails)
        {
            int n = computeBarcodeCount(dd);
            if (n <= 0)
            {
                continue;
            }
            List<Long> seeds = allocateSeeds(tenantChannel, tenantId, zsCust, ZsJsfsHighLow.ZS_SEED_WAREHOUSE_ID,
                ZsJsfsHighLow.highLowFlagFromJsfs(z.getJsfs()), n);
            for (Long seed : seeds)
            {
                DeliveryDetailBarcode b = new DeliveryDetailBarcode();
                b.setId(IdUtils.simpleUuid7());
                b.setDeliveryId(delivery.getDeliveryId());
                b.setDeliveryNo(deliveryNo);
                b.setDeliveryDetailId(dd.getDetailId());
                b.setSeedNum(seed);
                b.setBarcodeNo(buildBarcodeNo(customerCode, yymmdd, seed));
                rows.add(b);
            }
        }
        if (!rows.isEmpty())
        {
            deliveryDetailBarcodeMapper.batchInsert(rows);
        }
    }

    private static int computeBarcodeCount(DeliveryDetail dd)
    {
        BigDecimal dq = dd.getDeliveryQuantity();
        BigDecimal pc = dd.getPackCoefficient();
        if (dq == null || pc == null || pc.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("jsfs为3时，配送明细须填写配送数量与有效打包系数，行物料：" + StringUtils.trimToEmpty(dd.getMaterialName()));
        }
        BigDecimal[] dr = dq.divideAndRemainder(pc);
        if (dr[1].compareTo(BigDecimal.ZERO) != 0)
        {
            throw new ServiceException("jsfs为3时，配送数量须为打包系数的整数倍，行物料：" + StringUtils.trimToEmpty(dd.getMaterialName()));
        }
        return dr[0].setScale(0, RoundingMode.UNNECESSARY).intValueExact();
    }

    /**
     * 分配连续 n 个种子序号（返回每个条码对应的种子数值）。
     */
    @Transactional(rollbackFor = Exception.class)
    public List<Long> allocateSeeds(boolean tenantChannel, String tenantId, String zsCustomerId,
        String warehouseId, String highLowFlag, int count)
    {
        if (count <= 0)
        {
            return new ArrayList<>();
        }
        String ct = tenantChannel ? COUNTER_T : COUNTER_Z;
        String tid = StringUtils.trimToEmpty(tenantId);
        String wid = StringUtils.trimToEmpty(warehouseId);
        String hl = StringUtils.isEmpty(highLowFlag) ? HIGH_LOW_DEFAULT : highLowFlag;
        String zc = tenantChannel ? "" : StringUtils.trimToEmpty(zsCustomerId);

        if (tenantChannel)
        {
            scmBarcodeSeedMapper.ensureTenantSeed(IdUtils.simpleUuid7(), tid, wid, hl);
        }
        else
        {
            scmBarcodeSeedMapper.ensureZsCustomerSeed(IdUtils.simpleUuid7(), tid, zc, wid, hl);
        }

        ScmBarcodeSeed row = scmBarcodeSeedMapper.selectForUpdate(ct, tid, zc, wid, hl);
        if (row == null)
        {
            throw new ServiceException("条码种子行不存在，请重试或联系管理员");
        }
        long start = row.getSeedValue() != null ? row.getSeedValue() : 0L;
        long newVal = start + count;
        scmBarcodeSeedMapper.updateSeedValue(row.getId(), newVal);
        List<Long> out = new ArrayList<>(count);
        for (long i = 1; i <= count; i++)
        {
            out.add(start + i);
        }
        return out;
    }

    /**
     * 中设订单条码：P + 去掉K的客户编码 + '-' + yymmdd + 不足6位左补零的种子数
     */
    public static String buildBarcodeNo(String customerCodeWithoutK, String yymmdd, long seedNum)
    {
        String seedPart = String.valueOf(seedNum);
        if (seedPart.length() < 6)
        {
            seedPart = String.format("%6s", seedPart).replace(' ', '0');
        }
        return "P" + customerCodeWithoutK + "-" + yymmdd + seedPart;
    }

    public static String stripKFromCustomerCode(String customer)
    {
        if (customer == null)
        {
            return "";
        }
        return customer.replace("K", "");
    }

    public void deleteBarcodesByDeliveryId(Long deliveryId)
    {
        if (deliveryId != null)
        {
            deliveryDetailBarcodeMapper.deleteByDeliveryId(deliveryId);
        }
    }

    public void attachDetailBarcodes(List<DeliveryDetail> details, Long deliveryId)
    {
        if (details == null || details.isEmpty() || deliveryId == null)
        {
            return;
        }
        List<DeliveryDetailBarcode> all = deliveryDetailBarcodeMapper.selectListByDeliveryId(deliveryId);
        Map<Long, List<DeliveryDetailBarcode>> byDetail = new HashMap<>();
        for (DeliveryDetailBarcode b : all)
        {
            if (b.getDeliveryDetailId() == null)
            {
                continue;
            }
            byDetail.computeIfAbsent(b.getDeliveryDetailId(), k -> new ArrayList<>()).add(b);
        }
        for (DeliveryDetail d : details)
        {
            d.setDetailBarcodes(byDetail.getOrDefault(d.getDetailId(), new ArrayList<>()));
        }
    }
}
