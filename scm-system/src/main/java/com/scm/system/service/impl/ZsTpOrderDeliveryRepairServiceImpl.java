package com.scm.system.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.scm.common.utils.DateUtils;
import com.scm.common.utils.uuid.IdUtils;
import com.scm.system.domain.ScmOrderDetailDeliveryRel;
import com.scm.system.domain.ZsTpOrderDetailDeliveryRel;
import com.scm.system.domain.vo.ZsTpOrderDeliveryRepairResultVo;
import com.scm.system.mapper.ScmOrderDetailDeliveryRelMapper;
import com.scm.system.mapper.ZsTpOrderDeliveryRepairMapper;
import com.scm.system.mapper.ZsTpOrderDetailDeliveryRelMapper;
import com.scm.system.service.IZsTpOrderDeliveryRepairService;

@Service
public class ZsTpOrderDeliveryRepairServiceImpl implements IZsTpOrderDeliveryRepairService
{
    private static final int BATCH = 200;

    @Autowired
    private ZsTpOrderDeliveryRepairMapper zsTpOrderDeliveryRepairMapper;

    @Autowired
    private ZsTpOrderDetailDeliveryRelMapper zsTpOrderDetailDeliveryRelMapper;

    @Autowired
    private ScmOrderDetailDeliveryRelMapper scmOrderDetailDeliveryRelMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ZsTpOrderDeliveryRepairResultVo repairDeliveryLinks(String operator)
    {
        String op = StringUtils.trimToEmpty(operator);
        if (StringUtils.isEmpty(op))
        {
            op = "system";
        }
        ZsTpOrderDeliveryRepairResultVo vo = new ZsTpOrderDeliveryRepairResultVo();
        vo.setDeliveryHeaderBackfilled(zsTpOrderDeliveryRepairMapper.backfillDeliveryHeaderFromDetails(op));

        List<ZsTpOrderDetailDeliveryRel> missing = zsTpOrderDeliveryRepairMapper.selectMissingDeliveryRelRows();
        int relInserted = 0;
        if (missing != null && !missing.isEmpty())
        {
            String now = DateUtils.getTime();
            List<ZsTpOrderDetailDeliveryRel> batch = new ArrayList<>();
            for (ZsTpOrderDetailDeliveryRel row : missing)
            {
                if (row == null || StringUtils.isAnyEmpty(row.getOrderDetailId(), row.getDeliveryDetailId()))
                {
                    continue;
                }
                row.setId(IdUtils.simpleUuid7());
                row.setCreateTime(now);
                if (StringUtils.isEmpty(row.getCreateBy()))
                {
                    row.setCreateBy(op);
                }
                batch.add(row);
                if (batch.size() >= BATCH)
                {
                    relInserted += zsTpOrderDetailDeliveryRelMapper.batchInsert(batch);
                    batch.clear();
                }
            }
            if (!batch.isEmpty())
            {
                relInserted += zsTpOrderDetailDeliveryRelMapper.batchInsert(batch);
            }
        }
        vo.setDeliveryRelInserted(relInserted);

        vo.setScmDeliveryHeaderBackfilled(zsTpOrderDeliveryRepairMapper.backfillScmDeliveryHeaderFromDetails(op));
        List<ScmOrderDetailDeliveryRel> scmMissing = zsTpOrderDeliveryRepairMapper.selectMissingScmDeliveryRelRows();
        int scmRelInserted = 0;
        if (scmMissing != null && !scmMissing.isEmpty())
        {
            String now = DateUtils.getTime();
            List<ScmOrderDetailDeliveryRel> batch = new ArrayList<>();
            for (ScmOrderDetailDeliveryRel row : scmMissing)
            {
                if (row == null || StringUtils.isAnyEmpty(row.getOrderDetailId(), row.getDeliveryDetailId()))
                {
                    continue;
                }
                row.setId(IdUtils.simpleUuid7());
                row.setCreateTime(now);
                if (StringUtils.isEmpty(row.getCreateBy()))
                {
                    row.setCreateBy(op);
                }
                batch.add(row);
                if (batch.size() >= BATCH)
                {
                    scmRelInserted += scmOrderDetailDeliveryRelMapper.batchInsert(batch);
                    batch.clear();
                }
            }
            if (!batch.isEmpty())
            {
                scmRelInserted += scmOrderDetailDeliveryRelMapper.batchInsert(batch);
            }
        }
        vo.setScmDeliveryRelInserted(scmRelInserted);

        int anomalyCnt = zsTpOrderDeliveryRepairMapper.countDoneOrdersWithoutDelivery();
        vo.setAnomalyDoneWithoutDelivery(anomalyCnt);
        if (anomalyCnt > 0)
        {
            vo.setAnomalySamples(zsTpOrderDeliveryRepairMapper.selectDoneOrdersWithoutDeliverySample(20));
        }
        return vo;
    }
}
