package com.scm.system.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    /** 同供应商、同医院、同证件编码可共享预览图片的证件类型名称 */
    private static final Set<String> SHAREABLE_LICENSE_KIND_NAMES = Collections.unmodifiableSet(new LinkedHashSet<>(
        Arrays.asList(
            "医疗器械注册证",
            "生产许可证",
            "生产许可证说明书",
            "注册证",
            "生产企业营业执照",
            "生产企业许可证",
            "生产企业变更说明",
            "二级授权公司营业执照",
            "二级授权公司许可证",
            "二级授权公司授权书",
            "三级授权公司营业执照",
            "三级授权公司许可证",
            "三级授权公司授权书",
            "四级授权公司营业执照",
            "四级授权公司许可证",
            "四级授权公司授权书",
            "五级授权公司营业执照",
            "五级授权公司许可证",
            "五级授权公司授权书",
            "六级授权公司营业执照",
            "六级授权公司许可证",
            "六级授权公司授权书",
            "经营许可证",
            "第二类医疗器械经营备案凭证",
            "第一类医疗器械生产备案凭证和备案信息表",
            "备案信息表",
            "厂家授权书",
            "合同复印件",
            "检验报告结论",
            "消毒产品安全评价报告",
            "消毒产品安全评价报告备案表",
            "业务员身份证",
            "业务员身份证表")));

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

    private boolean isShareableLicenseKind(ProductCertLicenseSnap snap)
    {
        if (snap == null)
        {
            return false;
        }
        String kindName = StringUtils.trimToNull(snap.getLicenseKindName());
        if (kindName == null && StringUtils.isNotEmpty(snap.getLicenseKindCode()))
        {
            kindName = StringUtils.trimToNull(resolveKindNameFromTypeTable(snap.getLicenseKindCode()));
        }
        return kindName != null && SHAREABLE_LICENSE_KIND_NAMES.contains(kindName);
    }

    private boolean hasHospitalScope(ProductCertificate c)
    {
        return c != null
            && (StringUtils.isNotEmpty(StringUtils.trimToNull(c.getHospitalId()))
                || StringUtils.isNotEmpty(StringUtils.trimToNull(c.getHospitalCode())));
    }

    private boolean canShareLicenseSnapFiles(ProductCertLicenseSnap snap, ProductCertificate c)
    {
        return isShareableLicenseKind(snap)
            && c != null
            && c.getSupplierId() != null
            && hasHospitalScope(c)
            && StringUtils.isNotEmpty(StringUtils.trimToNull(snap.getLicenseKindCode()))
            && StringUtils.isNotEmpty(StringUtils.trimToNull(snap.getLicenseNo()));
    }

    private List<ProductCertLicenseSnap> findPeerSnaps(ProductCertificate c, String licenseNo, String licenseKindCode)
    {
        if (c == null || StringUtils.isEmpty(licenseNo) || c.getSupplierId() == null
            || StringUtils.isEmpty(licenseKindCode) || !hasHospitalScope(c))
        {
            return new ArrayList<>();
        }
        List<ProductCertLicenseSnap> list = productCertLicenseSnapMapper.selectSnapsByLicenseNoSupplierHospitalAndKind(
            licenseNo.trim(), c.getSupplierId(), c.getHospitalId(), c.getHospitalCode(), licenseKindCode.trim());
        return list != null ? list : new ArrayList<>();
    }

    private String resolveSharedSnapFiles(ProductCertificate c, String licenseNo, String licenseKindCode)
    {
        List<ProductCertLicenseSnap> peers = findPeerSnaps(c, licenseNo, licenseKindCode);
        List<String> parts = new ArrayList<>();
        for (ProductCertLicenseSnap snap : peers)
        {
            if (snap != null && StringUtils.isNotEmpty(snap.getCertificateFile()))
            {
                parts.add(snap.getCertificateFile());
            }
        }
        return mergeCertificateFileUrls(parts.toArray(new String[0]));
    }

    private void applySharedSnapFiles(List<ProductCertLicenseSnap> ordered, ProductCertificate c)
    {
        if (c == null || c.getSupplierId() == null || !hasHospitalScope(c) || ordered == null || ordered.isEmpty())
        {
            return;
        }
        for (ProductCertLicenseSnap s : ordered)
        {
            if (!canShareLicenseSnapFiles(s, c))
            {
                continue;
            }
            String shared = resolveSharedSnapFiles(c, s.getLicenseNo(), s.getLicenseKindCode());
            if (StringUtils.isNotEmpty(shared))
            {
                s.setCertificateFile(shared);
            }
        }
    }

    private void syncSnapFilesToPeers(ProductCertificate c, String licenseKindCode, String licenseNo,
        String certificateFile, String loginName)
    {
        if (c == null || StringUtils.isEmpty(licenseNo) || c.getSupplierId() == null
            || StringUtils.isEmpty(licenseKindCode) || !hasHospitalScope(c))
        {
            return;
        }
        List<ProductCertLicenseSnap> peers = findPeerSnaps(c, licenseNo, licenseKindCode);
        String normalized = certificateFile != null ? certificateFile : "";
        for (ProductCertLicenseSnap peer : peers)
        {
            if (peer == null || StringUtils.isEmpty(peer.getLicenseId()))
            {
                continue;
            }
            ProductCertLicenseSnap u = new ProductCertLicenseSnap();
            u.setLicenseId(peer.getLicenseId());
            u.setCertificateFile(normalized);
            u.setUpdateBy(loginName);
            productCertLicenseSnapMapper.update(u);
        }
    }

    private void syncSnapFilesIfShareable(ProductCertLicenseSnap snap, ProductCertificate c,
        String certificateFile, String loginName)
    {
        if (!canShareLicenseSnapFiles(snap, c))
        {
            return;
        }
        syncSnapFilesToPeers(c, snap.getLicenseKindCode(), snap.getLicenseNo(), certificateFile, loginName);
    }

    private boolean removeSharedSnapImage(ProductCertLicenseSnap snap, ProductCertificate c,
        String targetUrl, String loginName)
    {
        if (!canShareLicenseSnapFiles(snap, c))
        {
            return false;
        }
        String merged = resolveSharedSnapFiles(c, snap.getLicenseNo(), snap.getLicenseKindCode());
        if (StringUtils.isEmpty(merged))
        {
            return false;
        }
        String updated = removeUrlFromCsv(merged, target);
        syncSnapFilesToPeers(c, snap.getLicenseKindCode(), snap.getLicenseNo(), updated, loginName);
        return true;
    }

    private String mergeCertificateFileUrls(String... csvValues)
    {
        List<String> urls = new ArrayList<>();
        if (csvValues == null)
        {
            return "";
        }
        for (String csv : csvValues)
        {
            if (StringUtils.isEmpty(csv))
            {
                continue;
            }
            for (String part : csv.split(","))
            {
                String t = part != null ? part.trim() : "";
                if (!t.isEmpty() && !urls.contains(t))
                {
                    urls.add(t);
                }
            }
        }
        return urls.isEmpty() ? "" : String.join(",", urls);
    }

    private String removeUrlFromCsv(String csv, String targetUrl)
    {
        if (StringUtils.isEmpty(targetUrl))
        {
            return StringUtils.trimToEmpty(csv);
        }
        String target = targetUrl.trim();
        List<String> urls = new ArrayList<>();
        if (StringUtils.isNotEmpty(csv))
        {
            for (String part : csv.split(","))
            {
                String t = part != null ? part.trim() : "";
                if (!t.isEmpty() && !t.equals(target))
                {
                    urls.add(t);
                }
            }
        }
        return urls.isEmpty() ? "" : String.join(",", urls);
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
    public void ensureProductSnapStubsForCertificates(Collection<Long> certificateIds, String loginName)
    {
        if (certificateIds == null || certificateIds.isEmpty())
        {
            return;
        }
        Set<Long> unique = new LinkedHashSet<>();
        for (Long certificateId : certificateIds)
        {
            if (certificateId != null)
            {
                unique.add(certificateId);
            }
        }
        if (unique.isEmpty())
        {
            return;
        }
        List<CertificateType> types = certificateTypeService.selectProductExtensionTypesForSnap();
        if (types == null || types.isEmpty())
        {
            return;
        }
        String oper = loginName != null ? loginName : "";
        for (Long certificateId : unique)
        {
            try
            {
                ProductCertificate c = productCertificateMapper.selectProductCertificateById(certificateId);
                if (c == null)
                {
                    continue;
                }
                String certKey = String.valueOf(certificateId);
                for (CertificateType t : types)
                {
                    if (t == null || StringUtils.isEmpty(t.getTypeCode()))
                    {
                        continue;
                    }
                    String kindCode = t.getTypeCode().trim();
                    ProductCertLicenseSnap exist = productCertLicenseSnapMapper.selectByCertIdAndKind(certKey, kindCode);
                    if (exist == null)
                    {
                        insertStubRow(c, t, oper);
                    }
                }
            }
            catch (Exception ignored)
            {
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
        ProductCertificate c = assertCertificateScope(certificateId);
        applySharedSnapFiles(ordered, c);
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
        productCertLicenseSnapMapper.update(db);
        syncSnapFilesIfShareable(db, c, db.getCertificateFile(), loginName);
        return 1;
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
        String target = fileUrl.trim();
        if (removeSharedSnapImage(db, c, target, loginName))
        {
            return 1;
        }
        String existing = StringUtils.trimToEmpty(db.getCertificateFile());
        if (StringUtils.isEmpty(existing))
        {
            return 0;
        }
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
