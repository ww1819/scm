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
}
