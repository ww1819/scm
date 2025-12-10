package com.scm.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.scm.common.core.text.Convert;
import com.scm.common.utils.DateUtils;
import com.scm.common.utils.StringUtils;
import com.scm.system.domain.HospitalSupplier;
import com.scm.system.mapper.HospitalSupplierMapper;
import com.scm.system.service.IHospitalSupplierService;

/**
 * 医院供应商关联 服务层实现
 * 
 * @author scm
 */
@Service
public class HospitalSupplierServiceImpl implements IHospitalSupplierService
{
    @Autowired
    private HospitalSupplierMapper hospitalSupplierMapper;

    /**
     * 查询医院供应商关联信息
     * 
     * @param relationId 关联ID
     * @return 关联信息
     */
    @Override
    public HospitalSupplier selectHospitalSupplierById(Long relationId)
    {
        return hospitalSupplierMapper.selectHospitalSupplierById(relationId);
    }

    /**
     * 查询医院供应商关联列表
     * 
     * @param hospitalSupplier 关联信息
     * @return 关联集合
     */
    @Override
    public List<HospitalSupplier> selectHospitalSupplierList(HospitalSupplier hospitalSupplier)
    {
        return hospitalSupplierMapper.selectHospitalSupplierList(hospitalSupplier);
    }

    /**
     * 根据供应商ID查询关联的医院列表
     * 
     * @param supplierId 供应商ID
     * @return 关联集合
     */
    @Override
    public List<HospitalSupplier> selectHospitalSupplierBySupplierId(Long supplierId)
    {
        return hospitalSupplierMapper.selectHospitalSupplierBySupplierId(supplierId);
    }

    /**
     * 新增医院供应商关联
     * 
     * @param hospitalSupplier 关联信息
     * @return 结果
     */
    @Override
    public int insertHospitalSupplier(HospitalSupplier hospitalSupplier)
    {
        if (StringUtils.isEmpty(hospitalSupplier.getStatus()))
        {
            hospitalSupplier.setStatus("0"); // 默认正常
        }
        if (StringUtils.isEmpty(hospitalSupplier.getBindStatus()))
        {
            hospitalSupplier.setBindStatus("1"); // 默认已绑定
        }
        if (hospitalSupplier.getBindTime() == null)
        {
            hospitalSupplier.setBindTime(DateUtils.getNowDate());
        }
        return hospitalSupplierMapper.insertHospitalSupplier(hospitalSupplier);
    }

    /**
     * 修改医院供应商关联
     * 
     * @param hospitalSupplier 关联信息
     * @return 结果
     */
    @Override
    public int updateHospitalSupplier(HospitalSupplier hospitalSupplier)
    {
        return hospitalSupplierMapper.updateHospitalSupplier(hospitalSupplier);
    }

    /**
     * 批量删除医院供应商关联
     * 
     * @param relationIds 需要删除的数据ID
     * @return 结果
     */
    @Override
    public int deleteHospitalSupplierByIds(String relationIds)
    {
        return hospitalSupplierMapper.deleteHospitalSupplierByIds(Convert.toStrArray(relationIds));
    }

    /**
     * 根据供应商ID删除所有关联
     * 
     * @param supplierId 供应商ID
     * @return 结果
     */
    @Override
    public int deleteHospitalSupplierBySupplierId(Long supplierId)
    {
        return hospitalSupplierMapper.deleteHospitalSupplierBySupplierId(supplierId);
    }

    /**
     * 保存供应商的配送公司（先删除旧的，再插入新的）
     * 
     * @param supplierId 供应商ID
     * @param hospitalIds 医院ID数组
     * @param createBy 创建人
     * @return 结果
     */
    @Override
    @Transactional
    public int saveSupplierHospitals(Long supplierId, String[] hospitalIds, String createBy)
    {
        // 先删除该供应商的所有关联
        hospitalSupplierMapper.deleteHospitalSupplierBySupplierId(supplierId);
        
        // 如果有新的医院ID，则插入新的关联
        if (hospitalIds != null && hospitalIds.length > 0)
        {
            for (String hospitalIdStr : hospitalIds)
            {
                if (StringUtils.isNotEmpty(hospitalIdStr))
                {
                    try
                    {
                        Long hospitalId = Long.parseLong(hospitalIdStr);
                        HospitalSupplier hospitalSupplier = new HospitalSupplier();
                        hospitalSupplier.setSupplierId(supplierId);
                        hospitalSupplier.setHospitalId(hospitalId);
                        hospitalSupplier.setBindStatus("1"); // 已绑定
                        hospitalSupplier.setBindTime(DateUtils.getNowDate());
                        hospitalSupplier.setBindBy(createBy);
                        hospitalSupplier.setStatus("0"); // 正常
                        hospitalSupplier.setCreateBy(createBy);
                        hospitalSupplierMapper.insertHospitalSupplier(hospitalSupplier);
                    }
                    catch (NumberFormatException e)
                    {
                        // 忽略无效的ID
                    }
                }
            }
        }
        
        return 1;
    }
}

