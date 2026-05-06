package com.scm.system.service;

import java.util.List;
import com.scm.system.domain.ProductCertLicenseSnap;

/**
 * 产品证件扩展证照快照（与 scm_certificate_type / scm_certificate_config 联动）
 */
public interface IProductCertLicenseSnapService
{
    List<ProductCertLicenseSnap> selectListByCertificateId(Long certificateId);

    ProductCertLicenseSnap selectByLicenseId(String licenseId);

    int insertSnap(ProductCertLicenseSnap row, String loginName);

    int updateSnap(ProductCertLicenseSnap row, String loginName);

    int deleteByIds(String licenseIds, String loginName);

    /** 按配置为指定产品证件补齐缺失的扩展证照行 */
    void ensureProductSnapStubsForCertificate(Long certificateId, String loginName);

    /** 某物资下所有产品证件补齐扩展证照占位 */
    void ensureProductSnapStubsForMaterial(Long materialId, String loginName);

    /**
     * 合并展示：先补齐占位，再按证件类型配置顺序返回行（含历史多余类型排在末尾）
     */
    List<ProductCertLicenseSnap> listMergedForCertificate(Long certificateId, String loginName);

    /** 登记页单行保存（更新证号、日期、附件等） */
    int saveSnapRow(ProductCertLicenseSnap row, String loginName);
}
