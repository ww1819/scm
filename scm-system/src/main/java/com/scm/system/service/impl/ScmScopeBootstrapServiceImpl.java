package com.scm.system.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.scm.common.constant.ScmAuthConstants;
import com.scm.common.constant.ScmMenuConstants;
import com.scm.common.core.domain.entity.SysMenu;
import com.scm.common.core.domain.entity.SysRole;
import com.scm.common.utils.DateUtils;
import com.scm.common.utils.StringUtils;
import com.scm.common.utils.uuid.IdUtils;
import com.scm.system.domain.ScmHospitalMenuAuth;
import com.scm.system.domain.ScmSupplierMenuAuth;
import com.scm.system.domain.SysRoleMenu;
import com.scm.system.mapper.ScmHospitalMenuAuthMapper;
import com.scm.system.mapper.ScmSupplierMenuAuthMapper;
import com.scm.system.mapper.SysMenuMapper;
import com.scm.system.mapper.SysRoleMapper;
import com.scm.system.mapper.SysRoleMenuMapper;
import com.scm.system.service.IScmScopeBootstrapService;

@Service
public class ScmScopeBootstrapServiceImpl implements IScmScopeBootstrapService
{
    @Override
    public Set<Long> listAllScopeMenuIds(String authType)
    {
        return collectScopeMenuIdsWithAncestors(authType);
    }

    private static final int BATCH = 400;

    @Autowired
    private SysMenuMapper sysMenuMapper;
    @Autowired
    private SysRoleMapper sysRoleMapper;
    @Autowired
    private SysRoleMenuMapper sysRoleMenuMapper;
    @Autowired
    private ScmHospitalMenuAuthMapper hospitalMenuAuthMapper;
    @Autowired
    private ScmSupplierMenuAuthMapper supplierMenuAuthMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long bootstrapAfterSupplierRegister(Long supplierId, String operBy)
    {
        SysRole admin = ensureSupplierAdminRole(supplierId, operBy);
        Set<Long> menuIds = collectScopeMenuIdsWithAncestors(ScmAuthConstants.AUTH_SUPPLIER);
        syncSupplierMenus(admin.getRoleId(), supplierId, menuIds, operBy);
        return admin.getRoleId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bootstrapAfterHospitalCreated(Long hospitalId, String operBy)
    {
        SysRole admin = ensureHospitalAdminRole(hospitalId, operBy);
        Set<Long> menuIds = collectScopeMenuIdsWithAncestors(ScmAuthConstants.AUTH_HOSPITAL);
        syncHospitalMenus(admin.getRoleId(), hospitalId, menuIds, operBy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetHospitalMenuAuth(Long hospitalId, String operBy)
    {
        SysRole admin = ensureHospitalAdminRole(hospitalId, operBy);
        Set<Long> menuIds = collectScopeMenuIdsWithAncestors(ScmAuthConstants.AUTH_HOSPITAL);
        syncHospitalMenus(admin.getRoleId(), hospitalId, menuIds, operBy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetSupplierMenuAuth(Long supplierId, String operBy)
    {
        SysRole admin = ensureSupplierAdminRole(supplierId, operBy);
        Set<Long> menuIds = collectScopeMenuIdsWithAncestors(ScmAuthConstants.AUTH_SUPPLIER);
        syncSupplierMenus(admin.getRoleId(), supplierId, menuIds, operBy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceHospitalMenuAuth(Long hospitalId, List<Long> menuIds, String operBy)
    {
        SysRole admin = ensureHospitalAdminRole(hospitalId, operBy);
        Set<Long> normalized = normalizeMenuIdSet(menuIds);
        syncHospitalMenus(admin.getRoleId(), hospitalId, normalized, operBy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceSupplierMenuAuth(Long supplierId, List<Long> menuIds, String operBy)
    {
        SysRole admin = ensureSupplierAdminRole(supplierId, operBy);
        Set<Long> normalized = normalizeMenuIdSet(menuIds);
        syncSupplierMenus(admin.getRoleId(), supplierId, normalized, operBy);
    }

    private Set<Long> normalizeMenuIdSet(List<Long> menuIds)
    {
        Set<Long> set = new HashSet<>();
        if (menuIds != null)
        {
            for (Long id : menuIds)
            {
                if (id != null)
                {
                    set.add(id);
                }
            }
        }
        return set;
    }

    private SysRole ensureHospitalAdminRole(Long hospitalId, String operBy)
    {
        SysRole exist = sysRoleMapper.selectByRoleKeyAndHospitalId(ScmAuthConstants.ROLE_KEY_HOSPITAL_ADMIN, hospitalId);
        if (exist != null)
        {
            return exist;
        }
        SysRole role = new SysRole();
        role.setRoleName("医院管理员");
        role.setRoleKey(ScmAuthConstants.ROLE_KEY_HOSPITAL_ADMIN);
        role.setRoleSort("10");
        role.setDataScope("1");
        role.setStatus("0");
        role.setRoleType(ScmAuthConstants.ROLE_TYPE_HOSPITAL);
        role.setHospitalId(hospitalId);
        role.setRemark("系统自动创建");
        role.setCreateBy(StringUtils.isNotEmpty(operBy) ? operBy : "system");
        sysRoleMapper.insertRole(role);
        return role;
    }

    private SysRole ensureSupplierAdminRole(Long supplierId, String operBy)
    {
        SysRole exist = sysRoleMapper.selectByRoleKeyAndSupplierId(ScmAuthConstants.ROLE_KEY_SUPPLIER_ADMIN, supplierId);
        if (exist != null)
        {
            return exist;
        }
        SysRole role = new SysRole();
        role.setRoleName("供应商管理员");
        role.setRoleKey(ScmAuthConstants.ROLE_KEY_SUPPLIER_ADMIN);
        role.setRoleSort("5");
        role.setDataScope("1");
        role.setStatus("0");
        role.setRoleType(ScmAuthConstants.ROLE_TYPE_SUPPLIER);
        role.setSupplierId(supplierId);
        role.setRemark("系统自动创建");
        role.setCreateBy(StringUtils.isNotEmpty(operBy) ? operBy : "system");
        sysRoleMapper.insertRole(role);
        return role;
    }

    private void syncHospitalMenus(Long adminRoleId, Long hospitalId, Set<Long> menuIds, String operBy)
    {
        hospitalMenuAuthMapper.deleteByHospitalId(hospitalId);
        batchInsertHospitalAuth(hospitalId, menuIds, operBy);
        sysRoleMenuMapper.deleteRoleMenuByRoleId(adminRoleId);
        batchInsertRoleMenus(adminRoleId, menuIds);
    }

    private void syncSupplierMenus(Long adminRoleId, Long supplierId, Set<Long> menuIds, String operBy)
    {
        supplierMenuAuthMapper.deleteBySupplierId(supplierId);
        batchInsertSupplierAuth(supplierId, menuIds, operBy);
        sysRoleMenuMapper.deleteRoleMenuByRoleId(adminRoleId);
        batchInsertRoleMenus(adminRoleId, menuIds);
    }

    private void batchInsertHospitalAuth(Long hospitalId, Set<Long> menuIds, String operBy)
    {
        Date now = DateUtils.getNowDate();
        List<ScmHospitalMenuAuth> buf = new ArrayList<>();
        for (Long menuId : menuIds)
        {
            ScmHospitalMenuAuth row = new ScmHospitalMenuAuth();
            row.setId(IdUtils.simpleUuid7());
            row.setHospitalId(hospitalId);
            row.setMenuId(menuId);
            row.setCreateBy(operBy);
            row.setCreateTime(now);
            buf.add(row);
            if (buf.size() >= BATCH)
            {
                hospitalMenuAuthMapper.batchInsert(buf);
                buf.clear();
            }
        }
        if (!buf.isEmpty())
        {
            hospitalMenuAuthMapper.batchInsert(buf);
        }
    }

    private void batchInsertSupplierAuth(Long supplierId, Set<Long> menuIds, String operBy)
    {
        Date now = DateUtils.getNowDate();
        List<ScmSupplierMenuAuth> buf = new ArrayList<>();
        for (Long menuId : menuIds)
        {
            ScmSupplierMenuAuth row = new ScmSupplierMenuAuth();
            row.setId(IdUtils.simpleUuid7());
            row.setSupplierId(supplierId);
            row.setMenuId(menuId);
            row.setCreateBy(operBy);
            row.setCreateTime(now);
            buf.add(row);
            if (buf.size() >= BATCH)
            {
                supplierMenuAuthMapper.batchInsert(buf);
                buf.clear();
            }
        }
        if (!buf.isEmpty())
        {
            supplierMenuAuthMapper.batchInsert(buf);
        }
    }

    private void batchInsertRoleMenus(Long roleId, Set<Long> menuIds)
    {
        List<SysRoleMenu> buf = new ArrayList<>();
        for (Long menuId : menuIds)
        {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId(roleId);
            rm.setMenuId(menuId);
            buf.add(rm);
            if (buf.size() >= BATCH)
            {
                sysRoleMenuMapper.batchRoleMenu(buf);
                buf.clear();
            }
        }
        if (!buf.isEmpty())
        {
            sysRoleMenuMapper.batchRoleMenu(buf);
        }
    }

    /**
     * 指定主体侧（医院/供应商）建档时写入白名单的菜单种子（含祖先目录），受 auth_type、default_open_scope、hospital_grant_supplier_flag 约束。
     */
    private Set<Long> collectScopeMenuIdsWithAncestors(String authType)
    {
        List<SysMenu> all = sysMenuMapper.selectMenuAll();
        Map<Long, SysMenu> byId = new HashMap<>();
        for (SysMenu m : all)
        {
            byId.put(m.getMenuId(), m);
        }
        Set<Long> seed = new HashSet<>();
        for (SysMenu m : all)
        {
            if (m == null)
            {
                continue;
            }
            String df = StringUtils.trimToEmpty(m.getDelFlag());
            if (StringUtils.isNotEmpty(df) && !"0".equals(df))
            {
                continue;
            }
            if (ScmAuthConstants.AUTH_HOSPITAL.equalsIgnoreCase(authType) && eligibleForHospitalMenuAuthSeed(m))
            {
                seed.add(m.getMenuId());
            }
            else if (ScmAuthConstants.AUTH_SUPPLIER.equalsIgnoreCase(authType) && eligibleForSupplierGlobalMenuAuthSeed(m))
            {
                seed.add(m.getMenuId());
            }
        }
        Set<Long> result = new HashSet<>();
        for (Long mid : seed)
        {
            addMenuChain(result, mid, byId);
        }
        return result;
    }

    private static String normalizedOpenScope(SysMenu m)
    {
        String s = StringUtils.trim(m.getDefaultOpenScope());
        if (StringUtils.isEmpty(s))
        {
            return ScmMenuConstants.OPEN_SCOPE_ALL;
        }
        return s;
    }

    private static boolean scopeAllowsHospital(String scope)
    {
        return ScmMenuConstants.OPEN_SCOPE_ALL.equalsIgnoreCase(scope)
            || ScmMenuConstants.OPEN_SCOPE_ALL_HOSPITAL.equalsIgnoreCase(scope);
    }

    private static boolean scopeAllowsSupplier(String scope)
    {
        return ScmMenuConstants.OPEN_SCOPE_ALL.equalsIgnoreCase(scope)
            || ScmMenuConstants.OPEN_SCOPE_ALL_SUPPLIER.equalsIgnoreCase(scope);
    }

    private boolean eligibleForHospitalMenuAuthSeed(SysMenu m)
    {
        String scope = normalizedOpenScope(m);
        if (ScmMenuConstants.OPEN_SCOPE_NONE.equalsIgnoreCase(scope))
        {
            return false;
        }
        if (!scopeAllowsHospital(scope))
        {
            return false;
        }
        String at = StringUtils.isEmpty(m.getAuthType()) ? ScmAuthConstants.AUTH_PLATFORM : m.getAuthType();
        if (ScmAuthConstants.AUTH_HOSPITAL.equals(at))
        {
            return true;
        }
        return ScmAuthConstants.AUTH_HOSPITAL_SUPPLIER.equals(at);
    }

    private boolean eligibleForSupplierGlobalMenuAuthSeed(SysMenu m)
    {
        String scope = normalizedOpenScope(m);
        if (ScmMenuConstants.OPEN_SCOPE_NONE.equalsIgnoreCase(scope))
        {
            return false;
        }
        if (!scopeAllowsSupplier(scope))
        {
            return false;
        }
        String at = StringUtils.isEmpty(m.getAuthType()) ? ScmAuthConstants.AUTH_PLATFORM : m.getAuthType();
        if (ScmAuthConstants.AUTH_SUPPLIER.equals(at))
        {
            return true;
        }
        if (ScmAuthConstants.AUTH_HOSPITAL_SUPPLIER.equals(at))
        {
            return !"1".equals(StringUtils.trim(m.getHospitalGrantSupplierFlag()));
        }
        return false;
    }

    private void addMenuChain(Set<Long> acc, Long menuId, Map<Long, SysMenu> byId)
    {
        Long cur = menuId;
        int guard = 0;
        while (cur != null && cur > 0 && guard++ < 64)
        {
            acc.add(cur);
            SysMenu m = byId.get(cur);
            if (m == null)
            {
                break;
            }
            cur = m.getParentId();
        }
    }
}
