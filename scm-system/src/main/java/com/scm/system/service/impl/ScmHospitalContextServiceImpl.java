package com.scm.system.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.scm.common.utils.PageUtils;
import com.scm.system.domain.HospitalUser;
import com.scm.system.mapper.HospitalUserMapper;
import com.scm.system.service.IScmHospitalContextService;

@Service
public class ScmHospitalContextServiceImpl implements IScmHospitalContextService
{
    @Autowired
    private HospitalUserMapper hospitalUserMapper;

    @Override
    public Long resolveHospitalIdForUser(Long userId)
    {
        if (userId == null)
        {
            return null;
        }
        return PageUtils.callWithoutPaging(() -> {
            HospitalUser hu = hospitalUserMapper.selectHospitalUserByUserId(userId);
            return hu != null ? hu.getHospitalId() : null;
        });
    }
}
