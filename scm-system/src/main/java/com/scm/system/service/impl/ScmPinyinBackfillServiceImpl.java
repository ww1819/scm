package com.scm.system.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.scm.common.utils.PinyinUtils;
import com.scm.common.utils.StringUtils;
import com.scm.system.domain.Hospital;
import com.scm.system.domain.Supplier;
import com.scm.common.core.domain.entity.SysRole;
import com.scm.system.mapper.HospitalMapper;
import com.scm.system.mapper.SupplierMapper;
import com.scm.system.mapper.SysRoleMapper;
import com.scm.system.service.IScmPinyinBackfillService;

@Service
public class ScmPinyinBackfillServiceImpl implements IScmPinyinBackfillService
{
    @Autowired
    private HospitalMapper hospitalMapper;

    @Autowired
    private SupplierMapper supplierMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Integer> backfillHospitalAndSupplierPinyin(boolean overwrite)
    {
        int hospitalUpdated = 0;
        int supplierUpdated = 0;
        int roleUpdated = 0;
        List<Hospital> hospitals = hospitalMapper.selectHospitalList(new Hospital());
        if (hospitals != null)
        {
            for (Hospital h : hospitals)
            {
                if (h == null || h.getHospitalId() == null || StringUtils.isEmpty(h.getHospitalName()))
                {
                    continue;
                }
                if (!overwrite && StringUtils.isNotEmpty(h.getPinyinCode()))
                {
                    continue;
                }
                String py = PinyinUtils.getShortCode(h.getHospitalName().trim());
                if (py == null)
                {
                    py = "";
                }
                hospitalMapper.updateHospitalPinyinCode(h.getHospitalId(), py);
                hospitalUpdated++;
            }
        }
        List<Supplier> suppliers = supplierMapper.selectSupplierList(new Supplier());
        if (suppliers != null)
        {
            for (Supplier s : suppliers)
            {
                if (s == null || s.getSupplierId() == null || StringUtils.isEmpty(s.getCompanyName()))
                {
                    continue;
                }
                if (!overwrite && StringUtils.isNotEmpty(s.getPinyinCode()))
                {
                    continue;
                }
                String py = PinyinUtils.getShortCode(s.getCompanyName().trim());
                if (py == null)
                {
                    py = "";
                }
                supplierMapper.updateSupplierPinyinCode(s.getSupplierId(), py);
                supplierUpdated++;
            }
        }
        List<SysRole> roles = sysRoleMapper.selectRolesForPinyinBackfill();
        if (roles != null)
        {
            for (SysRole r : roles)
            {
                if (r == null || r.getRoleId() == null || StringUtils.isEmpty(r.getRoleName()))
                {
                    continue;
                }
                if (!overwrite && StringUtils.isNotEmpty(r.getPinyinCode()))
                {
                    continue;
                }
                String py = PinyinUtils.getShortCode(r.getRoleName().trim());
                if (py == null)
                {
                    py = "";
                }
                sysRoleMapper.updateRolePinyinCode(r.getRoleId(), py);
                roleUpdated++;
            }
        }
        Map<String, Integer> m = new HashMap<>(6);
        m.put("hospitalUpdated", hospitalUpdated);
        m.put("supplierUpdated", supplierUpdated);
        m.put("roleUpdated", roleUpdated);
        return m;
    }
}
