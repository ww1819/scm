package com.scm.system.mapper;

import java.util.List;
import com.scm.system.domain.Settlement;

/**
 * 结算单 数据层
 * 
 * @author scm
 */
public interface SettlementMapper
{
    /**
     * 查询结算单信息
     * 
     * @param settlementId 结算单ID
     * @return 结算单信息
     */
    public Settlement selectSettlementById(Long settlementId);

    /**
     * 查询结算单列表
     * 
     * @param settlement 结算单信息
     * @return 结算单集合
     */
    public List<Settlement> selectSettlementList(Settlement settlement);

    /**
     * 根据结算单号查询结算单
     * 
     * @param settlementNo 结算单号
     * @return 结算单信息
     */
    public Settlement selectSettlementBySettlementNo(String settlementNo);

    /**
     * 新增结算单信息
     * 
     * @param settlement 结算单信息
     * @return 结果
     */
    public int insertSettlement(Settlement settlement);

    /**
     * 修改结算单信息
     * 
     * @param settlement 结算单信息
     * @return 结果
     */
    public int updateSettlement(Settlement settlement);

    /**
     * 删除结算单信息
     * 
     * @param settlementId 结算单主键
     * @return 结果
     */
    public int deleteSettlementById(Long settlementId);

    /**
     * 批量删除结算单信息
     * 
     * @param settlementIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteSettlementByIds(String[] settlementIds);
}

