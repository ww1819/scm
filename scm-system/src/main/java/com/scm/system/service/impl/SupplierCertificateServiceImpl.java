package com.scm.system.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSON;
import com.scm.common.core.text.Convert;
import com.scm.common.exception.ServiceException;
import com.scm.common.utils.DateUtils;
import com.scm.common.utils.ShiroUtils;
import com.scm.common.utils.StringUtils;
import com.scm.common.utils.uuid.IdUtils;
import com.scm.system.domain.CertificateType;
import com.scm.system.domain.HospitalSupplier;
import com.scm.system.domain.ScmSupplierCertChangeLog;
import com.scm.system.domain.ScmFile;
import com.scm.system.domain.SupplierCertificate;
import com.scm.system.mapper.HospitalSupplierMapper;
import com.scm.system.mapper.ScmSupplierCertChangeLogMapper;
import com.scm.system.mapper.SupplierCertificateMapper;
import com.scm.system.service.ICertificateTypeService;
import com.scm.system.service.IScmSupplierCertificateFileService;
import com.scm.system.service.IScmSupplierContextService;
import com.scm.system.service.ISupplierCertificateService;

/**
 * 供应商证件 服务层实现
 * 
 * @author scm
 */
@Service
public class SupplierCertificateServiceImpl implements ISupplierCertificateService
{
    @Autowired
    private SupplierCertificateMapper supplierCertificateMapper;

    @Autowired
    private HospitalSupplierMapper hospitalSupplierMapper;

    @Autowired
    private ScmSupplierCertChangeLogMapper scmSupplierCertChangeLogMapper;

    @Autowired
    private IScmSupplierContextService scmSupplierContextService;

    @Autowired
    private ICertificateTypeService certificateTypeService;

    @Autowired
    private IScmSupplierCertificateFileService scmSupplierCertificateFileService;

    private void enrichCertificateFiles(SupplierCertificate certificate)
    {
        if (certificate == null || certificate.getCertificateId() == null)
        {
            return;
        }
        List<ScmFile> files = scmSupplierCertificateFileService.selectFilesByCertificateId(certificate.getCertificateId());
        certificate.setCertificateFiles(files);
        if (files != null && !files.isEmpty())
        {
            certificate.setCertificateFileIds(scmSupplierCertificateFileService.buildFileIdsCsv(files));
            certificate.setCertificateFile(scmSupplierCertificateFileService.buildFileUrlsCsv(files));
        }
    }

    private void saveCertificateFilesIfNeeded(SupplierCertificate certificate, String operBy)
    {
        if (certificate == null || certificate.getCertificateId() == null)
        {
            return;
        }
        if (certificate.getCertificateFileIds() != null)
        {
            scmSupplierCertificateFileService.replaceCertificateFiles(
                    certificate.getCertificateId(), certificate.getCertificateFileIds(), operBy);
        }
    }

    /**
     * 查询供应商证件信息
     * 
     * @param certificateId 证件ID
     * @return 证件信息
     */
    @Override
    public SupplierCertificate selectSupplierCertificateById(Long certificateId)
    {
        SupplierCertificate c = supplierCertificateMapper.selectSupplierCertificateById(certificateId);
        if (c != null)
        {
            assertSupplierCertificateViewScope(c);
            enrichCertificateFiles(c);
        }
        return c;
    }

    private void assertSupplierCertificateViewScope(SupplierCertificate c)
    {
        Long sid = scmSupplierContextService.resolveSupplierIdForUser(ShiroUtils.getUserId());
        if (sid != null && c.getSupplierId() != null && !sid.equals(c.getSupplierId()))
        {
            throw new ServiceException("无权查看其他供应商的资质证照");
        }
    }

    /**
     * 查询供应商证件列表
     * 
     * @param supplierCertificate 证件信息
     * @return 证件集合
     */
    @Override
    public List<SupplierCertificate> selectSupplierCertificateList(SupplierCertificate supplierCertificate)
    {
        applySupplierListScope(supplierCertificate);
        List<SupplierCertificate> list = supplierCertificateMapper.selectSupplierCertificateList(supplierCertificate);
        enrichCertificateFilesBatch(list);
        return list;
    }

    private void applySupplierListScope(SupplierCertificate supplierCertificate)
    {
        Long sid = scmSupplierContextService.resolveSupplierIdForUser(ShiroUtils.getUserId());
        if (sid != null)
        {
            supplierCertificate.setSupplierId(sid);
        }
    }

    /**
     * 根据供应商ID列表查询供应商证件列表
     * 
     * @param supplierCertificate 证件信息
     * @param supplierIds 供应商ID列表
     * @return 证件集合
     */
    @Override
    public List<SupplierCertificate> selectSupplierCertificateListBySupplierIds(SupplierCertificate supplierCertificate, List<Long> supplierIds, Long hospitalId)
    {
        Long ctx = scmSupplierContextService.resolveSupplierIdForUser(ShiroUtils.getUserId());
        if (ctx != null)
        {
            supplierIds = new ArrayList<>(Collections.singletonList(ctx));
        }
        List<SupplierCertificate> list = supplierCertificateMapper.selectSupplierCertificateListBySupplierIds(
                supplierCertificate, supplierIds, hospitalId);
        enrichCertificateFilesBatch(list);
        return list;
    }

    private void enrichCertificateFilesBatch(List<SupplierCertificate> list)
    {
        if (list == null || list.isEmpty())
        {
            return;
        }
        for (SupplierCertificate certificate : list)
        {
            enrichCertificateFiles(certificate);
        }
    }

    /**
     * 查询过期预警的供应商证件列表
     * 
     * @param supplierCertificate 证件信息
     * @return 证件集合
     */
    @Override
    public List<SupplierCertificate> selectExpiringCertificateList(SupplierCertificate supplierCertificate)
    {
        applySupplierListScope(supplierCertificate);
        List<SupplierCertificate> list = supplierCertificateMapper.selectExpiringCertificateList(supplierCertificate);
        enrichCertificateFilesBatch(list);
        return list;
    }

    /**
     * 新增供应商证件信息
     * 
     * @param supplierCertificate 证件信息
     * @return 结果
     */
    @Override
    public int insertSupplierCertificate(SupplierCertificate supplierCertificate)
    {
        Long sid = scmSupplierContextService.resolveSupplierIdForUser(ShiroUtils.getUserId());
        if (sid != null)
        {
            supplierCertificate.setSupplierId(sid);
        }
        if (StringUtils.isEmpty(supplierCertificate.getAuditStatus()))
        {
            supplierCertificate.setAuditStatus("0"); // 默认待审核
        }
        if (StringUtils.isEmpty(supplierCertificate.getStatus()))
        {
            supplierCertificate.setStatus("0"); // 默认正常
        }
        supplierCertificate.setCreateTime(DateUtils.getNowDate());
        // 检查过期状态
        checkExpiredStatus(supplierCertificate);
        int rows = supplierCertificateMapper.insertSupplierCertificate(supplierCertificate);
        if (rows > 0 && supplierCertificate.getCertificateId() != null)
        {
            saveCertificateFilesIfNeeded(supplierCertificate, supplierCertificate.getCreateBy());
            SupplierCertificate after = supplierCertificateMapper.selectSupplierCertificateById(supplierCertificate.getCertificateId());
            enrichCertificateFiles(after);
            writeCertChangeLogs("INSERT", null, after, supplierCertificate.getCreateBy());
        }
        return rows;
    }

    /**
     * 修改供应商证件信息
     * 
     * @param supplierCertificate 证件信息
     * @return 结果
     */
    @Override
    public int updateSupplierCertificate(SupplierCertificate supplierCertificate)
    {
        SupplierCertificate before = supplierCertificateMapper.selectSupplierCertificateById(supplierCertificate.getCertificateId());
        if (before != null)
        {
            assertSupplierCertificateViewScope(before);
            assertCertificateEditable(before);
        }
        Long sid = scmSupplierContextService.resolveSupplierIdForUser(ShiroUtils.getUserId());
        if (sid != null)
        {
            supplierCertificate.setSupplierId(sid);
        }
        supplierCertificate.setUpdateTime(DateUtils.getNowDate());
        // 检查过期状态
        checkExpiredStatus(supplierCertificate);
        int rows = supplierCertificateMapper.updateSupplierCertificate(supplierCertificate);
        if (rows > 0)
        {
            saveCertificateFilesIfNeeded(supplierCertificate, supplierCertificate.getUpdateBy());
            SupplierCertificate after = supplierCertificateMapper.selectSupplierCertificateById(supplierCertificate.getCertificateId());
            enrichCertificateFiles(after);
            writeCertChangeLogs("UPDATE", before, after, supplierCertificate.getUpdateBy());
        }
        return rows;
    }

    @Override
    public int updateCertificateFile(Long certificateId, String certificateFileIds, String updateBy)
    {
        SupplierCertificate before = supplierCertificateMapper.selectSupplierCertificateById(certificateId);
        if (before == null)
        {
            throw new ServiceException("证件不存在");
        }
        assertSupplierCertificateViewScope(before);
        assertCertificateEditable(before);

        SupplierCertificate bind = new SupplierCertificate();
        bind.setCertificateId(certificateId);
        bind.setCertificateFileIds(certificateFileIds != null ? certificateFileIds : "");
        saveCertificateFilesIfNeeded(bind, updateBy);

        SupplierCertificate after = supplierCertificateMapper.selectSupplierCertificateById(certificateId);
        writeCertChangeLogs("UPDATE", before, after, updateBy);
        return 1;
    }

    @Override
    public int updateSupplierCertificateUpload(SupplierCertificate supplierCertificate)
    {
        if (supplierCertificate.getCertificateId() == null)
        {
            throw new ServiceException("证件ID不能为空");
        }
        SupplierCertificate before = supplierCertificateMapper.selectSupplierCertificateById(supplierCertificate.getCertificateId());
        if (before == null)
        {
            throw new ServiceException("证件不存在");
        }
        assertSupplierCertificateViewScope(before);
        assertCertificateEditable(before);

        SupplierCertificate row = new SupplierCertificate();
        row.setCertificateId(supplierCertificate.getCertificateId());
        row.setCertificateNo(supplierCertificate.getCertificateNo() != null ? supplierCertificate.getCertificateNo() : "");
        String longTerm = supplierCertificate.getCertificateName();
        if (StringUtils.isEmpty(longTerm))
        {
            longTerm = "否";
        }
        row.setCertificateName(longTerm);
        row.setIssueDate(supplierCertificate.getIssueDate());
        if ("是".equals(longTerm) || "1".equals(longTerm))
        {
            row.setExpireDate(null);
            row.setIsExpired("0");
            row.setIsWarning("0");
        }
        else
        {
            row.setExpireDate(supplierCertificate.getExpireDate());
            if (row.getExpireDate() != null)
            {
                checkExpiredStatus(row);
            }
            else
            {
                row.setIsExpired("0");
                row.setIsWarning("0");
            }
        }
        row.setCertificateFile(supplierCertificate.getCertificateFile() != null ? supplierCertificate.getCertificateFile() : "");
        row.setRemark(supplierCertificate.getRemark() != null ? supplierCertificate.getRemark() : "");
        row.setUpdateBy(supplierCertificate.getUpdateBy());
        int rows = supplierCertificateMapper.updateSupplierCertificateUpload(row);
        if (rows > 0)
        {
            saveCertificateFilesIfNeeded(supplierCertificate, supplierCertificate.getUpdateBy());
            SupplierCertificate after = supplierCertificateMapper.selectSupplierCertificateById(supplierCertificate.getCertificateId());
            enrichCertificateFiles(after);
            writeCertChangeLogs("UPDATE", before, after, supplierCertificate.getUpdateBy());
        }
        return rows;
    }

    private void assertCertificateEditable(SupplierCertificate certificate)
    {
        if (certificate != null && "1".equals(certificate.getAuditStatus()))
        {
            throw new ServiceException("已审核的证件不允许修改");
        }
    }

    /**
     * 批量删除供应商证件信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    @Override
    public int deleteSupplierCertificateByIds(String ids)
    {
        String[] arr = Convert.toStrArray(ids);
        for (String id : arr)
        {
            Long certId = Long.parseLong(id);
            SupplierCertificate before = supplierCertificateMapper.selectSupplierCertificateById(certId);
            if (before != null)
            {
                assertSupplierCertificateViewScope(before);
            }
        }
        int rows = 0;
        for (String id : arr)
        {
            Long certId = Long.parseLong(id);
            SupplierCertificate before = supplierCertificateMapper.selectSupplierCertificateById(certId);
            rows += supplierCertificateMapper.deleteSupplierCertificateById(certId);
            scmSupplierCertificateFileService.deleteByCertificateId(certId);
            if (before != null)
            {
                writeCertChangeLogs("DELETE", before, null, ShiroUtils.getLoginName());
            }
        }
        return rows;
    }

    /**
     * 删除供应商证件信息
     * 
     * @param certificateId 证件ID
     * @return 结果
     */
    @Override
    public int deleteSupplierCertificateById(Long certificateId)
    {
        return supplierCertificateMapper.deleteSupplierCertificateById(certificateId);
    }

    /**
     * 审核供应商证件
     * 
     * @param supplierCertificate 证件信息
     * @return 结果
     */
    @Override
    public int auditSupplierCertificate(SupplierCertificate supplierCertificate)
    {
        // 确保审核状态不为空
        if (supplierCertificate.getAuditStatus() == null || supplierCertificate.getAuditStatus().isEmpty())
        {
            throw new RuntimeException("审核状态不能为空");
        }
        SupplierCertificate before = supplierCertificateMapper.selectSupplierCertificateById(supplierCertificate.getCertificateId());
        if (before != null)
        {
            assertSupplierCertificateViewScope(before);
            if ("1".equals(before.getAuditStatus()))
            {
                throw new ServiceException("该证件已审核，不能重复审核");
            }
        }
        supplierCertificate.setAuditTime(DateUtils.getNowDate());
        supplierCertificate.setUpdateTime(DateUtils.getNowDate());
        supplierCertificate.setUpdateBy(supplierCertificate.getAuditBy()); // 确保更新人也被设置
        int rows = supplierCertificateMapper.updateSupplierCertificate(supplierCertificate);
        if (rows > 0)
        {
            SupplierCertificate after = supplierCertificateMapper.selectSupplierCertificateById(supplierCertificate.getCertificateId());
            writeCertChangeLogs("AUDIT", before, after, supplierCertificate.getAuditBy());
        }
        return rows;
    }

    /**
     * 检查并更新证件过期状态
     */
    @Override
    public void checkAndUpdateExpiredStatus()
    {
        SupplierCertificate query = new SupplierCertificate();
        Long sid = scmSupplierContextService.resolveSupplierIdForUser(ShiroUtils.getUserId());
        if (sid != null)
        {
            query.setSupplierId(sid);
        }
        List<SupplierCertificate> certificates = supplierCertificateMapper.selectSupplierCertificateList(query);
        Date now = DateUtils.getNowDate();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.DAY_OF_MONTH, 30); // 默认预警天数30天
        Date warningDate = calendar.getTime();

        for (SupplierCertificate certificate : certificates)
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
                supplierCertificateMapper.updateSupplierCertificate(certificate);
            }
        }
    }

    /**
     * 检查证件过期状态
     * 
     * @param certificate 证件信息
     */
    private void checkExpiredStatus(SupplierCertificate certificate)
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

    private void writeCertChangeLogs(String changeType, SupplierCertificate before, SupplierCertificate after, String operBy)
    {
        Long supplierId = after != null ? after.getSupplierId() : (before != null ? before.getSupplierId() : null);
        if (supplierId == null)
        {
            return;
        }
        Long certificateId = after != null ? after.getCertificateId() : (before != null ? before.getCertificateId() : null);
        if (certificateId == null)
        {
            return;
        }
        List<Long> hospitalIds = hospitalSupplierMapper.selectHospitalIdsInActiveSupplyForSupplier(supplierId);
        if (hospitalIds == null || hospitalIds.isEmpty())
        {
            return;
        }
        String bj = before != null ? JSON.toJSONString(before) : null;
        String aj = after != null ? JSON.toJSONString(after) : null;
        Long certHospitalId = after != null ? after.getHospitalId() : (before != null ? before.getHospitalId() : null);
        if (certHospitalId != null)
        {
            ScmSupplierCertChangeLog log = new ScmSupplierCertChangeLog();
            log.setLogId(IdUtils.dashedUuid7());
            log.setSupplierId(supplierId);
            log.setHospitalId(certHospitalId);
            log.setCertificateId(certificateId);
            log.setChangeType(changeType);
            log.setBeforeJson(bj);
            log.setAfterJson(aj);
            log.setCreateBy(operBy);
            scmSupplierCertChangeLogMapper.insertChangeLog(log);
            return;
        }
        for (Long hid : hospitalIds)
        {
            ScmSupplierCertChangeLog log = new ScmSupplierCertChangeLog();
            log.setLogId(IdUtils.dashedUuid7());
            log.setSupplierId(supplierId);
            log.setHospitalId(hid);
            log.setCertificateId(certificateId);
            log.setChangeType(changeType);
            log.setBeforeJson(bj);
            log.setAfterJson(aj);
            log.setCreateBy(operBy);
            scmSupplierCertChangeLogMapper.insertChangeLog(log);
        }
    }

    @Override
    public void ensureMissingCertificatesForSupplier(Long supplierId, String createBy)
    {
        if (supplierId == null)
        {
            return;
        }
        List<Long> hospitalIds = hospitalSupplierMapper.selectHospitalIdsInActiveSupplyForSupplier(supplierId);
        if (hospitalIds == null || hospitalIds.isEmpty())
        {
            return;
        }
        for (Long hospitalId : hospitalIds)
        {
            ensureMissingCertificatesForSupplierAtHospital(supplierId, hospitalId, createBy);
        }
    }

    @Override
    public void ensureMissingCertificatesForSupplierAtHospital(Long supplierId, Long hospitalId, String createBy)
    {
        List<CertificateType> types = certificateTypeService.selectSupplierExtensionTypesForSnap();
        int expectedCount = (types == null || types.isEmpty()) ? 0 : countUniqueSupplierTypeNames(types);
        ensureMissingCertificatesForSupplierAtHospital(supplierId, hospitalId, createBy, types, expectedCount);
    }

    private void ensureMissingCertificatesForSupplierAtHospital(Long supplierId, Long hospitalId, String createBy,
        List<CertificateType> types, int expectedCount)
    {
        if (supplierId == null || hospitalId == null)
        {
            return;
        }
        if (types == null || types.isEmpty() || expectedCount <= 0)
        {
            return;
        }
        int existingCount = supplierCertificateMapper.countBySupplierAndHospital(supplierId, hospitalId);
        if (existingCount >= expectedCount)
        {
            return;
        }
        if (existingCount == 0)
        {
            supplierCertificateMapper.assignNullHospitalCertificates(supplierId, hospitalId);
            existingCount = supplierCertificateMapper.countBySupplierAndHospital(supplierId, hospitalId);
        }
        if (existingCount > expectedCount)
        {
            supplierCertificateMapper.deleteDuplicateCertificatesBySupplierId(supplierId, hospitalId);
            existingCount = supplierCertificateMapper.countBySupplierAndHospital(supplierId, hospitalId);
        }
        if (existingCount >= expectedCount)
        {
            return;
        }
        Set<String> existingTypes = new HashSet<>(
            supplierCertificateMapper.selectCertificateTypeNamesBySupplierAndHospital(supplierId, hospitalId));
        String oper = StringUtils.isNotEmpty(createBy) ? createBy : "system";
        Set<String> seenTypeNames = new HashSet<>();
        for (CertificateType t : types)
        {
            if (t == null || StringUtils.isEmpty(t.getTypeCode()))
            {
                continue;
            }
            String code = t.getTypeCode().trim();
            String name = StringUtils.isNotEmpty(t.getTypeName()) ? t.getTypeName().trim() : code;
            if (seenTypeNames.contains(name))
            {
                continue;
            }
            seenTypeNames.add(name);
            if (existingTypes.contains(name) || existingTypes.contains(code))
            {
                continue;
            }
            insertPlaceholderCertificate(supplierId, hospitalId, name, oper);
        }
    }

    private int countUniqueSupplierTypeNames(List<CertificateType> types)
    {
        Set<String> names = new HashSet<>();
        for (CertificateType t : types)
        {
            if (t == null || StringUtils.isEmpty(t.getTypeCode()))
            {
                continue;
            }
            String name = StringUtils.isNotEmpty(t.getTypeName()) ? t.getTypeName().trim() : t.getTypeCode().trim();
            names.add(name);
        }
        return names.size();
    }

    /** 登记页自动补齐占位行：不写变更日志，避免切换医院时拖慢列表 */
    private void insertPlaceholderCertificate(Long supplierId, Long hospitalId, String typeName, String createBy)
    {
        SupplierCertificate row = new SupplierCertificate();
        row.setSupplierId(supplierId);
        row.setHospitalId(hospitalId);
        row.setCertificateType(typeName);
        row.setCertificateName("否");
        row.setAuditStatus("0");
        row.setStatus("0");
        row.setCreateBy(createBy);
        row.setCreateTime(DateUtils.getNowDate());
        try
        {
            supplierCertificateMapper.insertSupplierCertificate(row);
        }
        catch (Exception ignored)
        {
            // 单条失败不影响其它类型
        }
    }

    @Override
    public void ensureMissingCertificatesForListContext(Long bindSupplierId, String supplierIdsCsv, Long hospitalId,
        String createBy)
    {
        if (hospitalId == null)
        {
            return;
        }
        Set<Long> targetIds = new LinkedHashSet<>();
        if (bindSupplierId != null)
        {
            targetIds.add(bindSupplierId);
        }
        else if (StringUtils.isNotEmpty(supplierIdsCsv))
        {
            for (String part : supplierIdsCsv.split(","))
            {
                String idStr = part != null ? part.trim() : "";
                if (idStr.isEmpty() || "-1".equals(idStr))
                {
                    continue;
                }
                try
                {
                    targetIds.add(Long.parseLong(idStr));
                }
                catch (NumberFormatException ignored)
                {
                }
            }
        }
        else
        {
            HospitalSupplier q = new HospitalSupplier();
            q.setHospitalId(hospitalId);
            q.setStatus("0");
            List<HospitalSupplier> relations = hospitalSupplierMapper.selectHospitalSupplierList(q);
            if (relations != null)
            {
                for (HospitalSupplier hs : relations)
                {
                    if (hs.getSupplierId() != null)
                    {
                        targetIds.add(hs.getSupplierId());
                    }
                }
            }
        }
        List<CertificateType> types = certificateTypeService.selectSupplierExtensionTypesForSnap();
        int expectedCount = (types == null || types.isEmpty()) ? 0 : countUniqueSupplierTypeNames(types);
        for (Long supplierId : targetIds)
        {
            ensureMissingCertificatesForSupplierAtHospital(supplierId, hospitalId, createBy, types, expectedCount);
        }
    }
}
