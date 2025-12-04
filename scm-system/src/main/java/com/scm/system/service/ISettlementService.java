package com.scm.system.service;

import java.util.List;
import com.scm.system.domain.Settlement;
import com.scm.system.domain.SettlementDetail;

/**
 * 结算单 服务层
 * 
 * @author scm
 */
public interface ISettlementService
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
     * 批量删除结算单信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteSettlementByIds(String ids);

    /**
     * 删除结算单信息
     * 
     * @param settlementId 结算单ID
     * @return 结果
     */
    public int deleteSettlementById(Long settlementId);

    /**
     * 查询结算明细列表
     * 
     * @param settlementId 结算单ID
     * @return 明细集合
     */
    public List<SettlementDetail> selectSettlementDetailListBySettlementId(Long settlementId);

    /**
     * 审核结算单
     * 
     * @param settlementId 结算单ID
     * @return 结果
     */
    public int auditSettlement(Long settlementId);
}

