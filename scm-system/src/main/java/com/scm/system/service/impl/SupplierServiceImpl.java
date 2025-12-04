package com.scm.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.scm.common.core.text.Convert;
import com.scm.common.utils.DateUtils;
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
        // 如果拼音简码为空，根据公司名称自动生成
        if (StringUtils.isEmpty(supplier.getCompanyShortName()) && StringUtils.isNotEmpty(supplier.getCompanyName()))
        {
            supplier.setCompanyShortName(generateCompanyShortCode(supplier.getCompanyName()));
        }
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

    /**
     * 生成拼音简码（从公司名称提取首字母大写）
     * 
     * @param companyName 公司名称
     * @return 拼音简码
     */
    private String generateCompanyShortCode(String companyName)
    {
        if (StringUtils.isEmpty(companyName))
        {
            return "";
        }
        
        // 移除常见的公司后缀
        String name = companyName.trim();
        name = name.replaceAll("(有限公司|股份有限公司|有限责任公司|股份公司|科技公司|科技有限公司|技术公司|信息技术公司|医疗科技公司|健康产业公司|产业有限公司|集团有限公司|集团股份有限公司)$", "");
        
        // 提取首字母并转为大写
        String shortCode = "";
        
        // 如果包含括号，提取括号内的内容的首字母
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("[（(]([^）)]+)[）)]");
        java.util.regex.Matcher matcher = pattern.matcher(name);
        if (matcher.find())
        {
            String bracketContent = matcher.group(1);
            // 提取首字符并转为大写
            if (bracketContent.length() > 0)
            {
                char firstChar = bracketContent.charAt(0);
                // 如果是英文字母，转为大写
                if (Character.isLetter(firstChar))
                {
                    shortCode = String.valueOf(Character.toUpperCase(firstChar));
                }
                else
                {
                    // 如果是中文或其他字符，保留原字符
                    shortCode = String.valueOf(firstChar);
                }
            }
        }
        else
        {
            // 提取公司名称的首字符
            if (name.length() > 0)
            {
                char firstChar = name.charAt(0);
                // 如果是英文字母，转为大写
                if (Character.isLetter(firstChar))
                {
                    shortCode = String.valueOf(Character.toUpperCase(firstChar));
                }
                else
                {
                    // 如果是中文或其他字符，保留原字符
                    shortCode = String.valueOf(firstChar);
                }
            }
        }
        
        return shortCode;
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
        // 如果拼音简码为空，根据公司名称自动生成
        if (StringUtils.isEmpty(supplier.getCompanyShortName()) && StringUtils.isNotEmpty(supplier.getCompanyName()))
        {
            supplier.setCompanyShortName(generateCompanyShortCode(supplier.getCompanyName()));
        }
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

