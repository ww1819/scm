package com.scm.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.scm.common.core.text.Convert;
import com.scm.common.utils.DateUtils;
import com.scm.common.utils.PinyinUtils;
import com.scm.common.utils.StringUtils;
import com.scm.system.domain.Supplier;
import com.scm.system.mapper.SupplierMapper;
import com.scm.system.service.ISupplierService;

/**
 * 供应商信息 服务层实现
 * 
 * @author scm
 */
@Service
public class SupplierServiceImpl implements ISupplierService
{
    @Autowired
    private SupplierMapper supplierMapper;

    /**
     * 查询供应商信息
     * 
     * @param supplierId 供应商ID
     * @return 供应商信息
     */
    @Override
    public Supplier selectSupplierById(Long supplierId)
    {
        return supplierMapper.selectSupplierById(supplierId);
    }

    /**
     * 查询供应商信息列表
     * 
     * @param supplier 供应商信息
     * @return 供应商集合
     */
    @Override
    public List<Supplier> selectSupplierList(Supplier supplier)
    {
        return supplierMapper.selectSupplierList(supplier);
    }

    /**
     * 新增供应商信息
     * 
     * @param supplier 供应商信息
     * @return 结果
     */
    @Override
    public int insertSupplier(Supplier supplier)
    {
        if (StringUtils.isEmpty(supplier.getStatus()))
        {
            supplier.setStatus("1"); // 默认启用（正常）
        }
        if (StringUtils.isEmpty(supplier.getAuditStatus()))
        {
            supplier.setAuditStatus("0"); // 默认待审核
        }
        // 如果供应商编码为空，自动生成唯一编码
        if (StringUtils.isEmpty(supplier.getSupplierCode()))
        {
            supplier.setSupplierCode(generateSupplierCode());
        }
        fillSupplierPinyin(supplier);
        supplier.setCreateTime(DateUtils.getNowDate());
        return supplierMapper.insertSupplier(supplier);
    }

    /**
     * 生成唯一的供应商编码
     * 
     * @return 供应商编码
     */
    private String generateSupplierCode()
    {
        String code;
        int maxAttempts = 10;
        int attempt = 0;
        do
        {
            // 使用时间戳+随机数生成编码
            code = "S" + System.currentTimeMillis() + (int)(Math.random() * 1000);
            if (code.length() > 50)
            {
                code = code.substring(0, 50);
            }
            attempt++;
        }
        while (supplierMapper.checkSupplierCodeUnique(code) != null && attempt < maxAttempts);
        
        if (attempt >= maxAttempts)
        {
            // 如果10次尝试都失败，使用UUID
            String uuid = java.util.UUID.randomUUID().toString().replace("-", "");
            code = "S" + uuid.substring(0, Math.min(20, uuid.length()));
        }
        
        return code;
    }

    private void fillSupplierPinyin(Supplier supplier)
    {
        if (supplier == null)
        {
            return;
        }
        if (StringUtils.isEmpty(supplier.getCompanyName()))
        {
            supplier.setPinyinCode("");
            return;
        }
        String raw = PinyinUtils.getShortCode(supplier.getCompanyName().trim());
        String py = raw != null ? raw : "";
        supplier.setPinyinCode(py);
        supplier.setCompanyShortName(StringUtils.isNotEmpty(py) ? py.toUpperCase() : "");
    }

    /**
     * 修改供应商信息
     * 
     * @param supplier 供应商信息
     * @return 结果
     */
    @Override
    public int updateSupplier(Supplier supplier)
    {
        fillSupplierPinyin(supplier);
        supplier.setUpdateTime(DateUtils.getNowDate());
        return supplierMapper.updateSupplier(supplier);
    }

    /**
     * 批量删除供应商信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    @Override
    public int deleteSupplierByIds(String ids)
    {
        return supplierMapper.deleteSupplierByIds(Convert.toStrArray(ids));
    }

    /**
     * 删除供应商信息
     * 
     * @param supplierId 供应商ID
     * @return 结果
     */
    @Override
    public int deleteSupplierById(Long supplierId)
    {
        return supplierMapper.deleteSupplierById(supplierId);
    }

    /**
     * 校验供应商编码是否唯一
     * 
     * @param supplier 供应商信息
     * @return 结果
     */
    @Override
    public boolean checkSupplierCodeUnique(Supplier supplier)
    {
        Long supplierId = StringUtils.isNull(supplier.getSupplierId()) ? -1L : supplier.getSupplierId();
        Supplier info = supplierMapper.checkSupplierCodeUnique(supplier.getSupplierCode());
        if (StringUtils.isNotNull(info) && info.getSupplierId().longValue() != supplierId.longValue())
        {
            return false;
        }
        return true;
    }

    /**
     * 审核供应商
     * 
     * @param supplier 供应商信息
     * @return 结果
     */
    @Override
    public int auditSupplier(Supplier supplier)
    {
        supplier.setAuditTime(DateUtils.getNowDate());
        supplier.setUpdateTime(DateUtils.getNowDate());
        if ("1".equals(supplier.getAuditStatus()))
        {
            supplier.setStatus("1"); // 审核通过，状态改为正常
        }
        else if ("2".equals(supplier.getAuditStatus()))
        {
            supplier.setStatus("2"); // 审核拒绝，状态改为停用
        }
        return supplierMapper.updateSupplier(supplier);
    }
}

