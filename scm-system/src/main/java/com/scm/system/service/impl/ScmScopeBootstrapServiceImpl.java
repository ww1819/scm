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
import com.scm.common.utils.PinyinUtils;
import com.scm.common.utils.StringUtils;
import com.scm.common.utils.uuid.IdUtils;
import com.scm.system.domain.ScmHospitalMenuAuth;
import com.scm.system.domain.ScmSupplierMenuAuth;
import com.scm.system.domain.SysRoleMenu;
import com.scm.system.mapper.ScmHospitalMenuAuthMapper;
import com.scm.system.mapper.ScmSupplierMenuAuthMapper;
import com.scm.system.mapper.HospitalMapper;
import com.scm.system.mapper.SupplierMapper;
import com.scm.system.mapper.SysMenuMapper;
import com.scm.system.mapper.SysRoleMapper;
import com.scm.system.mapper.SysRoleMenuMapper;
import com.scm.system.service.IScmScopeBootstrapService;
import com.scm.system.service.ISysConfigService;

@Service
public class ScmScopeBootstrapServiceImpl implements IScmScopeBootstrapService
{
    private static final String CFG_HOSPITAL_STAFF_MENU_MODE = "scm.auth.bootstrap.hospital_staff.menu_mode";
    private static final String CFG_SUPPLIER_SALES_MENU_MODE = "scm.auth.bootstrap.supplier_sales.menu_mode";
    private static final String MENU_MODE_SAME_AS_ADMIN = "same_as_admin";
    private static final String MENU_MODE_NONE = "none";

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
    @Autowired
    private HospitalMapper hospitalMapper;
    @Autowired
    private SupplierMapper supplierMapper;
    @Autowired
    private ISysConfigService configService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long bootstrapAfterSupplierRegister(Long supplierId, String operBy)
    {
        SysRole admin = ensureSupplierAdminRole(supplierId, operBy);
        SysRole sales = ensureSupplierSalesRole(supplierId, operBy);
        Set<Long> menuIds = collectScopeMenuIdsWithAncestors(ScmAuthConstants.AUTH_SUPPLIER);
        syncSupplierMenus(admin.getRoleId(), supplierId, menuIds, operBy);
        if (shouldSyncSupplierSalesMenus())
        {
            syncScopedRoleMenus(sales.getRoleId(), menuIds, "", String.valueOf(supplierId));
        }
        return admin.getRoleId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bootstrapAfterHospitalCreated(Long hospitalId, String operBy)
    {
        SysRole admin = ensureHospitalAdminRole(hospitalId, operBy);
        SysRole staff = ensureHospitalStaffRole(hospitalId, operBy);
        Set<Long> menuIds = collectScopeMenuIdsWithAncestors(ScmAuthConstants.AUTH_HOSPITAL);
        syncHospitalMenus(admin.getRoleId(), hospitalId, menuIds, operBy);
        if (shouldSyncHospitalStaffMenus())
        {
            syncScopedRoleMenus(staff.getRoleId(), menuIds, String.valueOf(hospitalId), "");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetHospitalMenuAuth(Long hospitalId, String operBy)
    {
        SysRole admin = ensureHospitalAdminRole(hospitalId, operBy);
        SysRole staff = ensureHospitalStaffRole(hospitalId, operBy);
        Set<Long> menuIds = collectScopeMenuIdsWithAncestors(ScmAuthConstants.AUTH_HOSPITAL);
        syncHospitalMenus(admin.getRoleId(), hospitalId, menuIds, operBy);
        if (shouldSyncHospitalStaffMenus())
        {
            syncScopedRoleMenus(staff.getRoleId(), menuIds, String.valueOf(hospitalId), "");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetSupplierMenuAuth(Long supplierId, String operBy)
    {
        SysRole admin = ensureSupplierAdminRole(supplierId, operBy);
        SysRole sales = ensureSupplierSalesRole(supplierId, operBy);
        Set<Long> menuIds = collectScopeMenuIdsWithAncestors(ScmAuthConstants.AUTH_SUPPLIER);
        syncSupplierMenus(admin.getRoleId(), supplierId, menuIds, operBy);
        if (shouldSyncSupplierSalesMenus())
        {
            syncScopedRoleMenus(sales.getRoleId(), menuIds, "", String.valueOf(supplierId));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceHospitalMenuAuth(Long hospitalId, List<Long> menuIds, String operBy)
    {
        SysRole admin = ensureHospitalAdminRole(hospitalId, operBy);
        SysRole staff = ensureHospitalStaffRole(hospitalId, operBy);
        Set<Long> normalized = normalizeMenuIdSet(menuIds);
        syncHospitalMenus(admin.getRoleId(), hospitalId, normalized, operBy);
        if (shouldSyncHospitalStaffMenus())
        {
            syncScopedRoleMenus(staff.getRoleId(), normalized, String.valueOf(hospitalId), "");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceSupplierMenuAuth(Long supplierId, List<Long> menuIds, String operBy)
    {
        SysRole admin = ensureSupplierAdminRole(supplierId, operBy);
        SysRole sales = ensureSupplierSalesRole(supplierId, operBy);
        Set<Long> normalized = normalizeMenuIdSet(menuIds);
        syncSupplierMenus(admin.getRoleId(), supplierId, normalized, operBy);
        if (shouldSyncSupplierSalesMenus())
        {
            syncScopedRoleMenus(sales.getRoleId(), normalized, "", String.valueOf(supplierId));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Integer> repairLegacyAdminScopes(String operBy)
    {
        String realOper = StringUtils.isNotEmpty(operBy) ? operBy : "system_upgrade";
        Map<String, Integer> stat = new HashMap<>();
        stat.put("createdHospitalAdminRole", 0);
        stat.put("createdHospitalStaffRole", 0);
        stat.put("createdSupplierAdminRole", 0);
        stat.put("createdSupplierSalesRole", 0);
        stat.put("addedHospitalMenuAuth", 0);
        stat.put("addedSupplierMenuAuth", 0);
        stat.put("addedHospitalRoleMenu", 0);
        stat.put("addedSupplierRoleMenu", 0);
        stat.put("addedHospitalStaffRoleMenu", 0);
        stat.put("addedSupplierSalesRoleMenu", 0);

        List<Long> hospitalIds = hospitalMapper.selectActiveHospitalIds();
        List<Long> supplierIds = supplierMapper.selectActiveSupplierIds();
        Set<Long> hospitalSeed = collectScopeMenuIdsWithAncestors(ScmAuthConstants.AUTH_HOSPITAL);
        Set<Long> supplierSeed = collectScopeMenuIdsWithAncestors(ScmAuthConstants.AUTH_SUPPLIER);

        for (Long hospitalId : hospitalIds)
        {
            if (hospitalId == null)
            {
                continue;
            }
            if (sysRoleMapper.selectByRoleKeyAndHospitalId(ScmAuthConstants.ROLE_KEY_HOSPITAL_ADMIN, hospitalId) == null)
            {
                stat.put("createdHospitalAdminRole", stat.get("createdHospitalAdminRole") + 1);
            }
            if (sysRoleMapper.selectByRoleKeyAndHospitalId(ScmAuthConstants.ROLE_KEY_HOSPITAL_STAFF, hospitalId) == null)
            {
                stat.put("createdHospitalStaffRole", stat.get("createdHospitalStaffRole") + 1);
            }
            SysRole admin = ensureHospitalAdminRole(hospitalId, realOper);
            SysRole staff = ensureHospitalStaffRole(hospitalId, realOper);
            Set<Long> existingAuth = new HashSet<>(hospitalMenuAuthMapper.selectMenuIdsByHospitalId(hospitalId));
            Set<Long> needAuth = new HashSet<>(hospitalSeed);
            needAuth.removeAll(existingAuth);
            if (!needAuth.isEmpty())
            {
                batchInsertHospitalAuth(hospitalId, needAuth, realOper);
                stat.put("addedHospitalMenuAuth", stat.get("addedHospitalMenuAuth") + needAuth.size());
            }
            Set<Long> existingRoleMenus = new HashSet<>(
                sysRoleMenuMapper.selectMenuIdsByRoleAndScope(admin.getRoleId(), String.valueOf(hospitalId), ""));
            Set<Long> needRoleMenus = new HashSet<>(hospitalSeed);
            needRoleMenus.removeAll(existingRoleMenus);
            if (!needRoleMenus.isEmpty())
            {
                batchInsertRoleMenus(admin.getRoleId(), needRoleMenus, String.valueOf(hospitalId), "");
                stat.put("addedHospitalRoleMenu", stat.get("addedHospitalRoleMenu") + needRoleMenus.size());
            }
            if (shouldSyncHospitalStaffMenus())
            {
                Set<Long> existingStaffRoleMenus = new HashSet<>(
                    sysRoleMenuMapper.selectMenuIdsByRoleAndScope(staff.getRoleId(), String.valueOf(hospitalId), ""));
                Set<Long> needStaffRoleMenus = new HashSet<>(hospitalSeed);
                needStaffRoleMenus.removeAll(existingStaffRoleMenus);
                if (!needStaffRoleMenus.isEmpty())
                {
                    batchInsertRoleMenus(staff.getRoleId(), needStaffRoleMenus, String.valueOf(hospitalId), "");
                    stat.put("addedHospitalStaffRoleMenu", stat.get("addedHospitalStaffRoleMenu") + needStaffRoleMenus.size());
                }
            }
        }

        for (Long supplierId : supplierIds)
        {
            if (supplierId == null)
            {
                continue;
            }
            if (sysRoleMapper.selectByRoleKeyAndSupplierId(ScmAuthConstants.ROLE_KEY_SUPPLIER_ADMIN, supplierId) == null)
            {
                stat.put("createdSupplierAdminRole", stat.get("createdSupplierAdminRole") + 1);
            }
            if (sysRoleMapper.selectByRoleKeyAndSupplierId(ScmAuthConstants.ROLE_KEY_SUPPLIER_SALES, supplierId) == null)
            {
                stat.put("createdSupplierSalesRole", stat.get("createdSupplierSalesRole") + 1);
            }
            SysRole admin = ensureSupplierAdminRole(supplierId, realOper);
            SysRole sales = ensureSupplierSalesRole(supplierId, realOper);
            Set<Long> existingAuth = new HashSet<>(supplierMenuAuthMapper.selectMenuIdsBySupplierId(supplierId));
            Set<Long> needAuth = new HashSet<>(supplierSeed);
            needAuth.removeAll(existingAuth);
            if (!needAuth.isEmpty())
            {
                batchInsertSupplierAuth(supplierId, needAuth, realOper);
                stat.put("addedSupplierMenuAuth", stat.get("addedSupplierMenuAuth") + needAuth.size());
            }
            Set<Long> existingRoleMenus = new HashSet<>(
                sysRoleMenuMapper.selectMenuIdsByRoleAndScope(admin.getRoleId(), "", String.valueOf(supplierId)));
            Set<Long> needRoleMenus = new HashSet<>(supplierSeed);
            needRoleMenus.removeAll(existingRoleMenus);
            if (!needRoleMenus.isEmpty())
            {
                batchInsertRoleMenus(admin.getRoleId(), needRoleMenus, "", String.valueOf(supplierId));
                stat.put("addedSupplierRoleMenu", stat.get("addedSupplierRoleMenu") + needRoleMenus.size());
            }
            if (shouldSyncSupplierSalesMenus())
            {
                Set<Long> existingSalesRoleMenus = new HashSet<>(
                    sysRoleMenuMapper.selectMenuIdsByRoleAndScope(sales.getRoleId(), "", String.valueOf(supplierId)));
                Set<Long> needSalesRoleMenus = new HashSet<>(supplierSeed);
                needSalesRoleMenus.removeAll(existingSalesRoleMenus);
                if (!needSalesRoleMenus.isEmpty())
                {
                    batchInsertRoleMenus(sales.getRoleId(), needSalesRoleMenus, "", String.valueOf(supplierId));
                    stat.put("addedSupplierSalesRoleMenu", stat.get("addedSupplierSalesRoleMenu") + needSalesRoleMenus.size());
                }
            }
        }
        return stat;
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
        role.setPinyinCode(PinyinUtils.getShortCode(role.getRoleName()));
        sysRoleMapper.insertRole(role);
        return role;
    }

    private SysRole ensureHospitalStaffRole(Long hospitalId, String operBy)
    {
        SysRole exist = sysRoleMapper.selectByRoleKeyAndHospitalId(ScmAuthConstants.ROLE_KEY_HOSPITAL_STAFF, hospitalId);
        if (exist != null)
        {
            return exist;
        }
        SysRole role = new SysRole();
        role.setRoleName("医院职工");
        role.setRoleKey(ScmAuthConstants.ROLE_KEY_HOSPITAL_STAFF);
        role.setRoleSort("20");
        role.setDataScope("1");
        role.setStatus("0");
        role.setRoleType(ScmAuthConstants.ROLE_TYPE_HOSPITAL);
        role.setHospitalId(hospitalId);
        role.setRemark("系统自动创建");
        role.setCreateBy(StringUtils.isNotEmpty(operBy) ? operBy : "system");
        role.setPinyinCode(PinyinUtils.getShortCode(role.getRoleName()));
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
        role.setPinyinCode(PinyinUtils.getShortCode(role.getRoleName()));
        sysRoleMapper.insertRole(role);
        return role;
    }

    private SysRole ensureSupplierSalesRole(Long supplierId, String operBy)
    {
        SysRole exist = sysRoleMapper.selectByRoleKeyAndSupplierId(ScmAuthConstants.ROLE_KEY_SUPPLIER_SALES, supplierId);
        if (exist != null)
        {
            return exist;
        }
        SysRole role = new SysRole();
        role.setRoleName("供应商业务员");
        role.setRoleKey(ScmAuthConstants.ROLE_KEY_SUPPLIER_SALES);
        role.setRoleSort("15");
        role.setDataScope("1");
        role.setStatus("0");
        role.setRoleType(ScmAuthConstants.ROLE_TYPE_SUPPLIER);
        role.setSupplierId(supplierId);
        role.setRemark("系统自动创建");
        role.setCreateBy(StringUtils.isNotEmpty(operBy) ? operBy : "system");
        role.setPinyinCode(PinyinUtils.getShortCode(role.getRoleName()));
        sysRoleMapper.insertRole(role);
        return role;
    }

    private void syncHospitalMenus(Long adminRoleId, Long hospitalId, Set<Long> menuIds, String operBy)
    {
        hospitalMenuAuthMapper.deleteByHospitalId(hospitalId);
        batchInsertHospitalAuth(hospitalId, menuIds, operBy);
        sysRoleMenuMapper.deleteRoleMenuByRoleId(adminRoleId);
        batchInsertRoleMenus(adminRoleId, menuIds, String.valueOf(hospitalId), "");
    }

    private void syncSupplierMenus(Long adminRoleId, Long supplierId, Set<Long> menuIds, String operBy)
    {
        supplierMenuAuthMapper.deleteBySupplierId(supplierId);
        batchInsertSupplierAuth(supplierId, menuIds, operBy);
        syncScopedRoleMenus(adminRoleId, menuIds, "", String.valueOf(supplierId));
    }

    private void syncScopedRoleMenus(Long roleId, Set<Long> menuIds, String hospitalId, String supplierId)
    {
        sysRoleMenuMapper.deleteRoleMenuByRoleId(roleId);
        batchInsertRoleMenus(roleId, menuIds, hospitalId, supplierId);
    }

    private boolean shouldSyncHospitalStaffMenus()
    {
        return MENU_MODE_SAME_AS_ADMIN.equals(resolveMenuMode(CFG_HOSPITAL_STAFF_MENU_MODE));
    }

    private boolean shouldSyncSupplierSalesMenus()
    {
        return MENU_MODE_SAME_AS_ADMIN.equals(resolveMenuMode(CFG_SUPPLIER_SALES_MENU_MODE));
    }

    private String resolveMenuMode(String configKey)
    {
        String v = StringUtils.trimToEmpty(configService.selectConfigByKey(configKey));
        if (StringUtils.isEmpty(v))
        {
            return MENU_MODE_SAME_AS_ADMIN;
        }
        if (MENU_MODE_NONE.equalsIgnoreCase(v))
        {
            return MENU_MODE_NONE;
        }
        return MENU_MODE_SAME_AS_ADMIN;
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

    private void batchInsertRoleMenus(Long roleId, Set<Long> menuIds, String hospitalId, String supplierId)
    {
        String h = StringUtils.isNotEmpty(hospitalId) ? hospitalId : "";
        String s = StringUtils.isNotEmpty(supplierId) ? supplierId : "";
        List<SysRoleMenu> buf = new ArrayList<>();
        for (Long menuId : menuIds)
        {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setId(IdUtils.simpleUuid7());
            rm.setRoleId(roleId);
            rm.setMenuId(menuId);
            rm.setHospitalId(h);
            rm.setSupplierId(s);
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
