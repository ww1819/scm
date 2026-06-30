package com.scm.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.scm.system.domain.ProductCertLicenseSnap;

/**
 * 产品证件扩展证照快照
 */
public interface ProductCertLicenseSnapMapper
{
    ProductCertLicenseSnap selectById(String licenseId);

    List<ProductCertLicenseSnap> selectListByCertificateId(String certificateId);

    ProductCertLicenseSnap selectByCertIdAndKind(@Param("certificateId") String certificateId,
        @Param("licenseKindCode") String licenseKindCode);

    int insert(ProductCertLicenseSnap row);

    int update(ProductCertLicenseSnap row);

    int softDeleteById(ProductCertLicenseSnap row);

    /**
     * 同供应商、同医院、同证件类型、同证件编码可共享预览图片的证件类型名称
     */
    List<ProductCertLicenseSnap> selectSnapsByLicenseNoSupplierHospitalAndKind(
        @Param("licenseNo") String licenseNo,
        @Param("supplierId") Long supplierId,
        @Param("hospitalId") String hospitalId,
        @Param("hospitalCode") String hospitalCode,
        @Param("licenseKindCode") String licenseKindCode);
}
