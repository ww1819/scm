package com.scm.system.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.scm.common.utils.PageUtils;
import com.scm.system.domain.SupplierUser;
import com.scm.system.mapper.SupplierUserMapper;
import com.scm.system.service.IScmSupplierContextService;

@Service
public class ScmSupplierContextServiceImpl implements IScmSupplierContextService
{
    @Autowired
    private SupplierUserMapper supplierUserMapper;

    @Override
    public Long resolveSupplierIdForUser(Long userId)
    {
        if (userId == null)
        {
            return null;
        }
        return PageUtils.callWithoutPaging(() -> {
            SupplierUser su = supplierUserMapper.selectSupplierUserByUserId(userId);
            return su != null ? su.getSupplierId() : null;
        });
    }
}
