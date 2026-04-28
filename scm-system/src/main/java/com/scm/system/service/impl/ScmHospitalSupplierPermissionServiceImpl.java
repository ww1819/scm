package com.scm.system.service.impl;

import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.scm.common.exception.ServiceException;
import com.scm.common.utils.DateUtils;
import com.scm.common.utils.StringUtils;
import com.scm.common.utils.uuid.IdUtils;
import com.scm.system.domain.ScmHospitalSupplierPermission;
import com.scm.system.mapper.ScmHospitalSupplierPermissionMapper;
import com.scm.system.service.IScmHospitalSupplierPermissionService;

@Service
public class ScmHospitalSupplierPermissionServiceImpl implements IScmHospitalSupplierPermissionService
{
    @Autowired
    private ScmHospitalSupplierPermissionMapper permissionMapper;

    @Override
    public List<Long> listForbidSubmitHospitalIds(Long supplierId)
    {
        if (supplierId == null)
        {
            return Collections.emptyList();
        }
        return permissionMapper.selectForbidSubmitHospitalIds(supplierId);
    }

    @Override
    public List<ScmHospitalSupplierPermission> selectList(ScmHospitalSupplierPermission query)
    {
        return permissionMapper.selectList(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveOrUpdate(ScmHospitalSupplierPermission row, String operBy)
    {
        if (row.getHospitalId() == null || row.getSupplierId() == null)
        {
            throw new ServiceException("医院与供应商不能为空");
        }
        ScmHospitalSupplierPermission exist = permissionMapper.selectByHospitalAndSupplier(row.getHospitalId(), row.getSupplierId());
        row.setUpdateBy(operBy);
        row.setUpdateTime(DateUtils.getNowDate());
        if (StringUtils.isEmpty(row.getDelFlag()))
        {
            row.setDelFlag("0");
        }
        if (StringUtils.isEmpty(row.getForbidSubmitFlag()))
        {
            row.setForbidSubmitFlag("0");
        }
        if (StringUtils.isEmpty(row.getForbidBindFlag()))
        {
            row.setForbidBindFlag("0");
        }
        if (exist == null)
        {
            row.setId(IdUtils.simpleUuid7());
            row.setCreateBy(operBy);
            row.setCreateTime(DateUtils.getNowDate());
            return permissionMapper.insert(row);
        }
        row.setId(exist.getId());
        return permissionMapper.update(row);
    }

    @Override
    public int removeLogical(String id, String operBy)
    {
        return permissionMapper.logicalDeleteById(id, operBy);
    }

    @Override
    public void assertSubmitAllowed(Long hospitalId, Long supplierId)
    {
        if (hospitalId == null || supplierId == null)
        {
            return;
        }
        ScmHospitalSupplierPermission p = permissionMapper.selectByHospitalAndSupplier(hospitalId, supplierId);
        if (p != null && "1".equals(p.getForbidSubmitFlag()))
        {
            throw new ServiceException("无权限向该医院提交业务数据（已被平台禁用）");
        }
    }

    @Override
    public void assertBindAllowed(Long hospitalId, Long supplierId)
    {
        if (hospitalId == null || supplierId == null)
        {
            return;
        }
        ScmHospitalSupplierPermission p = permissionMapper.selectByHospitalAndSupplier(hospitalId, supplierId);
        if (p != null && "1".equals(p.getForbidBindFlag()))
        {
            throw new ServiceException("无权限关联该医院（已被平台禁用）");
        }
    }
}
