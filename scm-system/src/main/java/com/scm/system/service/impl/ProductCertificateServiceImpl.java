package com.scm.system.service.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.scm.common.core.text.Convert;
import com.scm.common.utils.DateUtils;
import com.scm.common.utils.StringUtils;
import com.scm.system.domain.ProductCertificate;
import com.scm.system.mapper.ProductCertificateMapper;
import com.scm.system.service.IProductCertificateService;

/**
 * 产品证件 服务层实现
 * 
 * @author scm
 */
@Service
public class ProductCertificateServiceImpl implements IProductCertificateService
{
    @Autowired
    private ProductCertificateMapper productCertificateMapper;
    
    @Autowired
    private com.scm.system.service.IMaterialDictService materialDictService;

    /**
     * 查询产品证件信息
     * 
     * @param certificateId 证件ID
     * @return 证件信息
     */
    @Override
    public ProductCertificate selectProductCertificateById(Long certificateId)
    {
        return productCertificateMapper.selectProductCertificateById(certificateId);
    }

    /**
     * 查询产品证件列表
     * 
     * @param productCertificate 证件信息
     * @return 证件集合
     */
    @Override
    public List<ProductCertificate> selectProductCertificateList(ProductCertificate productCertificate)
    {
        return productCertificateMapper.selectProductCertificateList(productCertificate);
    }

    /**
     * 查询过期预警的产品证件列表
     * 
     * @param productCertificate 证件信息
     * @return 证件集合
     */
    @Override
    public List<ProductCertificate> selectExpiringCertificateList(ProductCertificate productCertificate)
    {
        return productCertificateMapper.selectExpiringCertificateList(productCertificate);
    }

    /**
     * 新增产品证件信息
     * 
     * @param productCertificate 证件信息
     * @return 结果
     */
    @Override
    public int insertProductCertificate(ProductCertificate productCertificate)
    {
        return insertProductCertificate(productCertificate, null, null, null, null, null);
    }
    
    /**
     * 新增产品证件信息（带产品详细信息）
     * 
     * @param productCertificate 证件信息
     * @param specification 规格
     * @param model 型号
     * @param unit 单位
     * @param manufacturerName 生产厂家
     * @param purchasePrice 采购价格
     * @return 结果
     */
    public int insertProductCertificate(ProductCertificate productCertificate,
                                       String specification, String model, String unit,
                                       String manufacturerName, java.math.BigDecimal purchasePrice)
    {
        if (StringUtils.isEmpty(productCertificate.getAuditStatus()))
        {
            productCertificate.setAuditStatus("0"); // 默认待审核
        }
        if (StringUtils.isEmpty(productCertificate.getStatus()))
        {
            productCertificate.setStatus("0"); // 默认正常
        }
        
        // 如果materialId为空或为0，根据产品信息自动创建物资字典记录
        Long materialId = productCertificate.getMaterialId();
        if ((materialId == null || materialId == 0) && StringUtils.isNotEmpty(productCertificate.getMaterialName()))
        {
            materialId = createMaterialDictFromCertificate(productCertificate, 
                specification, model, unit, manufacturerName, purchasePrice);
            if (materialId == null || materialId == 0)
            {
                throw new RuntimeException("自动创建物资字典记录失败，无法保存产品证件");
            }
            productCertificate.setMaterialId(materialId);
        }
        
        // 验证materialId是否已设置
        if (productCertificate.getMaterialId() == null || productCertificate.getMaterialId() == 0)
        {
            throw new RuntimeException("物资ID不能为空，请确保产品名称已填写");
        }
        
        productCertificate.setCreateTime(DateUtils.getNowDate());
        // 检查过期状态
        checkExpiredStatus(productCertificate);
        return productCertificateMapper.insertProductCertificate(productCertificate);
    }
    
    /**
     * 根据产品证件信息创建物资字典记录
     * 
     * @param productCertificate 产品证件信息
     * @param specification 规格
     * @param model 型号
     * @param unit 单位
     * @param manufacturerName 生产厂家
     * @param purchasePrice 采购价格
     * @return 物资ID
     */
    private Long createMaterialDictFromCertificate(ProductCertificate productCertificate,
                                                   String specification, String model, String unit,
                                                   String manufacturerName, java.math.BigDecimal purchasePrice)
    {
        com.scm.system.domain.MaterialDict materialDict = new com.scm.system.domain.MaterialDict();
        materialDict.setMaterialName(productCertificate.getMaterialName());
        if (StringUtils.isNotEmpty(specification))
        {
            materialDict.setSpecification(specification);
        }
        if (StringUtils.isNotEmpty(model))
        {
            materialDict.setModel(model);
        }
        if (StringUtils.isNotEmpty(unit))
        {
            materialDict.setUnit(unit);
        }
        // 如果提供了厂家名称，需要查找或创建厂家记录
        // 注意：MaterialDict表中只保存manufacturerId，不保存manufacturerName
        // manufacturerName只是用于显示，需要通过JOIN获取
        // 这里暂时不处理厂家名称，因为需要厂家服务支持
        // 如果后续需要，可以添加厂家查询/创建逻辑
        if (purchasePrice != null)
        {
            materialDict.setPurchasePrice(purchasePrice);
        }
        materialDict.setStatus("0"); // 默认正常
        materialDict.setDelFlag("0");
        materialDict.setCreateBy(productCertificate.getCreateBy());
        materialDict.setCreateTime(DateUtils.getNowDate());
        
        // 产品编码由MaterialDictService自动生成，不需要手动设置
        
        // 保存物资字典
        try
        {
            int result = materialDictService.insertMaterialDict(materialDict);
            if (result > 0 && materialDict.getMaterialId() != null)
            {
                return materialDict.getMaterialId();
            }
            else
            {
                // 如果保存失败，抛出异常
                throw new RuntimeException("创建物资字典记录失败，返回结果：" + result + "，物资ID：" + materialDict.getMaterialId());
            }
        }
        catch (Exception e)
        {
            // 记录详细错误信息
            throw new RuntimeException("创建物资字典记录时发生错误：" + e.getMessage(), e);
        }
    }

    /**
     * 修改产品证件信息
     * 
     * @param productCertificate 证件信息
     * @return 结果
     */
    @Override
    public int updateProductCertificate(ProductCertificate productCertificate)
    {
        productCertificate.setUpdateTime(DateUtils.getNowDate());
        // 检查过期状态
        checkExpiredStatus(productCertificate);
        return productCertificateMapper.updateProductCertificate(productCertificate);
    }
    
    /**
     * 修改产品证件信息（带产品详细信息）
     * 
     * @param productCertificate 证件信息
     * @param specification 规格
     * @param model 型号
     * @param unit 单位
     * @param manufacturerName 生产厂家
     * @param purchasePrice 采购价格
     * @return 结果
     */
    public int updateProductCertificate(ProductCertificate productCertificate,
                                       String specification, String model, String unit,
                                       String manufacturerName, java.math.BigDecimal purchasePrice)
    {
        productCertificate.setUpdateTime(DateUtils.getNowDate());
        // 检查过期状态
        checkExpiredStatus(productCertificate);
        
        // 更新ProductCertificate
        int result = productCertificateMapper.updateProductCertificate(productCertificate);
        
        // 如果materialId不为空，同时更新MaterialDict
        if (productCertificate.getMaterialId() != null && productCertificate.getMaterialId() > 0)
        {
            com.scm.system.domain.MaterialDict materialDict = materialDictService.selectMaterialDictById(productCertificate.getMaterialId());
            if (materialDict != null)
            {
                boolean needUpdate = false;
                if (StringUtils.isNotEmpty(specification) && !specification.equals(materialDict.getSpecification()))
                {
                    materialDict.setSpecification(specification);
                    needUpdate = true;
                }
                if (StringUtils.isNotEmpty(model) && !model.equals(materialDict.getModel()))
                {
                    materialDict.setModel(model);
                    needUpdate = true;
                }
                if (StringUtils.isNotEmpty(unit) && !unit.equals(materialDict.getUnit()))
                {
                    materialDict.setUnit(unit);
                    needUpdate = true;
                }
                if (purchasePrice != null && !purchasePrice.equals(materialDict.getPurchasePrice()))
                {
                    materialDict.setPurchasePrice(purchasePrice);
                    needUpdate = true;
                }
                // 处理生产厂家：根据manufacturerName查找或创建厂家，然后更新manufacturerId
                if (StringUtils.isNotEmpty(manufacturerName))
                {
                    Long manufacturerId = findOrCreateManufacturer(manufacturerName, productCertificate.getUpdateBy());
                    if (manufacturerId != null && (materialDict.getManufacturerId() == null || !manufacturerId.equals(materialDict.getManufacturerId())))
                    {
                        materialDict.setManufacturerId(manufacturerId);
                        needUpdate = true;
                    }
                }
                
                if (needUpdate)
                {
                    materialDict.setUpdateBy(productCertificate.getUpdateBy());
                    materialDict.setUpdateTime(DateUtils.getNowDate());
                    materialDictService.updateMaterialDict(materialDict);
                }
            }
        }
        
        return result;
    }

    /**
     * 批量删除产品证件信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    @Override
    public int deleteProductCertificateByIds(String ids)
    {
        return productCertificateMapper.deleteProductCertificateByIds(Convert.toStrArray(ids));
    }

    /**
     * 删除产品证件信息
     * 
     * @param certificateId 证件ID
     * @return 结果
     */
    @Override
    public int deleteProductCertificateById(Long certificateId)
    {
        return productCertificateMapper.deleteProductCertificateById(certificateId);
    }

    /**
     * 审核产品证件
     * 
     * @param productCertificate 证件信息
     * @return 结果
     */
    @Override
    public int auditProductCertificate(ProductCertificate productCertificate)
    {
        productCertificate.setAuditTime(DateUtils.getNowDate());
        productCertificate.setUpdateTime(DateUtils.getNowDate());
        return productCertificateMapper.updateProductCertificate(productCertificate);
    }

    /**
     * 检查并更新证件过期状态
     */
    @Override
    public void checkAndUpdateExpiredStatus()
    {
        List<ProductCertificate> certificates = productCertificateMapper.selectProductCertificateList(new ProductCertificate());
        Date now = DateUtils.getNowDate();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.DAY_OF_MONTH, 30); // 默认预警天数30天
        Date warningDate = calendar.getTime();

        for (ProductCertificate certificate : certificates)
        {
            boolean needUpdate = false;
            if (certificate.getExpireDate() != null)
            {
                if (certificate.getExpireDate().before(now))
                {
                    if (!"1".equals(certificate.getIsExpired()))
                    {
                        certificate.setIsExpired("1");
                        needUpdate = true;
                    }
                }
                else
                {
                    if ("1".equals(certificate.getIsExpired()))
                    {
                        certificate.setIsExpired("0");
                        needUpdate = true;
                    }
                }

                if (certificate.getExpireDate().before(warningDate) && certificate.getExpireDate().after(now))
                {
                    if (!"1".equals(certificate.getIsWarning()))
                    {
                        certificate.setIsWarning("1");
                        needUpdate = true;
                    }
                }
                else
                {
                    if ("1".equals(certificate.getIsWarning()))
                    {
                        certificate.setIsWarning("0");
                        needUpdate = true;
                    }
                }
            }

            if (needUpdate)
            {
                productCertificateMapper.updateProductCertificate(certificate);
            }
        }
    }

    /**
     * 检查证件过期状态
     * 
     * @param certificate 证件信息
     */
    private void checkExpiredStatus(ProductCertificate certificate)
    {
        if (certificate.getExpireDate() != null)
        {
            Date now = DateUtils.getNowDate();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(now);
            calendar.add(Calendar.DAY_OF_MONTH, 30); // 默认预警天数30天
            Date warningDate = calendar.getTime();

            if (certificate.getExpireDate().before(now))
            {
                certificate.setIsExpired("1");
            }
            else
            {
                certificate.setIsExpired("0");
            }

            if (certificate.getExpireDate().before(warningDate) && certificate.getExpireDate().after(now))
            {
                certificate.setIsWarning("1");
            }
            else
            {
                certificate.setIsWarning("0");
            }
        }
    }
    
    /**
     * 根据厂家名称查找或创建厂家记录
     * 
     * @param manufacturerName 厂家名称
     * @param createBy 创建者
     * @return 厂家ID
     */
    private Long findOrCreateManufacturer(String manufacturerName, String createBy)
    {
        if (StringUtils.isEmpty(manufacturerName))
        {
            return null;
        }
        
        // 先查找是否存在
        Long manufacturerId = productCertificateMapper.findManufacturerByName(manufacturerName);
        if (manufacturerId != null)
        {
            return manufacturerId;
        }
        
        // 不存在则创建
        productCertificateMapper.insertManufacturer(manufacturerName, createBy);
        // 获取最后插入的ID
        manufacturerId = productCertificateMapper.getLastInsertManufacturerId();
        return manufacturerId;
    }
}

