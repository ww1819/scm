package com.scm.system.service.impl;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import com.scm.common.core.text.Convert;
import com.scm.common.exception.ServiceException;
import com.scm.common.utils.DateUtils;
import com.scm.common.utils.ShiroUtils;
import com.scm.common.utils.StringUtils;
import com.scm.system.domain.HospitalSupplier;
import com.scm.system.domain.OrderDetail;
import com.scm.system.domain.ProductCertificate;
import com.scm.system.domain.ScmFile;
import com.scm.system.domain.Supplier;
import com.scm.system.domain.vo.ProductCertificateImportVo;
import com.scm.system.domain.vo.ProductMaterialArchiveVo;
import com.scm.system.mapper.OrderDetailMapper;
import com.scm.system.mapper.ProductCertificateMapper;
import com.scm.system.mapper.SupplierMapper;
import com.scm.system.service.IHospitalSupplierService;
import com.scm.system.service.IProductCertLicenseSnapService;
import com.scm.system.service.IProductCertificateService;
import com.scm.system.service.IScmProductCertificateFileService;
import com.scm.system.service.IScmSupplierContextService;

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

    @Autowired
    private IScmSupplierContextService scmSupplierContextService;

    @Autowired
    private IHospitalSupplierService hospitalSupplierService;

    @Autowired
    @Lazy
    private IProductCertLicenseSnapService productCertLicenseSnapService;

    @Autowired
    private IScmProductCertificateFileService scmProductCertificateFileService;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private SupplierMapper supplierMapper;

    private void enrichCertificateFiles(ProductCertificate certificate)
    {
        if (certificate == null || certificate.getCertificateId() == null)
        {
            return;
        }
        List<ScmFile> files = scmProductCertificateFileService.selectFilesByCertificateId(certificate.getCertificateId());
        certificate.setCertificateFiles(files);
        if (files != null && !files.isEmpty())
        {
            certificate.setCertificateFileIds(scmProductCertificateFileService.buildFileIdsCsv(files));
            certificate.setCertificateFile(scmProductCertificateFileService.buildFileUrlsCsv(files));
        }
    }

    private void saveCertificateFilesIfNeeded(ProductCertificate certificate, String operBy)
    {
        if (certificate == null || certificate.getCertificateId() == null)
        {
            return;
        }
        if (certificate.getCertificateFileIds() != null)
        {
            scmProductCertificateFileService.replaceCertificateFiles(
                    certificate.getCertificateId(), certificate.getCertificateFileIds(), operBy);
        }
    }

    private void ensureSnapStubsAfterCertChange(Long certificateId, String createOrUpdateBy)
    {
        if (certificateId == null)
        {
            return;
        }
        try
        {
            productCertLicenseSnapService.ensureProductSnapStubsForCertificate(certificateId,
                createOrUpdateBy != null ? createOrUpdateBy : "");
        }
        catch (Exception ignored)
        {
            // 权限或类型表未就绪时不阻断主流程
        }
    }

    private void assertProductCertificateSupplierScope(ProductCertificate c)
    {
        Long sid = scmSupplierContextService.resolveSupplierIdForUser(ShiroUtils.getUserId());
        if (sid != null && c != null && c.getSupplierId() != null && !sid.equals(c.getSupplierId()))
        {
            throw new ServiceException("无权操作其他供应商的产品证件");
        }
    }

    private void assertProductCertificateEditable(ProductCertificate certificate)
    {
        if (certificate != null && "1".equals(certificate.getAuditStatus()))
        {
            throw new ServiceException("已审核的产品不允许修改");
        }
    }

    private void applySupplierListScope(ProductCertificate q)
    {
        Long sid = scmSupplierContextService.resolveSupplierIdForUser(ShiroUtils.getUserId());
        if (sid != null)
        {
            q.setSupplierId(sid);
            assertHospitalCodeAllowedForSupplier(sid, q.getHospitalCode());
        }
    }

    private void assertHospitalCodeAllowedForSupplier(Long supplierId, String hospitalCode)
    {
        if (supplierId == null || StringUtils.isEmpty(hospitalCode))
        {
            return;
        }
        assertHospitalLinkedToSupplier(supplierId, hospitalCode.trim());
    }

    private void assertHospitalLinkedToSupplier(Long supplierId, String hospitalCode)
    {
        List<HospitalSupplier> list = hospitalSupplierService.selectSupplierLinkedHospitalsForProduct(supplierId);
        for (HospitalSupplier hs : list)
        {
            if (hospitalCode.equals(hs.getHospitalCode()))
            {
                return;
            }
        }
        throw new ServiceException("无权操作：所选医院与当前供应商无有效关联");
    }

    /**
     * 新增时医院必填：供应商须选择关联医院（编码+ID）；平台须填写医院编码。
     */
    private void assertHospitalRequiredOnInsert(ProductCertificate productCertificate, Long supplierUserSid)
    {
        if (supplierUserSid != null)
        {
            if (StringUtils.isEmpty(StringUtils.trimToNull(productCertificate.getHospitalCode())))
            {
                throw new ServiceException("请选择医院");
            }
            if (StringUtils.isEmpty(StringUtils.trimToNull(productCertificate.getHospitalId())))
            {
                throw new ServiceException("请选择医院");
            }
            assertHospitalLinkedToSupplier(supplierUserSid, productCertificate.getHospitalCode().trim());
        }
        else if (StringUtils.isEmpty(StringUtils.trimToNull(productCertificate.getHospitalCode())))
        {
            throw new ServiceException("请填写医院编码");
        }
    }

    /** 修改时不允许变更医院，始终以库中原值为准。 */
    private void preserveHospitalOnUpdate(ProductCertificate incoming, ProductCertificate before)
    {
        if (before == null || incoming == null)
        {
            return;
        }
        incoming.setHospitalCode(before.getHospitalCode());
        incoming.setHospitalId(before.getHospitalId());
    }

    /**
     * 查询产品证件信息
     * 
     * @param certificateId 证件ID
     * @return 证件信息
     */
    @Override
    public ProductCertificate selectProductCertificateById(Long certificateId)
    {
        ProductCertificate c = productCertificateMapper.selectProductCertificateById(certificateId);
        if (c != null)
        {
            assertProductCertificateSupplierScope(c);
            enrichCertificateFiles(c);
        }
        return c;
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
        applySupplierListScope(productCertificate);
        List<ProductCertificate> list = productCertificateMapper.selectProductCertificateList(productCertificate);
        enrichCertificateFilesBatch(list);
        return list;
    }

    private void enrichCertificateFilesBatch(List<ProductCertificate> list)
    {
        if (list == null || list.isEmpty())
        {
            return;
        }
        for (ProductCertificate certificate : list)
        {
            enrichCertificateFiles(certificate);
        }
    }

    @Override
    public void ensureProductMaterialArchiveAccess(String hospitalCode)
    {
        Long sid = scmSupplierContextService.resolveSupplierIdForUser(ShiroUtils.getUserId());
        if (sid == null)
        {
            throw new ServiceException("仅供应商账号可使用医院产品档案");
        }
        if (StringUtils.isEmpty(hospitalCode))
        {
            throw new ServiceException("请选择医院");
        }
        assertHospitalLinkedToSupplier(sid, hospitalCode.trim());
    }

    @Override
    public List<ProductMaterialArchiveVo> selectMaterialArchiveSummaryData(Long supplierId, String hospitalCode)
    {
        if (supplierId == null || StringUtils.isEmpty(hospitalCode))
        {
            return Collections.emptyList();
        }
        return productCertificateMapper.selectMaterialArchiveSummaryList(supplierId, hospitalCode.trim());
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
        applySupplierListScope(productCertificate);
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
        Long sid = scmSupplierContextService.resolveSupplierIdForUser(ShiroUtils.getUserId());
        if (sid != null)
        {
            productCertificate.setSupplierId(sid);
        }
        assertHospitalRequiredOnInsert(productCertificate, sid);
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
        int rows = productCertificateMapper.insertProductCertificate(productCertificate);
        if (rows > 0 && productCertificate.getCertificateId() != null)
        {
            saveCertificateFilesIfNeeded(productCertificate, productCertificate.getCreateBy());
            ensureSnapStubsAfterCertChange(productCertificate.getCertificateId(), productCertificate.getCreateBy());
        }
        return rows;
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
        ProductCertificate before = productCertificateMapper.selectProductCertificateById(productCertificate.getCertificateId());
        if (before != null)
        {
            assertProductCertificateSupplierScope(before);
            assertProductCertificateEditable(before);
        }
        preserveHospitalOnUpdate(productCertificate, before);
        Long sid = scmSupplierContextService.resolveSupplierIdForUser(ShiroUtils.getUserId());
        if (sid != null)
        {
            productCertificate.setSupplierId(sid);
            String hc = productCertificate.getHospitalCode();
            if (StringUtils.isNotEmpty(hc))
            {
                assertHospitalLinkedToSupplier(sid, hc.trim());
            }
        }
        productCertificate.setUpdateTime(DateUtils.getNowDate());
        // 检查过期状态
        checkExpiredStatus(productCertificate);
        int rows = productCertificateMapper.updateProductCertificate(productCertificate);
        if (rows > 0 && productCertificate.getCertificateId() != null)
        {
            ensureSnapStubsAfterCertChange(productCertificate.getCertificateId(), productCertificate.getUpdateBy());
        }
        return rows;
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
        ProductCertificate before = productCertificateMapper.selectProductCertificateById(productCertificate.getCertificateId());
        if (before != null)
        {
            assertProductCertificateSupplierScope(before);
            assertProductCertificateEditable(before);
        }
        preserveHospitalOnUpdate(productCertificate, before);
        Long sid = scmSupplierContextService.resolveSupplierIdForUser(ShiroUtils.getUserId());
        if (sid != null)
        {
            productCertificate.setSupplierId(sid);
            String hc = productCertificate.getHospitalCode();
            if (StringUtils.isNotEmpty(hc))
            {
                assertHospitalLinkedToSupplier(sid, hc.trim());
            }
        }
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
        if (result > 0 && productCertificate.getCertificateId() != null)
        {
            saveCertificateFilesIfNeeded(productCertificate, productCertificate.getUpdateBy());
            ensureSnapStubsAfterCertChange(productCertificate.getCertificateId(), productCertificate.getUpdateBy());
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
        String[] arr = Convert.toStrArray(ids);
        for (String id : arr)
        {
            Long certId = Long.parseLong(id);
            ProductCertificate before = productCertificateMapper.selectProductCertificateById(certId);
            if (before != null)
            {
                assertProductCertificateSupplierScope(before);
            }
            scmProductCertificateFileService.deleteByCertificateId(certId);
        }
        return productCertificateMapper.deleteProductCertificateByIds(arr);
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
        ProductCertificate before = productCertificateMapper.selectProductCertificateById(certificateId);
        if (before != null)
        {
            assertProductCertificateSupplierScope(before);
        }
        scmProductCertificateFileService.deleteByCertificateId(certificateId);
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
        ProductCertificate before = productCertificateMapper.selectProductCertificateById(productCertificate.getCertificateId());
        if (before != null)
        {
            assertProductCertificateSupplierScope(before);
            if ("1".equals(before.getAuditStatus()))
            {
                throw new ServiceException("该产品已审核，不能重复审核");
            }
        }
        productCertificate.setAuditTime(DateUtils.getNowDate());
        productCertificate.setUpdateTime(DateUtils.getNowDate());
        return productCertificateMapper.updateProductCertificate(productCertificate);
    }

    @Override
    public int updateProductCertificateFile(Long certificateId, String certificateFileIds, String updateBy)
    {
        ProductCertificate before = productCertificateMapper.selectProductCertificateById(certificateId);
        if (before == null)
        {
            throw new ServiceException("产品证件不存在");
        }
        assertProductCertificateSupplierScope(before);
        assertProductCertificateEditable(before);
        ProductCertificate bind = new ProductCertificate();
        bind.setCertificateId(certificateId);
        bind.setCertificateFileIds(certificateFileIds != null ? certificateFileIds : "");
        saveCertificateFilesIfNeeded(bind, updateBy);
        return 1;
    }

    /**
     * 检查并更新证件过期状态
     */
    @Override
    public void checkAndUpdateExpiredStatus()
    {
        ProductCertificate scopeQuery = new ProductCertificate();
        applySupplierListScope(scopeQuery);
        List<ProductCertificate> certificates = productCertificateMapper.selectProductCertificateList(scopeQuery);
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

    @Override
    public List<OrderDetail> selectOrderCatalogList(String hospitalId, Long supplierId)
    {
        if (StringUtils.isEmpty(hospitalId))
        {
            throw new ServiceException("请选择医院");
        }
        Long hid;
        try
        {
            hid = Long.valueOf(hospitalId.trim());
        }
        catch (NumberFormatException e)
        {
            throw new ServiceException("医院ID无效");
        }
        Long sid = scmSupplierContextService.resolveSupplierIdForUser(ShiroUtils.getUserId());
        if (sid != null)
        {
            supplierId = sid;
        }
        return orderDetailMapper.selectDistinctOrderCatalog(hid, supplierId);
    }

    @Override
    public String syncOrderCatalogToProducts(String hospitalId, String hospitalCode, Long supplierId, String operName)
    {
        List<OrderDetail> rawList = selectOrderCatalogList(hospitalId, supplierId);
        if (rawList == null || rawList.isEmpty())
        {
            throw new ServiceException("当前医院订单中暂无产品目录可同步");
        }
        String hid = hospitalId.trim();
        String hcode = StringUtils.trimToEmpty(hospitalCode);
        Long supplierUserSid = scmSupplierContextService.resolveSupplierIdForUser(ShiroUtils.getUserId());
        if (supplierUserSid != null)
        {
            supplierId = supplierUserSid;
            if (StringUtils.isNotEmpty(hcode))
            {
                assertHospitalLinkedToSupplier(supplierUserSid, hcode);
            }
        }
        Map<String, OrderDetail> catalogMap = dedupeOrderCatalogByMaterialCode(rawList, supplierId);
        int insertNum = 0;
        int updateNum = 0;
        int skipNum = 0;
        for (OrderDetail row : catalogMap.values())
        {
            if (row == null || StringUtils.isEmpty(StringUtils.trimToNull(row.getMaterialCode())))
            {
                skipNum++;
                continue;
            }
            if (StringUtils.isEmpty(StringUtils.trimToNull(row.getMaterialName())))
            {
                skipNum++;
                continue;
            }
            Long rowSupplierId = supplierId != null ? supplierId : row.getSupplierId();
            if (rowSupplierId == null)
            {
                skipNum++;
                continue;
            }
            String materialCode = row.getMaterialCode().trim();
            ProductCertificate existing = productCertificateMapper.selectProductCertificateByMaterialCode(
                hid, rowSupplierId, materialCode);
            if (existing != null)
            {
                assertProductCertificateSupplierScope(existing);
                if ("1".equals(existing.getAuditStatus()))
                {
                    skipNum++;
                    continue;
                }
                applyOrderRowToCertificate(existing, row, operName, true);
                updateProductCertificate(existing, row.getSpecification(), row.getModel(), row.getUnit(),
                    row.getManufacturer(), row.getPurchasePrice());
                updateNum++;
            }
            else
            {
                ProductCertificate cert = buildCertificateFromOrderRow(row, hid, hcode, rowSupplierId, operName);
                Long materialId = resolveOrCreateMaterialDictFromOrder(row, operName);
                cert.setMaterialId(materialId);
                insertProductCertificate(cert, row.getSpecification(), row.getModel(), row.getUnit(),
                    row.getManufacturer(), row.getPurchasePrice());
                insertNum++;
            }
        }
        return "同步完成：新增 " + insertNum + " 条，更新 " + updateNum + " 条"
            + (skipNum > 0 ? "，跳过 " + skipNum + " 条" : "");
    }

    /** 按耗材编码+供应商去重，保留最新订单明细并合并非空字段 */
    private Map<String, OrderDetail> dedupeOrderCatalogByMaterialCode(List<OrderDetail> rawList, Long defaultSupplierId)
    {
        Map<String, OrderDetail> map = new LinkedHashMap<>();
        for (OrderDetail row : rawList)
        {
            if (row == null || StringUtils.isEmpty(StringUtils.trimToNull(row.getMaterialCode())))
            {
                continue;
            }
            Long sid = defaultSupplierId != null ? defaultSupplierId : row.getSupplierId();
            String key = row.getMaterialCode().trim() + "@" + (sid != null ? sid : "");
            OrderDetail acc = map.get(key);
            if (acc == null)
            {
                map.put(key, row);
            }
            else
            {
                mergeOrderCatalogFields(acc, row);
            }
        }
        return map;
    }

    private void mergeOrderCatalogFields(OrderDetail target, OrderDetail incoming)
    {
        if (StringUtils.isNotEmpty(StringUtils.trimToNull(incoming.getMaterialName())))
        {
            target.setMaterialName(incoming.getMaterialName().trim());
        }
        if (StringUtils.isNotEmpty(StringUtils.trimToNull(incoming.getRegisterNo())))
        {
            target.setRegisterNo(incoming.getRegisterNo().trim());
        }
        if (StringUtils.isNotEmpty(StringUtils.trimToNull(incoming.getSpecification())))
        {
            target.setSpecification(incoming.getSpecification().trim());
        }
        if (StringUtils.isNotEmpty(StringUtils.trimToNull(incoming.getModel())))
        {
            target.setModel(incoming.getModel().trim());
        }
        if (StringUtils.isNotEmpty(StringUtils.trimToNull(incoming.getUnit())))
        {
            target.setUnit(incoming.getUnit().trim());
        }
        if (incoming.getPurchasePrice() != null)
        {
            target.setPurchasePrice(incoming.getPurchasePrice());
        }
        if (StringUtils.isNotEmpty(StringUtils.trimToNull(incoming.getManufacturer())))
        {
            target.setManufacturer(incoming.getManufacturer().trim());
        }
    }

    private ProductCertificate buildCertificateFromOrderRow(OrderDetail row, String hospitalId, String hospitalCode,
        Long supplierId, String operName)
    {
        ProductCertificate cert = new ProductCertificate();
        cert.setMaterialName(row.getMaterialName().trim());
        cert.setRegisterNo(StringUtils.trimToNull(row.getRegisterNo()));
        cert.setHospitalId(hospitalId);
        cert.setHospitalCode(hospitalCode);
        cert.setSupplierId(supplierId);
        cert.setProductCategory("低值");
        cert.setCreateBy(operName);
        if (row.getPurchasePrice() != null)
        {
            cert.setBidPrice(row.getPurchasePrice());
        }
        return cert;
    }

    private void applyOrderRowToCertificate(ProductCertificate cert, OrderDetail row, String operName, boolean updating)
    {
        cert.setUpdateBy(operName);
        if (StringUtils.isNotEmpty(StringUtils.trimToNull(row.getMaterialName())))
        {
            cert.setMaterialName(row.getMaterialName().trim());
        }
        if (StringUtils.isNotEmpty(StringUtils.trimToNull(row.getRegisterNo())))
        {
            cert.setRegisterNo(row.getRegisterNo().trim());
        }
        if (row.getPurchasePrice() != null)
        {
            cert.setBidPrice(row.getPurchasePrice());
        }
        if (updating && cert.getMaterialId() != null && cert.getMaterialId() > 0
            && StringUtils.isNotEmpty(StringUtils.trimToNull(row.getMaterialName())))
        {
            com.scm.system.domain.MaterialDict materialDict = materialDictService.selectMaterialDictById(cert.getMaterialId());
            if (materialDict != null && !row.getMaterialName().trim().equals(materialDict.getMaterialName()))
            {
                materialDict.setMaterialName(row.getMaterialName().trim());
                materialDict.setUpdateBy(operName);
                materialDict.setUpdateTime(DateUtils.getNowDate());
                materialDictService.updateMaterialDict(materialDict);
            }
        }
    }

    private Long resolveOrCreateMaterialDictFromOrder(OrderDetail row, String operName)
    {
        String materialCode = row.getMaterialCode().trim();
        com.scm.system.domain.MaterialDict dict = materialDictService.selectMaterialDictByCode(materialCode);
        if (dict == null)
        {
            dict = new com.scm.system.domain.MaterialDict();
            dict.setMaterialCode(materialCode);
            dict.setMaterialName(row.getMaterialName().trim());
            dict.setSpecification(row.getSpecification());
            dict.setModel(row.getModel());
            dict.setUnit(row.getUnit());
            dict.setPurchasePrice(row.getPurchasePrice());
            if (StringUtils.isNotEmpty(StringUtils.trimToNull(row.getManufacturer())))
            {
                Long manufacturerId = findOrCreateManufacturer(row.getManufacturer().trim(), operName);
                dict.setManufacturerId(manufacturerId);
            }
            dict.setStatus("0");
            dict.setDelFlag("0");
            dict.setCreateBy(operName);
            materialDictService.insertMaterialDict(dict);
            return dict.getMaterialId();
        }
        boolean needUpdate = false;
        if (StringUtils.isNotEmpty(StringUtils.trimToNull(row.getMaterialName()))
            && !row.getMaterialName().trim().equals(dict.getMaterialName()))
        {
            dict.setMaterialName(row.getMaterialName().trim());
            needUpdate = true;
        }
        if (StringUtils.isNotEmpty(StringUtils.trimToNull(row.getSpecification()))
            && !row.getSpecification().equals(dict.getSpecification()))
        {
            dict.setSpecification(row.getSpecification());
            needUpdate = true;
        }
        if (StringUtils.isNotEmpty(StringUtils.trimToNull(row.getModel()))
            && !row.getModel().equals(dict.getModel()))
        {
            dict.setModel(row.getModel());
            needUpdate = true;
        }
        if (StringUtils.isNotEmpty(StringUtils.trimToNull(row.getUnit()))
            && !row.getUnit().equals(dict.getUnit()))
        {
            dict.setUnit(row.getUnit());
            needUpdate = true;
        }
        if (row.getPurchasePrice() != null && !row.getPurchasePrice().equals(dict.getPurchasePrice()))
        {
            dict.setPurchasePrice(row.getPurchasePrice());
            needUpdate = true;
        }
        if (StringUtils.isNotEmpty(StringUtils.trimToNull(row.getManufacturer())))
        {
            Long manufacturerId = findOrCreateManufacturer(row.getManufacturer().trim(), operName);
            if (manufacturerId != null && !manufacturerId.equals(dict.getManufacturerId()))
            {
                dict.setManufacturerId(manufacturerId);
                needUpdate = true;
            }
        }
        if (needUpdate)
        {
            dict.setUpdateBy(operName);
            dict.setUpdateTime(DateUtils.getNowDate());
            materialDictService.updateMaterialDict(dict);
        }
        return dict.getMaterialId();
    }

    @Override
    public String importProductCatalog(List<ProductCertificateImportVo> rows, String hospitalId,
        String hospitalCode, boolean updateSupport, String operName)
    {
        if (rows == null || rows.isEmpty())
        {
            throw new ServiceException("导入数据不能为空");
        }
        if (StringUtils.isEmpty(hospitalId))
        {
            throw new ServiceException("请先在左侧选择医院");
        }
        String hid = hospitalId.trim();
        String hcode = StringUtils.trimToEmpty(hospitalCode);
        Long supplierUserSid = scmSupplierContextService.resolveSupplierIdForUser(ShiroUtils.getUserId());
        if (supplierUserSid != null && StringUtils.isNotEmpty(hcode))
        {
            assertHospitalLinkedToSupplier(supplierUserSid, hcode);
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for (ProductCertificateImportVo row : rows)
        {
            try
            {
                if (row == null || StringUtils.isEmpty(StringUtils.trimToNull(row.getMaterialName())))
                {
                    failureNum++;
                    failureMsg.append("<br/>").append(failureNum).append("、产品名称不能为空");
                    continue;
                }
                if (StringUtils.isEmpty(StringUtils.trimToNull(row.getRegisterNo())))
                {
                    failureNum++;
                    failureMsg.append("<br/>").append(failureNum).append("、").append(row.getMaterialName()).append("：注册证号不能为空");
                    continue;
                }
                ProductCertificate cert = new ProductCertificate();
                cert.setMaterialName(row.getMaterialName().trim());
                cert.setRegisterNo(row.getRegisterNo().trim());
                cert.setRegisterName(row.getRegisterName());
                cert.setUdiCode(row.getUdiCode());
                cert.setRegisterIssueDate(row.getRegisterIssueDate());
                cert.setExpireDate(row.getExpireDate());
                cert.setProductCategory(row.getProductCategory());
                cert.setHospitalId(hid);
                cert.setHospitalCode(hcode);
                cert.setCreateBy(operName);
                if (supplierUserSid != null)
                {
                    cert.setSupplierId(supplierUserSid);
                }
                else if (StringUtils.isNotEmpty(StringUtils.trimToNull(row.getSupplierName())))
                {
                    Supplier supplier = supplierMapper.selectSupplierByCompanyName(row.getSupplierName().trim());
                    if (supplier != null)
                    {
                        cert.setSupplierId(supplier.getSupplierId());
                    }
                }
                ProductCertificate query = new ProductCertificate();
                query.setHospitalId(hid);
                query.setHospitalCode(hcode);
                query.setRegisterNo(cert.getRegisterNo());
                if (cert.getSupplierId() != null)
                {
                    query.setSupplierId(cert.getSupplierId());
                }
                applySupplierListScope(query);
                List<ProductCertificate> exists = productCertificateMapper.selectProductCertificateList(query);
                if (exists != null && !exists.isEmpty())
                {
                    if (!updateSupport)
                    {
                        failureNum++;
                        failureMsg.append("<br/>").append(failureNum).append("、").append(cert.getMaterialName())
                            .append("（").append(cert.getRegisterNo()).append("）已存在");
                        continue;
                    }
                    ProductCertificate before = exists.get(0);
                    cert.setCertificateId(before.getCertificateId());
                    cert.setMaterialId(before.getMaterialId());
                    cert.setUpdateBy(operName);
                    updateProductCertificate(cert, row.getSpecification(), row.getModel(), row.getUnit(),
                        row.getManufacturerName(), row.getPurchasePrice());
                }
                else
                {
                    insertProductCertificate(cert, row.getSpecification(), row.getModel(), row.getUnit(),
                        row.getManufacturerName(), row.getPurchasePrice());
                }
                successNum++;
                successMsg.append("<br/>").append(successNum).append("、").append(cert.getMaterialName()).append(" 导入成功");
            }
            catch (Exception e)
            {
                failureNum++;
                String name = row != null && row.getMaterialName() != null ? row.getMaterialName() : "未知产品";
                failureMsg.append("<br/>").append(failureNum).append("、").append(name).append(" 导入失败：")
                    .append(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
            }
        }
        if (failureNum > 0)
        {
            failureMsg.insert(0, "导入完成，成功 " + successNum + " 条，失败 " + failureNum + " 条，错误如下：");
            throw new ServiceException(failureMsg.toString());
        }
        successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：");
        return successMsg.toString();
    }
}

