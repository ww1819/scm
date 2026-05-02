package com.scm.system.service.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSON;
import com.scm.common.core.text.Convert;
import com.scm.common.exception.ServiceException;
import com.scm.common.utils.DateUtils;
import com.scm.common.utils.ShiroUtils;
import com.scm.common.utils.StringUtils;
import com.scm.common.utils.uuid.IdUtils;
import com.scm.system.domain.ScmSupplierCertChangeLog;
import com.scm.system.domain.SupplierCertificate;
import com.scm.system.mapper.HospitalSupplierMapper;
import com.scm.system.mapper.ScmSupplierCertChangeLogMapper;
import com.scm.system.mapper.SupplierCertificateMapper;
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
        return supplierCertificateMapper.selectSupplierCertificateList(supplierCertificate);
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
        return supplierCertificateMapper.selectSupplierCertificateListBySupplierIds(supplierCertificate, supplierIds, hospitalId);
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
        return supplierCertificateMapper.selectExpiringCertificateList(supplierCertificate);
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
            SupplierCertificate after = supplierCertificateMapper.selectSupplierCertificateById(supplierCertificate.getCertificateId());
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
        }
        supplierCertificate.setUpdateTime(DateUtils.getNowDate());
        // 检查过期状态
        checkExpiredStatus(supplierCertificate);
        int rows = supplierCertificateMapper.updateSupplierCertificate(supplierCertificate);
        if (rows > 0)
        {
            SupplierCertificate after = supplierCertificateMapper.selectSupplierCertificateById(supplierCertificate.getCertificateId());
            writeCertChangeLogs("UPDATE", before, after, supplierCertificate.getUpdateBy());
        }
        return rows;
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
        List<SupplierCertificate> certificates = supplierCertificateMapper.selectSupplierCertificateList(new SupplierCertificate());
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
}

