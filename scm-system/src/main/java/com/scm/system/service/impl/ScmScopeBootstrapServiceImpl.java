package com.scm.system.service.impl;

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
import com.scm.system.domain.SysRoleMenu;
import com.scm.system.mapper.ScmHospitalMenuAuthMapper;
import com.scm.system.mapper.ScmSupplierMenuAuthMapper;
import com.scm.system.mapper.HospitalMapper;
import com.scm.system.mapper.SupplierMapper;
import com.scm.system.mapper.SysMenuMapper;
import com.scm.system.mapper.SysRoleMapper;
import com.scm.system.mapper.SysRoleMenuMapper;
import com.scm.system.service.IScmScopeBootstrapService;

/**
 * 医院/供应商维度角色与白名单、角色菜单初始化（按 sys_menu 四列开关 + 角色 org_admin）
 */
@Service
public class ScmScopeBootstrapServiceImpl implements IScmScopeBootstrapService
{
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

    @Override
    public Set<Long> listAllScopeMenuIds(String authType)
    {
        return collectScopeMenuIdsWithAncestors(authType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long bootstrapAfterSupplierRegister(Long supplierId, String operBy)
    {
        ensureSupplierAdminRole(supplierId, operBy);
        ensureSupplierSalesRole(supplierId, operBy);
        rebuildSupplierScopedMenus(supplierId, collectRawSupplierDefaultGrantSeeds(), operBy);
        SysRole admin = sysRoleMapper.selectByRoleKeyAndSupplierId(ScmAuthConstants.ROLE_KEY_SUPPLIER_ADMIN, supplierId);
        return admin != null ? admin.getRoleId() : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bootstrapAfterHospitalCreated(Long hospitalId, String operBy)
    {
        ensureHospitalAdminRole(hospitalId, operBy);
        ensureHospitalStaffRole(hospitalId, operBy);
        rebuildHospitalScopedMenus(hospitalId, collectRawHospitalDefaultGrantSeeds(), operBy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetHospitalMenuAuth(Long hospitalId, String operBy)
    {
        ensureHospitalAdminRole(hospitalId, operBy);
        ensureHospitalStaffRole(hospitalId, operBy);
        rebuildHospitalScopedMenus(hospitalId, collectRawHospitalDefaultGrantSeeds(), operBy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetSupplierMenuAuth(Long supplierId, String operBy)
    {
        ensureSupplierAdminRole(supplierId, operBy);
        ensureSupplierSalesRole(supplierId, operBy);
        rebuildSupplierScopedMenus(supplierId, collectRawSupplierDefaultGrantSeeds(), operBy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetHospitalBuiltinRoleMenus(Long hospitalId, String operBy)
    {
        ensureHospitalAdminRole(hospitalId, operBy);
        ensureHospitalStaffRole(hospitalId, operBy);
        List<SysMenu> all = sysMenuMapper.selectMenuAll();
        Map<Long, SysMenu> byId = indexMenusById(all);
        Set<Long> rawSeeds = collectRawHospitalDefaultGrantSeeds();
        Set<Long> adminExpanded = expandSeedsWithAncestors(rawSeeds, byId);
        Set<Long> nonAdminRaw = filterHospitalSeedsExcludeAdminOnly(rawSeeds, byId);
        Set<Long> nonAdminExpanded = expandSeedsWithAncestors(nonAdminRaw, byId);
        hospitalMenuAuthMapper.deleteByHospitalId(hospitalId);
        batchInsertHospitalAuth(hospitalId, adminExpanded, operBy);
        String hid = String.valueOf(hospitalId);
        SysRole admin = sysRoleMapper.selectByRoleKeyAndHospitalId(ScmAuthConstants.ROLE_KEY_HOSPITAL_ADMIN, hospitalId);
        SysRole staff = sysRoleMapper.selectByRoleKeyAndHospitalId(ScmAuthConstants.ROLE_KEY_HOSPITAL_STAFF, hospitalId);
        if (admin != null && admin.getRoleId() != null)
        {
            sysRoleMenuMapper.deleteRoleMenuByRoleId(admin.getRoleId());
            batchInsertRoleMenus(admin.getRoleId(), adminExpanded, hid, "");
        }
        if (staff != null && staff.getRoleId() != null)
        {
            sysRoleMenuMapper.deleteRoleMenuByRoleId(staff.getRoleId());
            batchInsertRoleMenus(staff.getRoleId(), nonAdminExpanded, hid, "");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetSupplierBuiltinRoleMenus(Long supplierId, String operBy)
    {
        ensureSupplierAdminRole(supplierId, operBy);
        ensureSupplierSalesRole(supplierId, operBy);
        List<SysMenu> all = sysMenuMapper.selectMenuAll();
        Map<Long, SysMenu> byId = indexMenusById(all);
        Set<Long> rawSeeds = collectRawSupplierDefaultGrantSeeds();
        Set<Long> adminExpanded = expandSeedsWithAncestors(rawSeeds, byId);
        Set<Long> nonAdminRaw = filterSupplierSeedsExcludeAdminOnly(rawSeeds, byId);
        Set<Long> nonAdminExpanded = expandSeedsWithAncestors(nonAdminRaw, byId);
        supplierMenuAuthMapper.deleteBySupplierId(supplierId);
        batchInsertSupplierAuth(supplierId, adminExpanded, operBy);
        String sid = String.valueOf(supplierId);
        SysRole admin = sysRoleMapper.selectByRoleKeyAndSupplierId(ScmAuthConstants.ROLE_KEY_SUPPLIER_ADMIN, supplierId);
        SysRole sales = sysRoleMapper.selectByRoleKeyAndSupplierId(ScmAuthConstants.ROLE_KEY_SUPPLIER_SALES, supplierId);
        if (admin != null && admin.getRoleId() != null)
        {
            sysRoleMenuMapper.deleteRoleMenuByRoleId(admin.getRoleId());
            batchInsertRoleMenus(admin.getRoleId(), adminExpanded, "", sid);
        }
        if (sales != null && sales.getRoleId() != null)
        {
            sysRoleMenuMapper.deleteRoleMenuByRoleId(sales.getRoleId());
            batchInsertRoleMenus(sales.getRoleId(), nonAdminExpanded, "", sid);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceHospitalMenuAuth(Long hospitalId, List<Long> menuIds, String operBy)
    {
        ensureHospitalAdminRole(hospitalId, operBy);
        ensureHospitalStaffRole(hospitalId, operBy);
        rebuildHospitalScopedMenus(hospitalId, normalizeMenuIdSet(menuIds), operBy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceSupplierMenuAuth(Long supplierId, List<Long> menuIds, String operBy)
    {
        ensureSupplierAdminRole(supplierId, operBy);
        ensureSupplierSalesRole(supplierId, operBy);
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
        ensureSupplierAdminRole(supplierId, oper);

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

        SysRole admin = sysRoleMapper.selectByRoleKeyAndSupplierId(ScmAuthConstants.ROLE_KEY_SUPPLIER_ADMIN, supplierId);
        if (admin == null || admin.getRoleId() == null)
        {
            return;
        }
        String sid = String.valueOf(supplierId);
        Set<Long> existingRm = new HashSet<>(sysRoleMenuMapper.selectMenuIdsByRoleAndScope(admin.getRoleId(), "", sid));
        Set<Long> needRm = new HashSet<>(toAttach);
        needRm.removeAll(existingRm);
        if (!needRm.isEmpty())
        {
            batchInsertRoleMenus(admin.getRoleId(), needRm, "", sid);
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
        Set<Long> hospitalSeedExpanded = collectScopeMenuIdsWithAncestors(ScmAuthConstants.AUTH_HOSPITAL);
        Set<Long> supplierSeedExpanded = collectScopeMenuIdsWithAncestors(ScmAuthConstants.AUTH_SUPPLIER);

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
            ensureHospitalAdminRole(hospitalId, realOper);
            ensureHospitalStaffRole(hospitalId, realOper);
            Set<Long> existingAuth = new HashSet<>(hospitalMenuAuthMapper.selectMenuIdsByHospitalId(hospitalId));
            Set<Long> needAuth = new HashSet<>(hospitalSeedExpanded);
            needAuth.removeAll(existingAuth);
            if (!needAuth.isEmpty())
            {
                batchInsertHospitalAuth(hospitalId, needAuth, realOper);
                stat.put("addedHospitalMenuAuth", stat.get("addedHospitalMenuAuth") + needAuth.size());
            }
            repairHospitalRoleMenusFromWhitelist(hospitalId, stat);
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
            Set<Long> needAuth = new HashSet<>(supplierSeedExpanded);
            needAuth.removeAll(existingAuth);
            if (!needAuth.isEmpty())
            {
                batchInsertSupplierAuth(supplierId, needAuth, realOper);
                stat.put("addedSupplierMenuAuth", stat.get("addedSupplierMenuAuth") + needAuth.size());
            }
            Set<Long> existingRoleMenus = new HashSet<>(
                sysRoleMenuMapper.selectMenuIdsByRoleAndScope(admin.getRoleId(), "", String.valueOf(supplierId)));
            Set<Long> needRoleMenus = new HashSet<>(supplierSeedExpanded);
            needRoleMenus.removeAll(existingRoleMenus);
            if (!needRoleMenus.isEmpty())
            {
                batchInsertRoleMenus(admin.getRoleId(), needRoleMenus, "", String.valueOf(supplierId));
                stat.put("addedSupplierRoleMenu", stat.get("addedSupplierRoleMenu") + needRoleMenus.size());
            }
            Set<Long> nonAdminExpanded = buildNonAdminSupplierExpandedWhitelist(collectRawSupplierDefaultGrantSeeds());
            Set<Long> existingSalesRoleMenus = new HashSet<>(
                sysRoleMenuMapper.selectMenuIdsByRoleAndScope(sales.getRoleId(), "", String.valueOf(supplierId)));
            Set<Long> needSalesRoleMenus = new HashSet<>(nonAdminExpanded);
            needSalesRoleMenus.removeAll(existingSalesRoleMenus);
            if (!needSalesRoleMenus.isEmpty())
            {
                batchInsertRoleMenus(sales.getRoleId(), needSalesRoleMenus, "", String.valueOf(supplierId));
                stat.put("addedSupplierSalesRoleMenu", stat.get("addedSupplierSalesRoleMenu") + needSalesRoleMenus.size());
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
        Set<Long> nonAdminRaw = filterHospitalSeedsExcludeAdminOnly(rawSeedMenuIds, byId);
        Set<Long> nonAdminExpanded = expandSeedsWithAncestors(nonAdminRaw, byId);
        List<SysRole> roles = sysRoleMapper.selectRolesByHospitalId(hospitalId);
        if (roles == null)
        {
            return;
        }
        for (SysRole r : roles)
        {
            if (r == null || !ScmAuthConstants.ROLE_TYPE_HOSPITAL.equalsIgnoreCase(StringUtils.trimToEmpty(r.getRoleType())))
            {
                continue;
            }
            if (isOrgAdminRole(r))
            {
                batchInsertRoleMenus(r.getRoleId(), adminExpanded, hid, "");
            }
            else
            {
                batchInsertRoleMenus(r.getRoleId(), nonAdminExpanded, hid, "");
            }
        }
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
        Set<Long> nonAdminRaw = filterSupplierSeedsExcludeAdminOnly(rawSeedMenuIds, byId);
        Set<Long> nonAdminExpanded = expandSeedsWithAncestors(nonAdminRaw, byId);
        List<SysRole> roles = sysRoleMapper.selectRolesBySupplierId(supplierId);
        if (roles == null)
        {
            return;
        }
        for (SysRole r : roles)
        {
            if (r == null || !ScmAuthConstants.ROLE_TYPE_SUPPLIER.equalsIgnoreCase(StringUtils.trimToEmpty(r.getRoleType())))
            {
                continue;
            }
            if (isOrgAdminRole(r))
            {
                batchInsertRoleMenus(r.getRoleId(), adminExpanded, "", sid);
            }
            else
            {
                batchInsertRoleMenus(r.getRoleId(), nonAdminExpanded, "", sid);
            }
        }
    }

    private Set<Long> buildNonAdminSupplierExpandedWhitelist(Set<Long> rawSupplierSeeds)
    {
        List<SysMenu> all = sysMenuMapper.selectMenuAll();
        Map<Long, SysMenu> byId = indexMenusById(all);
        Set<Long> nonAdminRaw = filterSupplierSeedsExcludeAdminOnly(rawSupplierSeeds, byId);
        return expandSeedsWithAncestors(nonAdminRaw, byId);
    }

    private static boolean isOrgAdminRole(SysRole r)
    {
        if (r == null)
        {
            return false;
        }
        if ("1".equals(StringUtils.trimToEmpty(r.getOrgAdmin())))
        {
            return true;
        }
        String key = StringUtils.trimToEmpty(r.getRoleKey());
        return ScmAuthConstants.ROLE_KEY_HOSPITAL_ADMIN.equals(key) || ScmAuthConstants.ROLE_KEY_SUPPLIER_ADMIN.equals(key);
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
            if (ScmAuthConstants.AUTH_SUPPLIER.equalsIgnoreCase(at))
            {
                seed.add(m.getMenuId());
            }
            else if (ScmAuthConstants.AUTH_HOSPITAL_SUPPLIER.equalsIgnoreCase(at))
            {
                if (!"1".equals(StringUtils.trimToEmpty(m.getHospitalGrantSupplierFlag())))
                {
                    seed.add(m.getMenuId());
                }
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
                    || (ScmAuthConstants.AUTH_HOSPITAL_SUPPLIER.equalsIgnoreCase(at)
                        && !"1".equals(StringUtils.trimToEmpty(m.getHospitalGrantSupplierFlag()))))
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
        role.setOrgAdmin("1");
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
        role.setOrgAdmin("0");
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
        role.setOrgAdmin("1");
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
        role.setOrgAdmin("0");
        role.setRemark("系统自动创建");
        role.setCreateBy(StringUtils.isNotEmpty(operBy) ? operBy : "system");
        role.setPinyinCode(PinyinUtils.getShortCode(role.getRoleName()));
        sysRoleMapper.insertRole(role);
        return role;
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
        int rmInserted = 0;
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
            String sidStr = String.valueOf(supplierId);
            List<SysRole> roles = sysRoleMapper.selectRolesBySupplierId(supplierId);
            if (roles == null)
            {
                continue;
            }
            for (SysRole role : roles)
            {
                if (role == null || role.getRoleId() == null)
                {
                    continue;
                }
                Set<Long> haveRm = new HashSet<>(
                    sysRoleMenuMapper.selectMenuIdsByRoleAndScope(role.getRoleId(), "", sidStr));
                List<SysRoleMenu> buf = new ArrayList<>();
                for (Long mid : merged)
                {
                    if (haveRm.contains(mid))
                    {
                        continue;
                    }
                    SysRoleMenu rm = new SysRoleMenu();
                    rm.setId(IdUtils.simpleUuid7());
                    rm.setRoleId(role.getRoleId());
                    rm.setMenuId(mid);
                    rm.setHospitalId("");
                    rm.setSupplierId(sidStr);
                    buf.add(rm);
                    if (buf.size() >= BATCH)
                    {
                        rmInserted += sysRoleMenuMapper.batchRoleMenuIgnore(buf);
                        buf.clear();
                    }
                }
                if (!buf.isEmpty())
                {
                    rmInserted += sysRoleMenuMapper.batchRoleMenuIgnore(buf);
                }
            }
        }
        Map<String, Object> ret = new LinkedHashMap<>();
        ret.put("supplierCount", suppliersTouched);
        ret.put("menuClosureSize", closure.size());
        ret.put("roleMenuInserted", rmInserted);
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

    /**
     * 按医院菜单白名单（scm_hospital_menu_auth）为该院全部医院角色补缺 sys_role_menu。
     * 白名单仅 migration 写入、未点「保存」时，界面已勾选但用户会话无 order:order:void 等权限，需由此同步。
     */
    private void repairHospitalRoleMenusFromWhitelist(Long hospitalId, Map<String, Integer> stat)
    {
        if (hospitalId == null || stat == null)
        {
            return;
        }
        List<Long> whitelistRaw = hospitalMenuAuthMapper.selectMenuIdsByHospitalId(hospitalId);
        Set<Long> rawSeeds = new HashSet<>();
        if (whitelistRaw != null && !whitelistRaw.isEmpty())
        {
            for (Long id : whitelistRaw)
            {
                if (id != null)
                {
                    rawSeeds.add(id);
                }
            }
        }
        if (rawSeeds.isEmpty())
        {
            rawSeeds = collectRawHospitalDefaultGrantSeeds();
        }
        List<SysMenu> all = sysMenuMapper.selectMenuAll();
        Map<Long, SysMenu> byId = indexMenusById(all);
        Set<Long> adminExpanded = expandSeedsWithAncestors(rawSeeds, byId);
        Set<Long> nonAdminRaw = filterHospitalSeedsExcludeAdminOnly(rawSeeds, byId);
        Set<Long> nonAdminExpanded = expandSeedsWithAncestors(nonAdminRaw, byId);
        String hid = String.valueOf(hospitalId);
        List<SysRole> roles = sysRoleMapper.selectRolesByHospitalId(hospitalId);
        if (roles == null)
        {
            return;
        }
        for (SysRole r : roles)
        {
            if (r == null || r.getRoleId() == null
                || !ScmAuthConstants.ROLE_TYPE_HOSPITAL.equalsIgnoreCase(StringUtils.trimToEmpty(r.getRoleType())))
            {
                continue;
            }
            Set<Long> target = isOrgAdminRole(r) ? adminExpanded : nonAdminExpanded;
            Set<Long> existing = new HashSet<>(sysRoleMenuMapper.selectMenuIdsByRoleAndScope(r.getRoleId(), hid, ""));
            Set<Long> need = new HashSet<>(target);
            need.removeAll(existing);
            if (need.isEmpty())
            {
                continue;
            }
            batchInsertRoleMenus(r.getRoleId(), need, hid, "");
            if (isOrgAdminRole(r))
            {
                stat.put("addedHospitalRoleMenu", stat.get("addedHospitalRoleMenu") + need.size());
            }
            else
            {
                stat.put("addedHospitalStaffRoleMenu", stat.get("addedHospitalStaffRoleMenu") + need.size());
            }
        }
    }
}
