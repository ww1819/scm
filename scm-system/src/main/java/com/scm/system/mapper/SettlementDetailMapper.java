package com.scm.system.mapper;

import java.util.List;
import com.scm.system.domain.SettlementDetail;

/**
 * 结算明细 数据层
 * 
 * @author scm
 */
public interface SettlementDetailMapper
{
    /**
     * 查询结算明细信息
     * 
     * @param detailId 明细ID
     * @return 明细信息
     */
    public SettlementDetail selectSettlementDetailById(Long detailId);

    /**
     * 查询结算明细列表
     * 
     * @param settlementDetail 明细信息
     * @return 明细集合
     */
    public List<SettlementDetail> selectSettlementDetailList(SettlementDetail settlementDetail);

    /**
     * 根据结算单ID查询结算明细列表
     * 
     * @param settlementId 结算单ID
     * @return 明细集合
     */
    public List<SettlementDetail> selectSettlementDetailListBySettlementId(Long settlementId);

    /**
     * 新增结算明细信息
     * 
     * @param settlementDetail 明细信息
     * @return 结果
     */
    public int insertSettlementDetail(SettlementDetail settlementDetail);

    /**
     * 批量新增结算明细信息
     * 
     * @param settlementDetailList 明细信息列表
     * @return 结果
     */
    public int batchInsertSettlementDetail(List<SettlementDetail> settlementDetailList);

    /**
     * 修改结算明细信息
     * 
     * @param settlementDetail 明细信息
     * @return 结果
     */
    public int updateSettlementDetail(SettlementDetail settlementDetail);

    /**
     * 删除结算明细信息
     * 
     * @param detailId 明细主键
     * @return 结果
     */
    public int deleteSettlementDetailById(Long detailId);

    /**
     * 根据结算单ID删除结算明细信息
     * 
     * @param settlementId 结算单ID
     * @return 结果
     */
    public int deleteSettlementDetailBySettlementId(Long settlementId);

    /**
     * 批量删除结算明细信息
     * 
     * @param detailIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteSettlementDetailByIds(String[] detailIds);
}

