package com.scm.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.scm.common.utils.ShiroUtils;
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
        return scmSupplierCertChangeLogMapper.selectChangeLogList(query);
    }
}
