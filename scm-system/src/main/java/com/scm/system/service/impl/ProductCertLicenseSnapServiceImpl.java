package com.scm.system.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.scm.common.exception.ServiceException;
import com.scm.common.profiler.OperationProfiler;
import com.scm.common.utils.StringUtils;
import com.scm.common.utils.uuid.IdUtils;
import com.scm.system.domain.CertificateType;
import com.scm.system.domain.ProductCertificate;
import com.scm.system.domain.ProductCertLicenseSnap;
import com.scm.system.mapper.ProductCertificateMapper;
import com.scm.system.mapper.ProductCertLicenseSnapMapper;
import com.scm.system.service.ICertificateTypeService;
import com.scm.system.service.IProductCertificateService;
import com.scm.system.service.IProductCertLicenseSnapService;

@Service
public class ProductCertLicenseSnapServiceImpl implements IProductCertLicenseSnapService
{
    private static final Logger log = LoggerFactory.getLogger(ProductCertLicenseSnapServiceImpl.class);

    @Autowired
    private ProductCertLicenseSnapMapper productCertLicenseSnapMapper;

    @Autowired
    private IProductCertificateService productCertificateService;

    @Autowired
    private ICertificateTypeService certificateTypeService;

    @Autowired
    private ProductCertificateMapper productCertificateMapper;

    private ProductCertificate assertCertificateScope(Long certificateId)
    {
        if (certificateId == null)
        {
            throw new ServiceException("证件ID不能为空");
        }
        ProductCertificate c = productCertificateService.selectProductCertificateById(certificateId);
        if (c == null)
        {
            throw new ServiceException("证件不存在或无权查看");
        }
        return c;
    }

    private void assertCertificateEditable(ProductCertificate c)
    {
        if (c != null && "1".equals(c.getAuditStatus()))
        {
            throw new ServiceException("已审核的产品不允许修改");
        }
    }

    private String resolveKindNameFromTypeTable(String code)
    {
        if (StringUtils.isEmpty(code))
        {
            return "";
        }
        CertificateType t = certificateTypeService.selectByTypeCode(code.trim());
        return t != null && StringUtils.isNotEmpty(t.getTypeName()) ? t.getTypeName() : code.trim();
    }

    private void fillSnapshotFromCertificate(ProductCertificate c, ProductCertLicenseSnap row)
    {
        row.setCertificateId(String.valueOf(c.getCertificateId()));
        if (c.getMaterialId() != null)
        {
            row.setMaterialId(String.valueOf(c.getMaterialId()));
        }
        else
        {
            row.setMaterialId(null);
        }
        if (c.getSupplierId() != null)
        {
            row.setSupplierId(String.valueOf(c.getSupplierId()));
        }
        else
        {
            row.setSupplierId(null);
        }
        row.setHospitalId(c.getHospitalId());
        row.setHospitalCode(c.getHospitalCode());
        row.setProductNameSnap(c.getMaterialName());
        row.setManufacturerNameSnap(c.getManufacturerName());
        row.setSupplierCompanyNameSnap(c.getSupplierName());
        row.setRegisterNoSnap(c.getRegisterNo());
    }

    private void insertStubRow(ProductCertificate c, CertificateType type, String loginName)
    {
        ProductCertLicenseSnap row = new ProductCertLicenseSnap();
        fillSnapshotFromCertificate(c, row);
        row.setLicenseKindCode(type.getTypeCode());
        row.setLicenseKindName(StringUtils.isNotEmpty(type.getTypeName()) ? type.getTypeName() : resolveKindNameFromTypeTable(type.getTypeCode()));
        row.setLicenseId(IdUtils.dashedUuid7());
        row.setCreateBy(loginName != null ? loginName : "");
        row.setDelFlag("0");
        productCertLicenseSnapMapper.insert(row);
    }

    @Override
    public void ensureProductSnapStubsForCertificate(Long certificateId, String loginName)
    {
        ProductCertificate c = assertCertificateScope(certificateId);
        List<CertificateType> types = certificateTypeService.selectProductExtensionTypesForSnap();
        if (types == null || types.isEmpty())
        {
            return;
        }
        String certKey = String.valueOf(c.getCertificateId());
        for (CertificateType t : types)
        {
            if (t == null || StringUtils.isEmpty(t.getTypeCode()))
            {
                continue;
            }
            ProductCertLicenseSnap exist = productCertLicenseSnapMapper.selectByCertIdAndKind(certKey, t.getTypeCode().trim());
            if (exist == null)
            {
                insertStubRow(c, t, loginName);
            }
        }
    }

    @Override
    public void ensureProductSnapStubsForMaterial(Long materialId, String loginName)
    {
        if (materialId == null)
        {
            return;
        }
        List<Long> ids = productCertificateMapper.selectCertificateIdsByMaterialId(materialId);
        if (ids == null || ids.isEmpty())
        {
            return;
        }
        for (Long cid : ids)
        {
            try
            {
                ensureProductSnapStubsForCertificate(cid, loginName);
            }
            catch (Exception ignored)
            {
                // 无当前证件数据权限等场景跳过
            }
        }
    }

    @Override
    public List<ProductCertLicenseSnap> listMergedForCertificate(Long certificateId, String loginName)
    {
        OperationProfiler perf = OperationProfiler.start(log, "product-cert-license-merged",
            "certificateId=" + certificateId);
        ensureProductSnapStubsForCertificate(certificateId, loginName);
        perf.mark("ensureSnapStubs");
        List<CertificateType> types = certificateTypeService.selectProductExtensionTypesForSnap();
        perf.mark("selectExtensionTypes");
        List<ProductCertLicenseSnap> dbSnaps = productCertLicenseSnapMapper.selectListByCertificateId(String.valueOf(certificateId));
        perf.mark("selectSnapsByCertificateId");
        Map<String, ProductCertLicenseSnap> byCode = new LinkedHashMap<>();
        if (dbSnaps != null)
        {
            for (ProductCertLicenseSnap s : dbSnaps)
            {
                if (s != null && StringUtils.isNotEmpty(s.getLicenseKindCode()))
                {
                    byCode.putIfAbsent(s.getLicenseKindCode(), s);
                }
            }
        }
        List<ProductCertLicenseSnap> ordered = new ArrayList<>();
        if (types != null)
        {
            for (CertificateType t : types)
            {
                if (t == null || StringUtils.isEmpty(t.getTypeCode()))
                {
                    continue;
                }
                ProductCertLicenseSnap s = byCode.remove(t.getTypeCode().trim());
                if (s != null)
                {
                    ordered.add(s);
                }
            }
        }
        for (ProductCertLicenseSnap orphan : byCode.values())
        {
            ordered.add(orphan);
        }
        perf.mark("mergeOrderInMemory");
        perf.finish(350);
        return ordered;
    }

    @Override
    public int saveSnapRow(ProductCertLicenseSnap incoming, String loginName)
    {
        if (incoming == null || StringUtils.isEmpty(incoming.getLicenseId()))
        {
            throw new ServiceException("记录不存在");
        }
        ProductCertLicenseSnap db = productCertLicenseSnapMapper.selectById(incoming.getLicenseId());
        if (db == null)
        {
            throw new ServiceException("记录不存在或已删除");
        }
        Long certId = Long.valueOf(db.getCertificateId());
        ProductCertificate c = assertCertificateScope(certId);
        assertCertificateEditable(c);
        db.setLicenseTitle(incoming.getLicenseTitle());
        db.setLicenseNo(incoming.getLicenseNo());
        db.setIssuingBodySnap(incoming.getIssuingBodySnap());
        db.setIssueDate(incoming.getIssueDate());
        db.setExpireDate(incoming.getExpireDate());
        db.setCertificateFile(incoming.getCertificateFile());
        db.setRemark(incoming.getRemark());
        fillSnapshotFromCertificate(c, db);
        db.setLicenseKindCode(db.getLicenseKindCode());
        db.setLicenseKindName(resolveKindNameFromTypeTable(db.getLicenseKindCode()));
        db.setUpdateBy(loginName);
        return productCertLicenseSnapMapper.update(db);
    }

    @Override
    public int appendSnapCertificateImage(String licenseId, String fileUrl, String loginName)
    {
        if (StringUtils.isEmpty(licenseId) || StringUtils.isEmpty(fileUrl))
        {
            throw new ServiceException("参数无效");
        }
        ProductCertLicenseSnap db = productCertLicenseSnapMapper.selectById(licenseId);
        if (db == null)
        {
            throw new ServiceException("记录不存在或已删除");
        }
        ProductCertificate c = assertCertificateScope(Long.valueOf(db.getCertificateId()));
        assertCertificateEditable(c);
        String existing = StringUtils.trimToEmpty(db.getCertificateFile());
        List<String> urls = new ArrayList<>();
        if (StringUtils.isNotEmpty(existing))
        {
            for (String part : existing.split(","))
            {
                String t = part != null ? part.trim() : "";
                if (!t.isEmpty() && !urls.contains(t))
                {
                    urls.add(t);
                }
            }
        }
        String trimmedUrl = fileUrl.trim();
        if (!urls.contains(trimmedUrl))
        {
            urls.add(trimmedUrl);
        }
        db.setCertificateFile(String.join(",", urls));
        db.setUpdateBy(loginName);
        return productCertLicenseSnapMapper.update(db);
    }

    @Override
    public int removeSnapCertificateImage(String licenseId, String fileUrl, String loginName)
    {
        if (StringUtils.isEmpty(licenseId) || StringUtils.isEmpty(fileUrl))
        {
            throw new ServiceException("参数无效");
        }
        ProductCertLicenseSnap db = productCertLicenseSnapMapper.selectById(licenseId);
        if (db == null)
        {
            throw new ServiceException("记录不存在或已删除");
        }
        ProductCertificate c = assertCertificateScope(Long.valueOf(db.getCertificateId()));
        assertCertificateEditable(c);
        String existing = StringUtils.trimToEmpty(db.getCertificateFile());
        if (StringUtils.isEmpty(existing))
        {
            return 0;
        }
        String target = fileUrl.trim();
        List<String> urls = new ArrayList<>();
        for (String part : existing.split(","))
        {
            String t = part != null ? part.trim() : "";
            if (!t.isEmpty() && !t.equals(target))
            {
                urls.add(t);
            }
        }
        db.setCertificateFile(urls.isEmpty() ? "" : String.join(",", urls));
        db.setUpdateBy(loginName);
        return productCertLicenseSnapMapper.update(db);
    }

    @Override
    public List<ProductCertLicenseSnap> selectListByCertificateId(Long certificateId)
    {
        assertCertificateScope(certificateId);
        return productCertLicenseSnapMapper.selectListByCertificateId(String.valueOf(certificateId));
    }

    @Override
    public ProductCertLicenseSnap selectByLicenseId(String licenseId)
    {
        if (StringUtils.isEmpty(licenseId))
        {
            return null;
        }
        ProductCertLicenseSnap row = productCertLicenseSnapMapper.selectById(licenseId);
        if (row == null)
        {
            return null;
        }
        Long certId = Long.valueOf(row.getCertificateId());
        assertCertificateScope(certId);
        return row;
    }

    @Override
    public int insertSnap(ProductCertLicenseSnap row, String loginName)
    {
        if (row == null || StringUtils.isEmpty(row.getCertificateId()))
        {
            throw new ServiceException("证件ID不能为空");
        }
        Long certId = Long.valueOf(row.getCertificateId().trim());
        ProductCertificate c = assertCertificateScope(certId);
        assertCertificateEditable(c);
        if (StringUtils.isEmpty(StringUtils.trimToNull(row.getLicenseKindCode())))
        {
            throw new ServiceException("证照类型编码不能为空");
        }
        String kind = row.getLicenseKindCode().trim();
        ProductCertLicenseSnap exist = productCertLicenseSnapMapper.selectByCertIdAndKind(String.valueOf(certId), kind);
        if (exist != null)
        {
            throw new ServiceException("该证件类型已存在，请直接修改下方列表对应行");
        }
        fillSnapshotFromCertificate(c, row);
        row.setLicenseKindCode(kind);
        row.setLicenseKindName(resolveKindNameFromTypeTable(kind));
        row.setLicenseId(IdUtils.dashedUuid7());
        row.setCreateBy(loginName);
        row.setDelFlag("0");
        return productCertLicenseSnapMapper.insert(row);
    }

    @Override
    public int updateSnap(ProductCertLicenseSnap row, String loginName)
    {
        if (row == null || StringUtils.isEmpty(row.getLicenseId()))
        {
            throw new ServiceException("记录不存在");
        }
        ProductCertLicenseSnap db = productCertLicenseSnapMapper.selectById(row.getLicenseId());
        if (db == null)
        {
            throw new ServiceException("记录不存在或已删除");
        }
        Long certId = Long.valueOf(db.getCertificateId());
        ProductCertificate c = assertCertificateScope(certId);
        assertCertificateEditable(c);
        fillSnapshotFromCertificate(c, row);
        row.setCertificateId(db.getCertificateId());
        if (StringUtils.isNotEmpty(StringUtils.trimToNull(row.getLicenseKindCode())))
        {
            row.setLicenseKindName(resolveKindNameFromTypeTable(row.getLicenseKindCode()));
        }
        row.setUpdateBy(loginName);
        return productCertLicenseSnapMapper.update(row);
    }

    @Override
    public int deleteByIds(String licenseIds, String loginName)
    {
        if (StringUtils.isEmpty(licenseIds))
        {
            return 0;
        }
        int n = 0;
        for (String id : licenseIds.split(","))
        {
            String lid = id.trim();
            if (StringUtils.isEmpty(lid))
            {
                continue;
            }
            ProductCertLicenseSnap db = productCertLicenseSnapMapper.selectById(lid);
            if (db == null)
            {
                continue;
            }
            ProductCertificate c = assertCertificateScope(Long.valueOf(db.getCertificateId()));
            assertCertificateEditable(c);
            ProductCertLicenseSnap u = new ProductCertLicenseSnap();
            u.setLicenseId(lid);
            u.setUpdateBy(loginName);
            n += productCertLicenseSnapMapper.softDeleteById(u);
        }
        return n;
    }
}
