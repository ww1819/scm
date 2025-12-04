package com.scm.system.service.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.scm.common.core.text.Convert;
import com.scm.common.utils.DateUtils;
import com.scm.common.utils.StringUtils;
import com.scm.system.domain.SupplierCertificate;
import com.scm.system.mapper.SupplierCertificateMapper;
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

    /**
     * 查询供应商证件信息
     * 
     * @param certificateId 证件ID
     * @return 证件信息
     */
    @Override
    public SupplierCertificate selectSupplierCertificateById(Long certificateId)
    {
        return supplierCertificateMapper.selectSupplierCertificateById(certificateId);
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
        return supplierCertificateMapper.selectSupplierCertificateList(supplierCertificate);
    }

    /**
     * 根据供应商ID列表查询供应商证件列表
     * 
     * @param supplierCertificate 证件信息
     * @param supplierIds 供应商ID列表
     * @return 证件集合
     */
    @Override
    public List<SupplierCertificate> selectSupplierCertificateListBySupplierIds(SupplierCertificate supplierCertificate, List<Long> supplierIds)
    {
        return supplierCertificateMapper.selectSupplierCertificateListBySupplierIds(supplierCertificate, supplierIds);
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
        return supplierCertificateMapper.insertSupplierCertificate(supplierCertificate);
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
        supplierCertificate.setUpdateTime(DateUtils.getNowDate());
        // 检查过期状态
        checkExpiredStatus(supplierCertificate);
        return supplierCertificateMapper.updateSupplierCertificate(supplierCertificate);
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
        return supplierCertificateMapper.deleteSupplierCertificateByIds(Convert.toStrArray(ids));
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
        supplierCertificate.setAuditTime(DateUtils.getNowDate());
        supplierCertificate.setUpdateTime(DateUtils.getNowDate());
        return supplierCertificateMapper.updateSupplierCertificate(supplierCertificate);
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
}

