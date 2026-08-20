package com.scm.system.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.scm.common.utils.ShiroUtils;
import com.scm.common.utils.StringUtils;
import com.scm.system.domain.ScmSupplierCertChangeLog;
import com.scm.system.mapper.ScmSupplierCertChangeLogMapper;
import com.scm.system.service.IScmHospitalContextService;
import com.scm.system.service.IScmSupplierContextService;
import com.scm.system.service.IScmSupplierCertChangeLogService;

@Service
public class ScmSupplierCertChangeLogServiceImpl implements IScmSupplierCertChangeLogService
{
    @Autowired
    private ScmSupplierCertChangeLogMapper scmSupplierCertChangeLogMapper;

    @Autowired
    private IScmHospitalContextService scmHospitalContextService;

    @Autowired
    private IScmSupplierContextService scmSupplierContextService;

    @Override
    public List<ScmSupplierCertChangeLog> selectChangeLogList(ScmSupplierCertChangeLog query)
    {
        if (query == null)
        {
            query = new ScmSupplierCertChangeLog();
        }
        Long hospitalCtx = scmHospitalContextService.resolveHospitalIdForUser(ShiroUtils.getUserId());
        if (hospitalCtx != null)
        {
            query.setHospitalId(hospitalCtx);
        }
        Long supplierCtx = scmSupplierContextService.resolveSupplierIdForUser(ShiroUtils.getUserId());
        if (supplierCtx != null)
        {
            query.setSupplierId(supplierCtx);
        }
        List<ScmSupplierCertChangeLog> list = scmSupplierCertChangeLogMapper.selectChangeLogList(query);
        if (list != null)
        {
            for (ScmSupplierCertChangeLog row : list)
            {
                fillDisplayFields(row);
            }
        }
        return list;
    }

    private void fillDisplayFields(ScmSupplierCertChangeLog row)
    {
        if (row == null)
        {
            return;
        }
        JSONObject before = parseJson(row.getBeforeJson());
        JSONObject after = parseJson(row.getAfterJson());
        JSONObject prefer = after != null ? after : before;

        if (StringUtils.isEmpty(row.getSupplierName()))
        {
            row.setSupplierName(firstNonEmpty(
                prefer != null ? prefer.getString("supplierName") : null,
                before != null ? before.getString("supplierName") : null));
        }
        if (StringUtils.isEmpty(row.getSupplierCode()))
        {
            row.setSupplierCode(firstNonEmpty(
                prefer != null ? prefer.getString("supplierCode") : null,
                before != null ? before.getString("supplierCode") : null));
        }
        if (StringUtils.isEmpty(row.getHospitalName()))
        {
            row.setHospitalName(firstNonEmpty(
                prefer != null ? prefer.getString("hospitalName") : null,
                before != null ? before.getString("hospitalName") : null));
        }
        if (StringUtils.isEmpty(row.getHospitalCode()))
        {
            row.setHospitalCode(firstNonEmpty(
                prefer != null ? prefer.getString("hospitalCode") : null,
                before != null ? before.getString("hospitalCode") : null));
        }

        row.setCertificateType(firstNonEmpty(
            after != null ? after.getString("certificateType") : null,
            before != null ? before.getString("certificateType") : null));

        row.setCertificateNo(firstNonEmpty(
            after != null ? after.getString("certificateNo") : null,
            before != null ? before.getString("certificateNo") : null));

        Date issueDate = readDate(after, "issueDate");
        if (issueDate == null)
        {
            issueDate = readDate(before, "issueDate");
        }
        row.setIssueDate(issueDate);

        Date expireDate = row.getExpireDate();
        if (expireDate == null)
        {
            expireDate = readDate(after, "expireDate");
            if (expireDate == null)
            {
                expireDate = readDate(before, "expireDate");
            }
            row.setExpireDate(expireDate);
        }

        String file = firstNonEmpty(
            after != null ? after.getString("certificateFile") : null,
            before != null ? before.getString("certificateFile") : null);
        row.setCertificateFile(file);
        row.setUploadedFlag(StringUtils.isNotEmpty(file) ? "1" : "0");

        row.setCertCreateBy(firstNonEmpty(
            after != null ? after.getString("createBy") : null,
            before != null ? before.getString("createBy") : null));
        Date certCreateTime = readDate(after, "createTime");
        if (certCreateTime == null)
        {
            certCreateTime = readDate(before, "createTime");
        }
        row.setCertCreateTime(certCreateTime);

        row.setOldCertificateNo(before != null ? before.getString("certificateNo") : null);
        row.setOldExpireDate(readDate(before, "expireDate"));
        row.setNewCertificateNo(after != null ? after.getString("certificateNo") : null);
        row.setNewExpireDate(readDate(after, "expireDate"));

        row.setAuditBy(firstNonEmpty(
            after != null ? after.getString("auditBy") : null,
            before != null ? before.getString("auditBy") : null));
        Date auditTime = readDate(after, "auditTime");
        if (auditTime == null)
        {
            auditTime = readDate(before, "auditTime");
        }
        row.setAuditTime(auditTime);
    }

    private static JSONObject parseJson(String json)
    {
        if (StringUtils.isEmpty(json))
        {
            return null;
        }
        try
        {
            return JSON.parseObject(json);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private static Date readDate(JSONObject obj, String key)
    {
        if (obj == null || key == null)
        {
            return null;
        }
        return obj.getDate(key);
    }

    private static String firstNonEmpty(String a, String b)
    {
        if (StringUtils.isNotEmpty(a))
        {
            return a;
        }
        if (StringUtils.isNotEmpty(b))
        {
            return b;
        }
        return null;
    }
}
