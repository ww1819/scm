package com.scm.system.service.impl;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.scm.common.core.text.Convert;
import com.scm.common.utils.DateUtils;
import com.scm.common.utils.StringUtils;
import com.scm.system.domain.Settlement;
import com.scm.system.domain.SettlementDetail;
import com.scm.system.mapper.SettlementDetailMapper;
import com.scm.system.mapper.SettlementMapper;
import com.scm.system.service.ISettlementService;

/**
 * 结算单 服务层实现
 * 
 * @author scm
 */
@Service
public class SettlementServiceImpl implements ISettlementService
{
    @Autowired
    private SettlementMapper settlementMapper;

    @Autowired
    private SettlementDetailMapper settlementDetailMapper;

    /**
     * 查询结算单信息
     * 
     * @param settlementId 结算单ID
     * @return 结算单信息
     */
    @Override
    public Settlement selectSettlementById(Long settlementId)
    {
        Settlement settlement = settlementMapper.selectSettlementById(settlementId);
        if (settlement != null)
        {
            List<SettlementDetail> details = settlementDetailMapper.selectSettlementDetailListBySettlementId(settlementId);
            settlement.setSettlementDetails(details);
        }
        return settlement;
    }

    /**
     * 查询结算单列表
     * 
     * @param settlement 结算单信息
     * @return 结算单集合
     */
    @Override
    public List<Settlement> selectSettlementList(Settlement settlement)
    {
        return settlementMapper.selectSettlementList(settlement);
    }

    /**
     * 新增结算单信息
     * 
     * @param settlement 结算单信息
     * @return 结果
     */
    @Override
    @Transactional
    public int insertSettlement(Settlement settlement)
    {
        if (StringUtils.isEmpty(settlement.getAuditStatus()))
        {
            settlement.setAuditStatus("0"); // 默认待审核
        }
        if (StringUtils.isEmpty(settlement.getCustomerSettlementStatus()))
        {
            settlement.setCustomerSettlementStatus("0"); // 默认未结算
        }
        // 如果结算单号为空，自动生成唯一编号
        if (StringUtils.isEmpty(settlement.getSettlementNo()))
        {
            settlement.setSettlementNo(generateSettlementNo());
        }
        settlement.setCreateTime(DateUtils.getNowDate());
        
        // 计算总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (settlement.getSettlementDetails() != null && !settlement.getSettlementDetails().isEmpty())
        {
            for (SettlementDetail detail : settlement.getSettlementDetails())
            {
                if (detail.getPrice() != null && detail.getQuantity() != null)
                {
                    BigDecimal amount = detail.getPrice().multiply(detail.getQuantity());
                    detail.setAmount(amount);
                    totalAmount = totalAmount.add(amount);
                }
            }
        }
        settlement.setTotalAmount(totalAmount);
        
        int rows = settlementMapper.insertSettlement(settlement);
        
        // 保存结算明细
        if (settlement.getSettlementDetails() != null && !settlement.getSettlementDetails().isEmpty())
        {
            for (SettlementDetail detail : settlement.getSettlementDetails())
            {
                detail.setSettlementId(settlement.getSettlementId());
            }
            settlementDetailMapper.batchInsertSettlementDetail(settlement.getSettlementDetails());
        }
        
        return rows;
    }

    /**
     * 修改结算单信息
     * 
     * @param settlement 结算单信息
     * @return 结果
     */
    @Override
    @Transactional
    public int updateSettlement(Settlement settlement)
    {
        settlement.setUpdateTime(DateUtils.getNowDate());
        
        // 如果修改了明细，重新计算总金额
        if (settlement.getSettlementDetails() != null && !settlement.getSettlementDetails().isEmpty())
        {
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (SettlementDetail detail : settlement.getSettlementDetails())
            {
                if (detail.getPrice() != null && detail.getQuantity() != null)
                {
                    BigDecimal amount = detail.getPrice().multiply(detail.getQuantity());
                    detail.setAmount(amount);
                    totalAmount = totalAmount.add(amount);
                }
            }
            settlement.setTotalAmount(totalAmount);
        }
        
        return settlementMapper.updateSettlement(settlement);
    }

    /**
     * 批量删除结算单信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteSettlementByIds(String ids)
    {
        String[] settlementIds = Convert.toStrArray(ids);
        for (String settlementId : settlementIds)
        {
            // 删除结算明细
            settlementDetailMapper.deleteSettlementDetailBySettlementId(Long.parseLong(settlementId));
        }
        return settlementMapper.deleteSettlementByIds(settlementIds);
    }

    /**
     * 删除结算单信息
     * 
     * @param settlementId 结算单ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteSettlementById(Long settlementId)
    {
        // 删除结算明细
        settlementDetailMapper.deleteSettlementDetailBySettlementId(settlementId);
        return settlementMapper.deleteSettlementById(settlementId);
    }

    /**
     * 查询结算明细列表
     * 
     * @param settlementId 结算单ID
     * @return 明细集合
     */
    @Override
    public List<SettlementDetail> selectSettlementDetailListBySettlementId(Long settlementId)
    {
        return settlementDetailMapper.selectSettlementDetailListBySettlementId(settlementId);
    }

    /**
     * 审核结算单
     * 
     * @param settlementId 结算单ID
     * @return 结果
     */
    @Override
    public int auditSettlement(Long settlementId)
    {
        Settlement settlement = settlementMapper.selectSettlementById(settlementId);
        if (settlement == null)
        {
            return 0;
        }
        settlement.setAuditStatus("1"); // 已审核
        settlement.setAuditTime(DateUtils.getNowDate());
        settlement.setUpdateTime(DateUtils.getNowDate());
        return settlementMapper.updateSettlement(settlement);
    }

    /**
     * 生成唯一的结算单号
     * 
     * @return 结算单号
     */
    private String generateSettlementNo()
    {
        String code;
        int maxAttempts = 10;
        int attempt = 0;
        do
        {
            // 使用时间戳+随机数生成编号
            code = "SET" + System.currentTimeMillis() + (int)(Math.random() * 1000);
            if (code.length() > 50)
            {
                code = code.substring(0, 50);
            }
            attempt++;
        }
        while (settlementMapper.selectSettlementBySettlementNo(code) != null && attempt < maxAttempts);
        
        if (attempt >= maxAttempts)
        {
            // 如果10次尝试都失败，使用UUID
            String uuid = java.util.UUID.randomUUID().toString().replace("-", "");
            code = "SET" + uuid.substring(0, Math.min(20, uuid.length()));
        }
        
        return code;
    }
}

