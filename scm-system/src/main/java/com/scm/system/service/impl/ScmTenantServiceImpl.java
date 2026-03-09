package com.scm.system.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.scm.common.core.text.Convert;
import com.scm.common.utils.StringUtils;
import com.scm.common.utils.uuid.IdUtils;
import com.scm.common.utils.PinyinUtils;
import com.scm.system.domain.ScmTenant;
import com.scm.system.domain.ScmTenantModifyLog;
import com.scm.system.domain.ScmTenantStatusLog;
import com.scm.system.domain.ScmTenantStatusPeriod;
import com.scm.system.mapper.ScmTenantMapper;
import com.scm.system.mapper.ScmTenantModifyLogMapper;
import com.scm.system.mapper.ScmTenantStatusLogMapper;
import com.scm.system.mapper.ScmTenantStatusPeriodMapper;
import com.scm.system.service.IScmTenantService;
import com.scm.system.service.ITenantAdminCreateService;

@Service
public class ScmTenantServiceImpl implements IScmTenantService
{
    @Autowired
    private ScmTenantMapper scmTenantMapper;
    @Autowired
    private ScmTenantStatusPeriodMapper statusPeriodMapper;
    @Autowired
    private ScmTenantStatusLogMapper statusLogMapper;
    @Autowired
    private ScmTenantModifyLogMapper modifyLogMapper;
    @Autowired
    private ITenantAdminCreateService tenantAdminCreateService;

    @Override
    public ScmTenant selectScmTenantById(String tenantId)
    {
        return scmTenantMapper.selectScmTenantById(tenantId);
    }

    @Override
    public List<ScmTenant> selectScmTenantList(ScmTenant query)
    {
        return scmTenantMapper.selectScmTenantList(query);
    }

    @Override
    public boolean checkTenantCodeUnique(ScmTenant tenant)
    {
        ScmTenant t = scmTenantMapper.checkTenantCodeUnique(tenant.getTenantCode());
        if (t == null) return true;
        if (tenant.getTenantId() != null && tenant.getTenantId().equals(t.getTenantId())) return true;
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertScmTenant(ScmTenant tenant, String createBy)
    {
        if (StringUtils.isEmpty(tenant.getTenantId()))
            tenant.setTenantId(IdUtils.simpleUuid7());
        if (StringUtils.isEmpty(tenant.getPinyinCode()) && StringUtils.isNotEmpty(tenant.getTenantName()))
            tenant.setPinyinCode(PinyinUtils.getShortCode(tenant.getTenantName()));
        if (StringUtils.isEmpty(tenant.getStatus())) tenant.setStatus("0");
        tenant.setCreateBy(createBy);
        int r = scmTenantMapper.insertScmTenant(tenant);
        if (r > 0)
            tenantAdminCreateService.ensureHospitalAdminRoleAndUser(tenant.getTenantId(), tenant.getTenantName(), tenant.getPinyinCode(), createBy);
        return r;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateScmTenant(ScmTenant tenant, String updateBy)
    {
        ScmTenant old = scmTenantMapper.selectScmTenantById(tenant.getTenantId());
        if (old != null)
            logModify(tenant.getTenantId(), old, tenant, updateBy);
        if (StringUtils.isNotEmpty(tenant.getTenantName()) && StringUtils.isEmpty(tenant.getPinyinCode()))
            tenant.setPinyinCode(PinyinUtils.getShortCode(tenant.getTenantName()));
        tenant.setUpdateBy(updateBy);
        return scmTenantMapper.updateScmTenant(tenant);
    }

    private void logModify(String tenantId, ScmTenant old, ScmTenant neu, String operBy)
    {
        Date now = new Date();
        if (neu.getTenantName() != null && !neu.getTenantName().equals(old.getTenantName()))
            insertModifyLog(tenantId, "tenant_name", old.getTenantName(), neu.getTenantName(), operBy, now);
        if (neu.getTenantCode() != null && !neu.getTenantCode().equals(old.getTenantCode()))
            insertModifyLog(tenantId, "tenant_code", old.getTenantCode(), neu.getTenantCode(), operBy, now);
        if (neu.getStatus() != null && !neu.getStatus().equals(old.getStatus()))
            insertModifyLog(tenantId, "status", old.getStatus(), neu.getStatus(), operBy, now);
        if (neu.getPlannedStopTime() != null && (old.getPlannedStopTime() == null || !neu.getPlannedStopTime().equals(old.getPlannedStopTime())))
            insertModifyLog(tenantId, "planned_stop_time", old.getPlannedStopTime() != null ? old.getPlannedStopTime().toString() : "", neu.getPlannedStopTime().toString(), operBy, now);
        if (neu.getContactPerson() != null && !neu.getContactPerson().equals(old.getContactPerson()))
            insertModifyLog(tenantId, "contact_person", old.getContactPerson(), neu.getContactPerson(), operBy, now);
        if (neu.getContactPhone() != null && !neu.getContactPhone().equals(old.getContactPhone()))
            insertModifyLog(tenantId, "contact_phone", old.getContactPhone(), neu.getContactPhone(), operBy, now);
    }

    private void insertModifyLog(String tenantId, String field, String oldVal, String newVal, String operBy, Date time)
    {
        ScmTenantModifyLog log = new ScmTenantModifyLog();
        log.setLogId(IdUtils.simpleUuid7());
        log.setTenantId(tenantId);
        log.setFieldName(field);
        log.setOldValue(oldVal);
        log.setNewValue(newVal);
        log.setOperBy(operBy);
        log.setOperTime(time);
        modifyLogMapper.insert(log);
    }

    @Override
    public int deleteScmTenantByIds(String ids, String delBy)
    {
        String[] arr = Convert.toStrArray(ids);
        if (arr.length == 0) return 0;
        scmTenantMapper.deleteScmTenantByIds(arr);
        return arr.length;
    }

    @Override
    public List<ScmTenantStatusPeriod> selectStatusPeriodsByTenantId(String tenantId)
    {
        return statusPeriodMapper.selectByTenantId(tenantId);
    }

    @Override
    public List<ScmTenantStatusLog> selectStatusLogsByTenantId(String tenantId)
    {
        return statusLogMapper.selectByTenantId(tenantId);
    }

    @Override
    public List<ScmTenantModifyLog> selectModifyLogsByTenantId(String tenantId)
    {
        return modifyLogMapper.selectByTenantId(tenantId);
    }

    @Override
    public void resetTenantAdmin(String tenantId, String operBy)
    {
        ScmTenant t = scmTenantMapper.selectScmTenantById(tenantId);
        if (t == null) return;
        tenantAdminCreateService.ensureHospitalAdminRoleAndUser(tenantId, t.getTenantName(), t.getPinyinCode(), operBy);
    }
}
