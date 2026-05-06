package com.scm.system.service;

import java.util.List;
import com.scm.system.domain.ProductCertificate;
import com.scm.system.domain.vo.ProductMaterialArchiveVo;

/**
 * 产品证件 服务层
 * 
 * @author scm
 */
public interface IProductCertificateService
{
    /**
     * 查询产品证件信息
     * 
     * @param certificateId 证件ID
     * @return 证件信息
     */
    public ProductCertificate selectProductCertificateById(Long certificateId);

    /**
     * 查询产品证件列表
     * 
     * @param productCertificate 证件信息
     * @return 证件集合
     */
    public List<ProductCertificate> selectProductCertificateList(ProductCertificate productCertificate);

    /**
     * 校验当前登录供应商与医院编码是否允许进入「医院产品档案」查询
     */
    public void ensureProductMaterialArchiveAccess(String hospitalCode);

    /**
     * 已校验前提下：某供应商在某医院下的物资聚合摘要（供分页查询使用）
     */
    public List<ProductMaterialArchiveVo> selectMaterialArchiveSummaryData(Long supplierId, String hospitalCode);

    /**
     * 查询过期预警的产品证件列表
     * 
     * @param productCertificate 证件信息
     * @return 证件集合
     */
    public List<ProductCertificate> selectExpiringCertificateList(ProductCertificate productCertificate);

    /**
     * 新增产品证件信息
     * 
     * @param productCertificate 证件信息
     * @return 结果
     */
    public int insertProductCertificate(ProductCertificate productCertificate);
    
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
                                       String manufacturerName, java.math.BigDecimal purchasePrice);

    /**
     * 修改产品证件信息
     * 
     * @param productCertificate 证件信息
     * @return 结果
     */
    public int updateProductCertificate(ProductCertificate productCertificate);
    
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
                                       String manufacturerName, java.math.BigDecimal purchasePrice);

    /**
     * 批量删除产品证件信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteProductCertificateByIds(String ids);

    /**
     * 删除产品证件信息
     * 
     * @param certificateId 证件ID
     * @return 结果
     */
    public int deleteProductCertificateById(Long certificateId);

    /**
     * 审核产品证件
     * 
     * @param productCertificate 证件信息
     * @return 结果
     */
    public int auditProductCertificate(ProductCertificate productCertificate);

    /**
     * 检查并更新证件过期状态
     */
    public void checkAndUpdateExpiredStatus();

    /**
     * 仅更新产品证照图片
     */
    public int updateProductCertificateFile(Long certificateId, String certificateFile, String updateBy);
}

