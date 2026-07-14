package com.scm.system.service.impl;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.scm.common.constant.ScmAuthConstants;
import com.scm.common.core.domain.entity.SysMenu;
import com.scm.common.core.domain.entity.SysRole;
import com.scm.common.exception.ServiceException;
import com.scm.common.utils.DateUtils;
import com.scm.common.utils.PinyinUtils;
import com.scm.common.utils.StringUtils;
import com.scm.common.utils.uuid.IdUtils;
import com.scm.system.domain.ScmHospitalMenuAuth;
import com.scm.system.domain.ScmSupplierMenuAuth;
import com.scm.system.domain.SupplierUser;
import com.scm.system.domain.SysRoleMenu;
import com.scm.system.domain.SysUserRole;
import com.scm.system.mapper.ScmHospitalMenuAuthMapper;
import com.scm.system.mapper.ScmSupplierMenuAuthMapper;
import com.scm.system.mapper.HospitalMapper;
import com.scm.system.mapper.HospitalSupplierMapper;
import com.scm.system.mapper.SupplierMapper;
import com.scm.system.mapper.SupplierUserMapper;
import com.scm.system.mapper.SysMenuMapper;
import com.scm.system.mapper.SysRoleMapper;
import com.scm.system.mapper.SysRoleMenuMapper;
import com.scm.system.mapper.SysUserRoleMapper;
import com.scm.system.service.IScmScopeBootstrapService;

/**
 * SCM 全局模板角色与白名单初始化（按 sys_menu 默认开放开关；机构维度仅维护白名单与增量 role_menu）
 */
@Service
public class ScmScopeBootstrapServiceImpl implements IScmScopeBootstrapService
{
    private static final int BATCH = 400;

    private static final Long REGULAR_ORDER_MENU_ROOT = 2401L;
    private static final Long TP_ORDER_MENU_ROOT = 2403L;

    private static final Set<Long> SUPPLIER_CERT_AUDIT_MENU_IDS = new HashSet<>(Arrays.asList(
        23005L, 23015L, 2001605L));

    private static final Set<Long> REGULAR_ORDER_MENU_SEEDS = new HashSet<>(Arrays.asList(
        2401L, 24001L, 24006L));

    private static final Set<Long> TP_ORDER_MENU_SEEDS = new HashSet<>(Arrays.asList(
        2403L, 24031L, 24032L, 24033L));

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
    private HospitalSupplierMapper hospitalSupplierMapper;
    @Autowired
    private SupplierMapper supplierMapper;
    @Autowired
    private SupplierUserMapper supplierUserMapper;
    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Override
    public Set<Long> listAllScopeMenuIds(String authType)
    {
        return collectScopeMenuIdsWithAncestors(authType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long bootstrapAfterSupplierRegister(Long supplierId, String operBy)
    {
        // 仅确保全局模板角色存在；注册只绑角色，不再按机构 insert sys_role
        ensureGlobalSupplierRolesExist(operBy);
        rebuildSupplierScopedMenus(supplierId, collectRawSupplierDefaultGrantSeeds(), operBy);
        String adminRoleKey = resolveSupplierAdminRoleKey(supplierId);
        SysRole admin = sysRoleMapper.selectGlobalScmRoleByKey(adminRoleKey);
        if (admin == null)
        {
            throw new ServiceException("全局供应商模板角色未初始化，请先执行 migrate_global_template_roles_v2.sql 或联系管理员");
        }
        return admin.getRoleId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rebindSupplierBuiltinUserRoles(Long supplierId, String operBy)
    {
        if (supplierId == null)
        {
            return;
        }
        ensureGlobalSupplierRolesExist(operBy);
        boolean tp = hospitalSupplierMapper.isThirdPartyOrderSupplier(supplierId,
            ScmAuthConstants.HOSPITAL_ID_XINHUA_THIRD_PARTY);
        String adminKey = tp ? ScmAuthConstants.ROLE_KEY_TP_SUPPLIER_ADMIN : ScmAuthConstants.ROLE_KEY_SUPPLIER_ADMIN;
        String salesKey = tp ? ScmAuthConstants.ROLE_KEY_TP_SUPPLIER_SALES : ScmAuthConstants.ROLE_KEY_SUPPLIER_SALES;
        SysRole adminRole = sysRoleMapper.selectGlobalScmRoleByKey(adminKey);
        SysRole salesRole = sysRoleMapper.selectGlobalScmRoleByKey(salesKey);
        if (adminRole == null || salesRole == null)
        {
            return;
        }
        List<Long> builtinRoleIds = collectBuiltinSupplierGlobalRoleIds();
        List<SupplierUser> users = supplierUserMapper.selectSupplierUserListBySupplierId(supplierId);
        if (users == null || users.isEmpty())
        {
            return;
        }
        for (SupplierUser su : users)
        {
            if (su == null || su.getUserId() == null)
            {
                continue;
            }
            Long userId = su.getUserId();
            for (Long rid : builtinRoleIds)
            {
                if (rid == null)
                {
                    continue;
                }
                SysUserRole del = new SysUserRole();
                del.setUserId(userId);
                del.setRoleId(rid);
                userRoleMapper.deleteUserRoleInfo(del);
            }
            Long targetRoleId = "1".equals(StringUtils.trimToEmpty(su.getIsMain()))
                ? adminRole.getRoleId() : salesRole.getRoleId();
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(targetRoleId);
            List<SysUserRole> batch = new ArrayList<>();
            batch.add(ur);
            userRoleMapper.batchUserRole(batch);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bootstrapAfterHospitalCreated(Long hospitalId, String operBy)
    {
        ensureGlobalHospitalRolesExist(operBy);
        rebuildHospitalScopedMenus(hospitalId, collectRawHospitalDefaultGrantSeeds(), operBy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetHospitalMenuAuth(Long hospitalId, String operBy)
    {
        syncGlobalHospitalTemplateRoles(operBy);
        rebuildHospitalScopedMenus(hospitalId, collectRawHospitalDefaultGrantSeeds(), operBy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetSupplierMenuAuth(Long supplierId, String operBy)
    {
        syncGlobalSupplierTemplateRoles(operBy);
        rebuildSupplierScopedMenus(supplierId, collectRawSupplierDefaultGrantSeeds(), operBy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetHospitalBuiltinRoleMenus(Long hospitalId, String operBy)
    {
        syncGlobalHospitalTemplateRoles(operBy);
        rebuildHospitalScopedMenus(hospitalId, collectRawHospitalDefaultGrantSeeds(), operBy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetSupplierBuiltinRoleMenus(Long supplierId, String operBy)
    {
        syncGlobalSupplierTemplateRoles(operBy);
        rebuildSupplierScopedMenus(supplierId, collectRawSupplierDefaultGrantSeeds(), operBy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceHospitalMenuAuth(Long hospitalId, List<Long> menuIds, String operBy)
    {
        syncGlobalHospitalTemplateRoles(operBy);
        rebuildHospitalScopedMenus(hospitalId, normalizeMenuIdSet(menuIds), operBy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceSupplierMenuAuth(Long supplierId, List<Long> menuIds, String operBy)
    {
        syncGlobalSupplierTemplateRoles(operBy);
        rebuildSupplierScopedMenus(supplierId, normalizeMenuIdSet(menuIds), operBy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyDefaultHospitalGrantedSupplierMenus(Long hospitalId, Long supplierId, String operBy)
    {
        if (hospitalId == null || supplierId == null)
        {
            return;
        }
        String oper = StringUtils.isNotEmpty(operBy) ? operBy : "system";
        ensureGlobalSupplierRolesExist(oper);

        Set<Long> scope = collectScopeMenuIdsWithAncestors(ScmAuthConstants.AUTH_HOSPITAL_SUPPLIER);
        Set<Long> hospitalOwned = new HashSet<>(hospitalMenuAuthMapper.selectMenuIdsByHospitalId(hospitalId));
        Set<Long> allowed = new HashSet<>(scope);
        allowed.retainAll(hospitalOwned);
        if (allowed.isEmpty())
        {
            return;
        }

        List<SysMenu> all = sysMenuMapper.selectMenuAll();
        Map<Long, SysMenu> byId = indexMenusById(all);
        Set<Long> grantSeeds = new HashSet<>();
        for (Long mid : allowed)
        {
            SysMenu m = byId.get(mid);
            if (m == null)
            {
                continue;
            }
            if (!ScmAuthConstants.AUTH_HOSPITAL_SUPPLIER.equalsIgnoreCase(normalizeAuthType(m)))
            {
                continue;
            }
            if (!"1".equals(StringUtils.trimToEmpty(m.getHospitalGrantSupplierFlag())))
            {
                continue;
            }
            grantSeeds.add(mid);
        }
        if (grantSeeds.isEmpty())
        {
            return;
        }
        Set<Long> toAttach = expandSeedsWithAncestors(grantSeeds, byId);
        toAttach.retainAll(allowed);

        Set<Long> existingPair = new HashSet<>(supplierMenuAuthMapper.selectMenuIdsBySupplierAndHospital(supplierId, hospitalId));
        Set<Long> needAuth = new HashSet<>(toAttach);
        needAuth.removeAll(existingPair);
        if (!needAuth.isEmpty())
        {
            batchInsertSupplierHospitalMenuAuth(hospitalId, supplierId, needAuth, oper);
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

        if (sysRoleMapper.selectGlobalScmRoleByKey(ScmAuthConstants.ROLE_KEY_HOSPITAL_ADMIN) == null)
        {
            stat.put("createdHospitalAdminRole", 1);
        }
        if (sysRoleMapper.selectGlobalScmRoleByKey(ScmAuthConstants.ROLE_KEY_HOSPITAL_STAFF) == null)
        {
            stat.put("createdHospitalStaffRole", 1);
        }
        if (sysRoleMapper.selectGlobalScmRoleByKey(ScmAuthConstants.ROLE_KEY_SUPPLIER_ADMIN) == null)
        {
            stat.put("createdSupplierAdminRole", 1);
        }
        if (sysRoleMapper.selectGlobalScmRoleByKey(ScmAuthConstants.ROLE_KEY_SUPPLIER_SALES) == null)
        {
            stat.put("createdSupplierSalesRole", 1);
        }
        if (sysRoleMapper.selectGlobalScmRoleByKey(ScmAuthConstants.ROLE_KEY_TP_SUPPLIER_ADMIN) == null)
        {
            stat.put("createdTpSupplierAdminRole", 1);
        }
        if (sysRoleMapper.selectGlobalScmRoleByKey(ScmAuthConstants.ROLE_KEY_TP_SUPPLIER_SALES) == null)
        {
            stat.put("createdTpSupplierSalesRole", 1);
        }
        syncGlobalHospitalTemplateRoles(realOper);
        syncGlobalSupplierTemplateRoles(realOper);

        Set<Long> hospitalSeedExpanded = collectScopeMenuIdsWithAncestors(ScmAuthConstants.AUTH_HOSPITAL);
        Set<Long> supplierSeedExpanded = collectScopeMenuIdsWithAncestors(ScmAuthConstants.AUTH_SUPPLIER);
        List<Long> hospitalIds = hospitalMapper.selectActiveHospitalIds();
        for (Long hospitalId : hospitalIds)
        {
            if (hospitalId == null)
            {
                continue;
            }
            Set<Long> existingAuth = new HashSet<>(hospitalMenuAuthMapper.selectMenuIdsByHospitalId(hospitalId));
            Set<Long> needAuth = new HashSet<>(hospitalSeedExpanded);
            needAuth.removeAll(existingAuth);
            if (!needAuth.isEmpty())
            {
                batchInsertHospitalAuth(hospitalId, needAuth, realOper);
                stat.put("addedHospitalMenuAuth", stat.get("addedHospitalMenuAuth") + needAuth.size());
            }
        }
        List<Long> supplierIds = supplierMapper.selectActiveSupplierIds();
        for (Long supplierId : supplierIds)
        {
            if (supplierId == null)
            {
                continue;
            }
            Set<Long> existingAuth = new HashSet<>(supplierMenuAuthMapper.selectMenuIdsBySupplierId(supplierId));
            Set<Long> needAuth = new HashSet<>(supplierSeedExpanded);
            needAuth.removeAll(existingAuth);
            if (!needAuth.isEmpty())
            {
                batchInsertSupplierAuth(supplierId, needAuth, realOper);
                stat.put("addedSupplierMenuAuth", stat.get("addedSupplierMenuAuth") + needAuth.size());
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

    private void rebuildHospitalScopedMenus(Long hospitalId, Set<Long> rawSeedMenuIds, String operBy)
    {
        List<SysMenu> all = sysMenuMapper.selectMenuAll();
        Map<Long, SysMenu> byId = indexMenusById(all);
        Set<Long> adminExpanded = expandSeedsWithAncestors(rawSeedMenuIds, byId);
        String hid = String.valueOf(hospitalId);
        List<Long> oldHospitalAuthMenus = hospitalMenuAuthMapper.selectMenuIdsByHospitalId(hospitalId);
        hospitalMenuAuthMapper.deleteByHospitalId(hospitalId);
        sysRoleMenuMapper.deleteRoleMenuByHospitalScope(hid);
        if (oldHospitalAuthMenus != null && !oldHospitalAuthMenus.isEmpty())
        {
            Set<Long> revokedMenus = new HashSet<>(oldHospitalAuthMenus);
            revokedMenus.removeAll(adminExpanded);
            if (!revokedMenus.isEmpty())
            {
                supplierMenuAuthMapper.deleteByHospitalAndMenuIds(hospitalId, new ArrayList<>(revokedMenus));
            }
        }
        batchInsertHospitalAuth(hospitalId, adminExpanded, operBy);
    }

    private void rebuildSupplierScopedMenus(Long supplierId, Set<Long> rawSeedMenuIds, String operBy)
    {
        List<SysMenu> all = sysMenuMapper.selectMenuAll();
        Map<Long, SysMenu> byId = indexMenusById(all);
        Set<Long> adminExpanded = expandSeedsWithAncestors(rawSeedMenuIds, byId);
        String sid = String.valueOf(supplierId);
        supplierMenuAuthMapper.deleteBySupplierId(supplierId);
        sysRoleMenuMapper.deleteRoleMenuBySupplierScope(sid);
        batchInsertSupplierAuth(supplierId, adminExpanded, operBy);
    }

    private static Map<Long, SysMenu> indexMenusById(List<SysMenu> all)
    {
        Map<Long, SysMenu> byId = new HashMap<>();
        if (all != null)
        {
            for (SysMenu m : all)
            {
                if (m != null && m.getMenuId() != null)
                {
                    byId.put(m.getMenuId(), m);
                }
            }
        }
        return byId;
    }

    private static boolean menuRowActive(SysMenu m)
    {
        if (m == null)
        {
            return false;
        }
        String df = StringUtils.trimToEmpty(m.getDelFlag());
        return StringUtils.isEmpty(df) || "0".equals(df);
    }

    private static boolean menuFlagYes(String raw)
    {
        String t = StringUtils.trimToEmpty(raw);
        return "1".equals(t) || "Y".equalsIgnoreCase(t);
    }

    private static String normalizeAuthType(SysMenu m)
    {
        String at = StringUtils.trimToEmpty(m.getAuthType());
        if (StringUtils.isEmpty(at))
        {
            return ScmAuthConstants.AUTH_PLATFORM;
        }
        return at;
    }

    private Set<Long> collectRawHospitalDefaultGrantSeeds()
    {
        Set<Long> seed = new HashSet<>();
        List<SysMenu> all = sysMenuMapper.selectMenuAll();
        for (SysMenu m : all)
        {
            if (!menuRowActive(m) || !menuFlagYes(m.getDefaultOpenHospital()))
            {
                continue;
            }
            String at = normalizeAuthType(m);
            if (ScmAuthConstants.AUTH_PLATFORM.equalsIgnoreCase(at))
            {
                continue;
            }
            if (ScmAuthConstants.AUTH_HOSPITAL.equalsIgnoreCase(at) || ScmAuthConstants.AUTH_HOSPITAL_SUPPLIER.equalsIgnoreCase(at))
            {
                seed.add(m.getMenuId());
            }
        }
        return seed;
    }

    private Set<Long> collectSupplierBaseSeeds(Map<Long, SysMenu> byId)
    {
        Set<Long> seed = new HashSet<>();
        for (Long mid : collectRawSupplierDefaultGrantSeeds())
        {
            if (isSupplierCertAuditMenu(mid, byId))
            {
                continue;
            }
            seed.add(mid);
        }
        return seed;
    }

    private static boolean isSupplierCertAuditMenu(Long menuId, Map<Long, SysMenu> byId)
    {
        if (menuId == null)
        {
            return false;
        }
        if (SUPPLIER_CERT_AUDIT_MENU_IDS.contains(menuId))
        {
            return true;
        }
        SysMenu m = byId.get(menuId);
        if (m == null)
        {
            return false;
        }
        String perms = StringUtils.trimToEmpty(m.getPerms());
        return perms.endsWith(":audit");
    }

    private Set<Long> collectRegularSupplierTemplateSeeds(Map<Long, SysMenu> byId)
    {
        Set<Long> base = collectSupplierBaseSeeds(byId);
        Set<Long> filtered = new HashSet<>();
        for (Long mid : base)
        {
            if (!isDescendantOf(mid, TP_ORDER_MENU_ROOT, byId))
            {
                filtered.add(mid);
            }
        }
        filtered.addAll(REGULAR_ORDER_MENU_SEEDS);
        return filtered;
    }

    private Set<Long> collectTpSupplierTemplateSeeds(Map<Long, SysMenu> byId)
    {
        Set<Long> base = collectSupplierBaseSeeds(byId);
        Set<Long> filtered = new HashSet<>();
        for (Long mid : base)
        {
            if (!isDescendantOf(mid, REGULAR_ORDER_MENU_ROOT, byId)
                && !REGULAR_ORDER_MENU_ROOT.equals(mid))
            {
                filtered.add(mid);
            }
        }
        filtered.addAll(TP_ORDER_MENU_SEEDS);
        return filtered;
    }

    private static boolean isDescendantOf(Long menuId, Long ancestorId, Map<Long, SysMenu> byId)
    {
        if (menuId == null || ancestorId == null)
        {
            return false;
        }
        Long cur = menuId;
        int guard = 0;
        while (cur != null && cur > 0 && guard++ < 64)
        {
            if (ancestorId.equals(cur))
            {
                return true;
            }
            SysMenu m = byId.get(cur);
            if (m == null)
            {
                break;
            }
            cur = m.getParentId();
        }
        return false;
    }

    private String resolveSupplierAdminRoleKey(Long supplierId)
    {
        if (supplierId != null && hospitalSupplierMapper.isThirdPartyOrderSupplier(supplierId,
            ScmAuthConstants.HOSPITAL_ID_XINHUA_THIRD_PARTY))
        {
            return ScmAuthConstants.ROLE_KEY_TP_SUPPLIER_ADMIN;
        }
        return ScmAuthConstants.ROLE_KEY_SUPPLIER_ADMIN;
    }

    private Set<Long> collectRawSupplierDefaultGrantSeeds()
    {
        Set<Long> seed = new HashSet<>();
        List<SysMenu> all = sysMenuMapper.selectMenuAll();
        for (SysMenu m : all)
        {
            if (!menuRowActive(m) || !menuFlagYes(m.getDefaultOpenSupplier()))
            {
                continue;
            }
            String at = normalizeAuthType(m);
            if (ScmAuthConstants.AUTH_PLATFORM.equalsIgnoreCase(at))
            {
                continue;
            }
            if (ScmAuthConstants.AUTH_SUPPLIER.equalsIgnoreCase(at)
                || ScmAuthConstants.AUTH_HOSPITAL_SUPPLIER.equalsIgnoreCase(at))
            {
                seed.add(m.getMenuId());
            }
        }
        return seed;
    }

    private static Set<Long> filterHospitalSeedsExcludeAdminOnly(Set<Long> rawSeeds, Map<Long, SysMenu> byId)
    {
        Set<Long> out = new HashSet<>();
        if (rawSeeds == null)
        {
            return out;
        }
        for (Long mid : rawSeeds)
        {
            SysMenu m = byId.get(mid);
            if (m == null || menuFlagYes(m.getHospitalAdminOnly()))
            {
                continue;
            }
            out.add(mid);
        }
        return out;
    }

    private static Set<Long> filterSupplierSeedsExcludeAdminOnly(Set<Long> rawSeeds, Map<Long, SysMenu> byId)
    {
        Set<Long> out = new HashSet<>();
        if (rawSeeds == null)
        {
            return out;
        }
        for (Long mid : rawSeeds)
        {
            SysMenu m = byId.get(mid);
            if (m == null || menuFlagYes(m.getSupplierAdminOnly()))
            {
                continue;
            }
            out.add(mid);
        }
        return out;
    }

    private Set<Long> expandSeedsWithAncestors(Set<Long> rawSeeds, Map<Long, SysMenu> byId)
    {
        Set<Long> result = new HashSet<>();
        if (rawSeeds == null)
        {
            return result;
        }
        for (Long mid : rawSeeds)
        {
            if (mid != null)
            {
                addMenuChain(result, mid, byId);
            }
        }
        return result;
    }

    private Set<Long> collectScopeMenuIdsWithAncestors(String authType)
    {
        List<SysMenu> all = sysMenuMapper.selectMenuAll();
        Map<Long, SysMenu> byId = indexMenusById(all);
        Set<Long> seed = new HashSet<>();
        for (SysMenu m : all)
        {
            if (!menuRowActive(m))
            {
                continue;
            }
            if (ScmAuthConstants.AUTH_HOSPITAL.equalsIgnoreCase(authType) && menuFlagYes(m.getDefaultOpenHospital()))
            {
                String at = normalizeAuthType(m);
                if (!ScmAuthConstants.AUTH_PLATFORM.equalsIgnoreCase(at)
                    && (ScmAuthConstants.AUTH_HOSPITAL.equalsIgnoreCase(at) || ScmAuthConstants.AUTH_HOSPITAL_SUPPLIER.equalsIgnoreCase(at)))
                {
                    seed.add(m.getMenuId());
                }
            }
            else if (ScmAuthConstants.AUTH_SUPPLIER.equalsIgnoreCase(authType) && menuFlagYes(m.getDefaultOpenSupplier()))
            {
                String at = normalizeAuthType(m);
                if (ScmAuthConstants.AUTH_SUPPLIER.equalsIgnoreCase(at)
                    || ScmAuthConstants.AUTH_HOSPITAL_SUPPLIER.equalsIgnoreCase(at))
                {
                    seed.add(m.getMenuId());
                }
            }
            else if (ScmAuthConstants.AUTH_HOSPITAL_SUPPLIER.equalsIgnoreCase(authType))
            {
                String at = normalizeAuthType(m);
                if (!ScmAuthConstants.AUTH_HOSPITAL_SUPPLIER.equalsIgnoreCase(at))
                {
                    continue;
                }
                if (menuFlagYes(m.getDefaultOpenHospital()) || menuFlagYes(m.getDefaultOpenSupplier())
                    || "1".equals(StringUtils.trimToEmpty(m.getHospitalGrantSupplierFlag())))
                {
                    seed.add(m.getMenuId());
                }
            }
        }
        return expandSeedsWithAncestors(seed, byId);
    }

    private void ensureGlobalHospitalRolesExist(String operBy)
    {
        ensureGlobalHospitalAdminRole(operBy);
        ensureGlobalHospitalStaffRole(operBy);
    }

    private void ensureGlobalSupplierRolesExist(String operBy)
    {
        ensureGlobalSupplierAdminRole(operBy);
        ensureGlobalSupplierSalesRole(operBy);
        ensureGlobalTpSupplierAdminRole(operBy);
        ensureGlobalTpSupplierSalesRole(operBy);
    }

    private List<Long> collectBuiltinSupplierGlobalRoleIds()
    {
        List<Long> ids = new ArrayList<>();
        appendGlobalRoleId(ids, ScmAuthConstants.ROLE_KEY_SUPPLIER_ADMIN);
        appendGlobalRoleId(ids, ScmAuthConstants.ROLE_KEY_SUPPLIER_SALES);
        appendGlobalRoleId(ids, ScmAuthConstants.ROLE_KEY_TP_SUPPLIER_ADMIN);
        appendGlobalRoleId(ids, ScmAuthConstants.ROLE_KEY_TP_SUPPLIER_SALES);
        return ids;
    }

    private void appendGlobalRoleId(List<Long> ids, String roleKey)
    {
        SysRole role = sysRoleMapper.selectGlobalScmRoleByKey(roleKey);
        if (role != null && role.getRoleId() != null)
        {
            ids.add(role.getRoleId());
        }
    }

    private void insertGlobalScmTemplateRole(SysRole role)
    {
        role.setHospitalId(null);
        role.setSupplierId(null);
        role.setTenantId(null);
        sysRoleMapper.insertRole(role);
    }

    private SysRole ensureGlobalHospitalAdminRole(String operBy)
    {
        SysRole exist = sysRoleMapper.selectGlobalScmRoleByKey(ScmAuthConstants.ROLE_KEY_HOSPITAL_ADMIN);
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
        role.setOrgAdmin("1");
        role.setRemark("SCM全局模板角色");
        role.setCreateBy(StringUtils.isNotEmpty(operBy) ? operBy : "system");
        role.setPinyinCode(PinyinUtils.getShortCode(role.getRoleName()));
        insertGlobalScmTemplateRole(role);
        return role;
    }

    private SysRole ensureGlobalHospitalStaffRole(String operBy)
    {
        SysRole exist = sysRoleMapper.selectGlobalScmRoleByKey(ScmAuthConstants.ROLE_KEY_HOSPITAL_STAFF);
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
        role.setOrgAdmin("0");
        role.setRemark("SCM全局模板角色");
        role.setCreateBy(StringUtils.isNotEmpty(operBy) ? operBy : "system");
        role.setPinyinCode(PinyinUtils.getShortCode(role.getRoleName()));
        insertGlobalScmTemplateRole(role);
        return role;
    }

    private SysRole ensureGlobalSupplierAdminRole(String operBy)
    {
        SysRole exist = sysRoleMapper.selectGlobalScmRoleByKey(ScmAuthConstants.ROLE_KEY_SUPPLIER_ADMIN);
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
        role.setOrgAdmin("1");
        role.setRemark("SCM全局模板角色");
        role.setCreateBy(StringUtils.isNotEmpty(operBy) ? operBy : "system");
        role.setPinyinCode(PinyinUtils.getShortCode(role.getRoleName()));
        insertGlobalScmTemplateRole(role);
        return role;
    }

    private SysRole ensureGlobalSupplierSalesRole(String operBy)
    {
        SysRole exist = sysRoleMapper.selectGlobalScmRoleByKey(ScmAuthConstants.ROLE_KEY_SUPPLIER_SALES);
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
        role.setOrgAdmin("0");
        role.setRemark("SCM全局模板角色");
        role.setCreateBy(StringUtils.isNotEmpty(operBy) ? operBy : "system");
        role.setPinyinCode(PinyinUtils.getShortCode(role.getRoleName()));
        insertGlobalScmTemplateRole(role);
        return role;
    }

    private SysRole ensureGlobalTpSupplierAdminRole(String operBy)
    {
        SysRole exist = sysRoleMapper.selectGlobalScmRoleByKey(ScmAuthConstants.ROLE_KEY_TP_SUPPLIER_ADMIN);
        if (exist != null)
        {
            return exist;
        }
        SysRole role = new SysRole();
        role.setRoleName("第三方供应商管理员");
        role.setRoleKey(ScmAuthConstants.ROLE_KEY_TP_SUPPLIER_ADMIN);
        role.setRoleSort("6");
        role.setDataScope("1");
        role.setStatus("0");
        role.setRoleType(ScmAuthConstants.ROLE_TYPE_SUPPLIER);
        role.setOrgAdmin("1");
        role.setRemark("SCM全局模板角色");
        role.setCreateBy(StringUtils.isNotEmpty(operBy) ? operBy : "system");
        role.setPinyinCode(PinyinUtils.getShortCode(role.getRoleName()));
        insertGlobalScmTemplateRole(role);
        return role;
    }

    private SysRole ensureGlobalTpSupplierSalesRole(String operBy)
    {
        SysRole exist = sysRoleMapper.selectGlobalScmRoleByKey(ScmAuthConstants.ROLE_KEY_TP_SUPPLIER_SALES);
        if (exist != null)
        {
            return exist;
        }
        SysRole role = new SysRole();
        role.setRoleName("第三方供应商业务员");
        role.setRoleKey(ScmAuthConstants.ROLE_KEY_TP_SUPPLIER_SALES);
        role.setRoleSort("16");
        role.setDataScope("1");
        role.setStatus("0");
        role.setRoleType(ScmAuthConstants.ROLE_TYPE_SUPPLIER);
        role.setOrgAdmin("0");
        role.setRemark("SCM全局模板角色");
        role.setCreateBy(StringUtils.isNotEmpty(operBy) ? operBy : "system");
        role.setPinyinCode(PinyinUtils.getShortCode(role.getRoleName()));
        insertGlobalScmTemplateRole(role);
        return role;
    }

    private void syncGlobalHospitalTemplateRoles(String operBy)
    {
        SysRole admin = ensureGlobalHospitalAdminRole(operBy);
        SysRole staff = ensureGlobalHospitalStaffRole(operBy);
        List<SysMenu> all = sysMenuMapper.selectMenuAll();
        Map<Long, SysMenu> byId = indexMenusById(all);
        Set<Long> rawSeeds = collectRawHospitalDefaultGrantSeeds();
        Set<Long> adminExpanded = expandSeedsWithAncestors(rawSeeds, byId);
        Set<Long> nonAdminExpanded = expandSeedsWithAncestors(filterHospitalSeedsExcludeAdminOnly(rawSeeds, byId), byId);
        sysRoleMenuMapper.deleteDefaultRoleMenuByRoleId(admin.getRoleId());
        sysRoleMenuMapper.deleteDefaultRoleMenuByRoleId(staff.getRoleId());
        batchInsertRoleMenus(admin.getRoleId(), adminExpanded, "", "");
        batchInsertRoleMenus(staff.getRoleId(), nonAdminExpanded, "", "");
    }

    private void syncGlobalSupplierTemplateRoles(String operBy)
    {
        SysRole admin = ensureGlobalSupplierAdminRole(operBy);
        SysRole sales = ensureGlobalSupplierSalesRole(operBy);
        SysRole tpAdmin = ensureGlobalTpSupplierAdminRole(operBy);
        SysRole tpSales = ensureGlobalTpSupplierSalesRole(operBy);
        List<SysMenu> all = sysMenuMapper.selectMenuAll();
        Map<Long, SysMenu> byId = indexMenusById(all);
        Set<Long> regularRaw = collectRegularSupplierTemplateSeeds(byId);
        Set<Long> tpRaw = collectTpSupplierTemplateSeeds(byId);
        Set<Long> regularAdminExpanded = expandSeedsWithAncestors(regularRaw, byId);
        Set<Long> regularSalesExpanded = expandSeedsWithAncestors(filterSupplierSeedsExcludeAdminOnly(regularRaw, byId), byId);
        Set<Long> tpAdminExpanded = expandSeedsWithAncestors(tpRaw, byId);
        Set<Long> tpSalesExpanded = expandSeedsWithAncestors(filterSupplierSeedsExcludeAdminOnly(tpRaw, byId), byId);
        sysRoleMenuMapper.deleteDefaultRoleMenuByRoleId(admin.getRoleId());
        sysRoleMenuMapper.deleteDefaultRoleMenuByRoleId(sales.getRoleId());
        sysRoleMenuMapper.deleteDefaultRoleMenuByRoleId(tpAdmin.getRoleId());
        sysRoleMenuMapper.deleteDefaultRoleMenuByRoleId(tpSales.getRoleId());
        batchInsertRoleMenus(admin.getRoleId(), regularAdminExpanded, "", "");
        batchInsertRoleMenus(sales.getRoleId(), regularSalesExpanded, "", "");
        batchInsertRoleMenus(tpAdmin.getRoleId(), tpAdminExpanded, "", "");
        batchInsertRoleMenus(tpSales.getRoleId(), tpSalesExpanded, "", "");
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

    /** 医院授予供应商维度：{@code scm_supplier_menu_auth.hospital_id} 必填 */
    private void batchInsertSupplierHospitalMenuAuth(Long hospitalId, Long supplierId, Set<Long> menuIds, String operBy)
    {
        Date now = DateUtils.getNowDate();
        List<ScmSupplierMenuAuth> buf = new ArrayList<>();
        for (Long menuId : menuIds)
        {
            ScmSupplierMenuAuth row = new ScmSupplierMenuAuth();
            row.setId(IdUtils.simpleUuid7());
            row.setSupplierId(supplierId);
            row.setHospitalId(hospitalId);
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchGrantHospitalSupplierMenus(Long hospitalId, List<Long> supplierIds,
        Set<Long> menuSeedIds, String operBy)
    {
        String oper = StringUtils.isNotEmpty(operBy) ? operBy : "system";
        if (hospitalId == null || supplierIds == null || supplierIds.isEmpty())
        {
            throw new ServiceException("医院或供应商列表不能为空");
        }
        Set<Long> seeds = menuSeedIds != null ? new HashSet<>(menuSeedIds) : new HashSet<>();
        seeds.removeIf(Objects::isNull);
        if (seeds.isEmpty())
        {
            throw new ServiceException("请先在菜单树中勾选至少一个菜单或按钮");
        }
        List<SysMenu> allMenus = sysMenuMapper.selectMenuAll();
        Map<Long, SysMenu> byId = indexMenusById(allMenus);
        Map<Long, List<Long>> childrenByParent = buildChildrenByParentIndex(allMenus);
        Set<Long> scope = collectScopeMenuIdsWithAncestors(ScmAuthConstants.AUTH_HOSPITAL_SUPPLIER);
        Set<Long> hospitalOwned = new HashSet<>(hospitalMenuAuthMapper.selectMenuIdsByHospitalId(hospitalId));
        Set<Long> allowed = new HashSet<>(scope);
        allowed.retainAll(hospitalOwned);
        Set<Long> closure = expandHospitalGrantMenuClosure(seeds, byId, childrenByParent);
        closure.retainAll(allowed);
        if (closure.isEmpty())
        {
            throw new ServiceException("所选菜单不在本院可授范围内或与医院菜单白名单无交集");
        }
        LinkedHashSet<Long> distinctSuppliers = new LinkedHashSet<>(supplierIds);
        int suppliersTouched = 0;
        for (Long supplierId : distinctSuppliers)
        {
            if (supplierId == null)
            {
                continue;
            }
            Set<Long> existing = new HashSet<>(supplierMenuAuthMapper.selectMenuIdsBySupplierAndHospital(supplierId, hospitalId));
            Set<Long> merged = new HashSet<>(existing);
            merged.addAll(closure);
            supplierMenuAuthMapper.deleteBySupplierAndHospital(supplierId, hospitalId);
            batchInsertSupplierHospitalMenuAuth(hospitalId, supplierId, merged, oper);
            suppliersTouched++;
        }
        Map<String, Object> ret = new LinkedHashMap<>();
        ret.put("supplierCount", suppliersTouched);
        ret.put("menuClosureSize", closure.size());
        ret.put("roleMenuInserted", 0);
        return ret;
    }

    private static Map<Long, List<Long>> buildChildrenByParentIndex(List<SysMenu> all)
    {
        Map<Long, List<Long>> map = new HashMap<>();
        if (all == null)
        {
            return map;
        }
        for (SysMenu m : all)
        {
            if (m == null || m.getMenuId() == null)
            {
                continue;
            }
            Long p = m.getParentId() == null ? 0L : m.getParentId();
            map.computeIfAbsent(p, k -> new ArrayList<>()).add(m.getMenuId());
        }
        return map;
    }

    private static Set<Long> expandHospitalGrantMenuClosure(Set<Long> seedIds, Map<Long, SysMenu> byId,
        Map<Long, List<Long>> childrenByParent)
    {
        Set<Long> acc = new LinkedHashSet<>();
        if (seedIds == null)
        {
            return acc;
        }
        for (Long seed : seedIds)
        {
            if (seed == null)
            {
                continue;
            }
            addMenuChainStatic(acc, seed, byId);
        }
        Deque<Long> dq = new ArrayDeque<>();
        for (Long seed : seedIds)
        {
            if (seed != null && seed > 0)
            {
                dq.addLast(seed);
            }
        }
        while (!dq.isEmpty())
        {
            Long id = dq.pollFirst();
            List<Long> ch = childrenByParent.get(id);
            if (ch == null)
            {
                continue;
            }
            for (Long c : ch)
            {
                if (acc.add(c))
                {
                    dq.addLast(c);
                }
            }
        }
        return acc;
    }

    private static void addMenuChainStatic(Set<Long> acc, Long menuId, Map<Long, SysMenu> byId)
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

    private void addMenuChain(Set<Long> acc, Long menuId, Map<Long, SysMenu> byId)
    {
        addMenuChainStatic(acc, menuId, byId);
    }
}
