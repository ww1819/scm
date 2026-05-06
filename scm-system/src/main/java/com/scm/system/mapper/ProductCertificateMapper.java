package com.scm.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.scm.system.domain.ProductCertificate;
import com.scm.system.domain.vo.ProductMaterialArchiveVo;

/**
 * 产品证件 数据层
 * 
 * @author scm
 */
public interface ProductCertificateMapper
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
     * 某供应商在某医院下，按物资聚合的产品档案摘要
     */
    public List<ProductMaterialArchiveVo> selectMaterialArchiveSummaryList(@Param("supplierId") Long supplierId,
        @Param("hospitalCode") String hospitalCode);

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
     * 修改产品证件信息
     * 
     * @param productCertificate 证件信息
     * @return 结果
     */
    public int updateProductCertificate(ProductCertificate productCertificate);

    /**
     * 仅更新证照图片字段
     */
    public int updateProductCertificateFile(ProductCertificate productCertificate);

    /**
     * 删除产品证件信息
     * 
     * @param certificateId 证件主键
     * @return 结果
     */
    public int deleteProductCertificateById(Long certificateId);

    /**
     * 批量删除产品证件信息
     * 
     * @param certificateIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteProductCertificateByIds(String[] certificateIds);
    
    /**
     * 根据厂家名称查找厂家ID
     * 
     * @param manufacturerName 厂家名称
     * @return 厂家ID
     */
    public Long findManufacturerByName(String manufacturerName);
    
    /**
     * 创建厂家
     * 
     * @param manufacturerName 厂家名称
     * @param createBy 创建者
     * @return 影响行数
     */
    public int insertManufacturer(@Param("manufacturerName") String manufacturerName, @Param("createBy") String createBy);
    
    /**
     * 获取最后插入的厂家ID
     * 
     * @return 厂家ID
     */
    public Long getLastInsertManufacturerId();

    /**
     * 某物资下未删除的产品证件主键（用于同步扩展证照占位）
     */
    public List<Long> selectCertificateIdsByMaterialId(@Param("materialId") Long materialId);
}

