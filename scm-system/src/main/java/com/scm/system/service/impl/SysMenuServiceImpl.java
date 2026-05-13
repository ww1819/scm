package com.scm.system.service.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.scm.common.constant.UserConstants;
import com.scm.common.core.domain.Ztree;
import com.scm.common.core.domain.entity.SysMenu;
import com.scm.common.core.domain.entity.SysMenuChangeLog;
import com.scm.common.core.domain.entity.SysRole;
import com.scm.common.core.domain.entity.SysUser;
import com.scm.common.core.text.Convert;
import com.scm.common.exception.ServiceException;
import com.scm.common.utils.DateUtils;
import com.scm.common.utils.ShiroUtils;
import com.scm.common.utils.StringUtils;
import com.scm.common.utils.uuid.IdUtils;
import com.scm.common.utils.scm.ScmMenuMetadataInferer;
import com.scm.common.utils.scm.ScmMenuSnapshotHelper;
import com.scm.system.domain.MenuGrantHospitalAuthPair;
import com.scm.system.domain.MenuGrantHospitalRoleRef;
import com.scm.system.domain.MenuGrantRoleMenuPair;
import com.scm.system.domain.MenuGrantSupplierAuthPair;
import com.scm.system.domain.MenuGrantSupplierRoleRef;
import com.scm.system.domain.ScmHospitalMenuAuth;
import com.scm.system.domain.ScmSupplierMenuAuth;
import com.scm.system.domain.SysRoleMenu;
import com.scm.system.mapper.HospitalMapper;
import com.scm.system.mapper.ScmHospitalMenuAuthMapper;
import com.scm.system.mapper.ScmSupplierMenuAuthMapper;
import com.scm.system.mapper.SupplierMapper;
import com.scm.system.mapper.SysMenuChangeLogMapper;
import com.scm.system.mapper.SysMenuMapper;
import com.scm.system.mapper.SysRoleMapper;
import com.scm.system.mapper.SysRoleMenuMapper;
import com.scm.system.service.ISysMenuService;

/**
 * 菜单 业务层处理
 * 
 * @author scm
 */
@Service
public class SysMenuServiceImpl implements ISysMenuService
{
    public static final String PREMISSION_STRING = "perms[\"{0}\"]";

    @Autowired
    private SysMenuMapper menuMapper;

    @Autowired
    private SysRoleMenuMapper roleMenuMapper;

    @Autowired
    private HospitalMapper hospitalMapper;

    @Autowired
    private SupplierMapper supplierMapper;

    @Autowired
    private ScmHospitalMenuAuthMapper hospitalMenuAuthMapper;

    @Autowired
    private ScmSupplierMenuAuthMapper supplierMenuAuthMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysMenuChangeLogMapper menuChangeLogMapper;

    private static final int GLOBAL_GRANT_BATCH = 400;

    /**
     * 根据用户查询菜单
     * 
     * @param user 用户信息
     * @return 菜单列表
     */
    @Override
    public List<SysMenu> selectMenusByUser(SysUser user)
    {
        List<SysMenu> menus = new LinkedList<SysMenu>();
        // 管理员显示所有菜单信息
        if (user.isAdmin())
        {
            menus = menuMapper.selectMenuNormalAll();
        }
        else
        {
            menus = menuMapper.selectMenusByUserId(user.getUserId());
        }
        return getChildPerms(menus, 0);
    }

    /**
     * 查询菜单集合
     * 
     * @return 所有菜单信息
     */
    @Override
    public List<SysMenu> selectMenuList(SysMenu menu, Long userId)
    {
        List<SysMenu> menuList = null;
        if (SysUser.isAdmin(userId))
        {
            menuList = menuMapper.selectMenuList(menu);
        }
        else
        {
            menu.getParams().put("userId", userId);
            menuList = menuMapper.selectMenuListByUserId(menu);
        }
        return menuList;
    }

    /**
     * 查询菜单集合
     * 
     * @return 所有菜单信息
     */
    @Override
    public List<SysMenu> selectMenuAll(Long userId)
    {
        List<SysMenu> menuList = null;
        if (SysUser.isAdmin(userId))
        {
            menuList = menuMapper.selectMenuAll();
        }
        else
        {
            menuList = menuMapper.selectMenuAllByUserId(userId);
        }
        return menuList;
    }

    /**
     * 根据用户ID查询权限
     * 
     * @param userId 用户ID
     * @return 权限列表
     */
    @Override
    public Set<String> selectPermsByUserId(Long userId)
    {
        List<String> perms = menuMapper.selectPermsByUserId(userId);
        Set<String> permsSet = new HashSet<>();
        for (String perm : perms)
        {
            if (StringUtils.isNotEmpty(perm))
            {
                permsSet.addAll(Arrays.asList(perm.trim().split(",")));
            }
        }
        return permsSet;
    }

    /**
     * 根据角色ID查询权限
     * 
     * @param roleId 角色ID
     * @return 权限列表
     */
    @Override
    public Set<String> selectPermsByRoleId(Long roleId)
    {
        List<String> perms = menuMapper.selectPermsByRoleId(roleId);
        Set<String> permsSet = new HashSet<>();
        for (String perm : perms)
        {
            if (StringUtils.isNotEmpty(perm))
            {
                permsSet.addAll(Arrays.asList(perm.trim().split(",")));
            }
        }
        return permsSet;
    }

    /**
     * 根据角色ID查询菜单
     * 
     * @param role 角色对象
     * @return 菜单列表
     */
    @Override
    public List<Ztree> roleMenuTreeData(SysRole role, Long userId)
    {
        Long roleId = role.getRoleId();
        List<Ztree> ztrees = new ArrayList<Ztree>();
        List<SysMenu> menuList = selectMenuAll(userId);
        if (StringUtils.isNotNull(roleId))
        {
            List<String> roleMenuList = menuMapper.selectMenuTree(roleId);
            ztrees = initZtree(menuList, roleMenuList, true);
        }
        else
        {
            ztrees = initZtree(menuList, null, true);
        }
        return ztrees;
    }

    /**
     * 查询所有菜单
     * 
     * @return 菜单列表
     */
    @Override
    public List<Ztree> menuTreeData(Long userId)
    {
        List<SysMenu> menuList = selectMenuAll(userId);
        List<Ztree> ztrees = initZtree(menuList);
        return ztrees;
    }

    @Override
    public List<Ztree> menuTreeDataWithChecked(Long userId, List<Long> checkedMenuIds)
    {
        List<SysMenu> menuList = selectMenuAll(userId);
        return buildMenuTreeWithChecked(menuList, checkedMenuIds);
    }

    @Override
    public List<Ztree> buildMenuTreeWithChecked(List<SysMenu> menuList, List<Long> checkedMenuIds)
    {
        List<String> checkList = null;
        if (checkedMenuIds != null && !checkedMenuIds.isEmpty() && menuList != null)
        {
            checkList = new ArrayList<>();
            for (SysMenu m : menuList)
            {
                if (checkedMenuIds.contains(m.getMenuId()))
                {
                    checkList.add(m.getMenuId() + (m.getPerms() != null ? m.getPerms() : ""));
                }
            }
        }
        return initZtree(menuList != null ? menuList : new ArrayList<SysMenu>(), checkList, true);
    }

    @Override
    public List<SysMenu> selectInvalidMenuNameList(Long userId)
    {
        List<SysMenu> menuList = selectMenuAll(userId);
        List<SysMenu> invalidList = new ArrayList<>();
        if (menuList == null || menuList.isEmpty())
        {
            return invalidList;
        }
        for (SysMenu menu : menuList)
        {
            if (menu == null || menu.getMenuId() == null)
            {
                continue;
            }
            if (isInvalidNodeText(StringUtils.trim(menu.getMenuName())))
            {
                invalidList.add(menu);
            }
        }
        return invalidList;
    }

    /**
     * 查询系统所有权限
     * 
     * @return 权限列表
     */
    @Override
    public LinkedHashMap<String, String> selectPermsAll(Long userId)
    {
        LinkedHashMap<String, String> section = new LinkedHashMap<>();
        List<SysMenu> permissions = selectMenuAll(userId);
        if (StringUtils.isNotEmpty(permissions))
        {
            for (SysMenu menu : permissions)
            {
                section.put(menu.getUrl(), MessageFormat.format(PREMISSION_STRING, menu.getPerms()));
            }
        }
        return section;
    }

    /**
     * 对象转菜单树
     * 
     * @param menuList 菜单列表
     * @return 树结构列表
     */
    public List<Ztree> initZtree(List<SysMenu> menuList)
    {
        return initZtree(menuList, null, false);
    }

    /**
     * 对象转菜单树
     * 
     * @param menuList 菜单列表
     * @param roleMenuList 角色已存在菜单列表
     * @param permsFlag 是否需要显示权限标识
     * @return 树结构列表
     */
    public List<Ztree> initZtree(List<SysMenu> menuList, List<String> roleMenuList, boolean permsFlag)
    {
        List<Ztree> ztrees = new ArrayList<Ztree>();
        boolean isCheck = StringUtils.isNotNull(roleMenuList);
        for (SysMenu menu : menuList)
        {
            if (menu == null || menu.getMenuId() == null)
            {
                continue;
            }
            Ztree ztree = new Ztree();
            ztree.setId(menu.getMenuId());
            ztree.setpId(menu.getParentId() == null ? 0L : menu.getParentId());
            ztree.setName(transMenuName(menu, permsFlag));
            ztree.setTitle(resolveMenuDisplayName(menu));
            if (isCheck)
            {
                ztree.setChecked(roleMenuList.contains(menu.getMenuId() + menu.getPerms()));
            }
            ztrees.add(ztree);
        }
        return ztrees;
    }

    public String transMenuName(SysMenu menu, boolean permsFlag)
    {
        StringBuffer sb = new StringBuffer();
        String perms = normalizePerms(menu == null ? null : menu.getPerms());
        sb.append(resolveMenuDisplayName(menu));
        if (permsFlag && StringUtils.isNotEmpty(perms))
        {
            sb.append("<font color=\"#888\">&nbsp;&nbsp;&nbsp;" + perms + "</font>");
        }
        return sb.toString();
    }

    private String resolveMenuDisplayName(SysMenu menu)
    {
        if (menu == null)
        {
            return "未命名菜单";
        }
        String menuName = StringUtils.trim(menu.getMenuName());
        if (isInvalidNodeText(menuName))
        {
            String perms = normalizePerms(menu.getPerms());
            if (StringUtils.isNotEmpty(perms))
            {
                return "未命名菜单(" + perms + ")";
            }
            String url = StringUtils.trim(menu.getUrl());
            if (StringUtils.isNotEmpty(url))
            {
                return "未命名菜单(" + url + ")";
            }
            return "未命名菜单#" + menu.getMenuId();
        }
        return menuName;
    }

    private String normalizePerms(String perms)
    {
        String normalized = StringUtils.trim(perms);
        if (isInvalidNodeText(normalized))
        {
            return "";
        }
        return normalized;
    }

    private boolean isInvalidNodeText(String text)
    {
        if (StringUtils.isEmpty(text))
        {
            return true;
        }
        return "undefined".equalsIgnoreCase(text) || "null".equalsIgnoreCase(text);
    }

    /**
     * 删除菜单管理信息
     * 
     * @param menuId 菜单ID
     * @return 结果
     */
    @Override
    public int deleteMenuById(Long menuId)
    {
        List<Long> ids = menuMapper.selectMenuIdsInSoftDeleteSubtree(menuId);
        if (ids == null || ids.isEmpty())
        {
            return 0;
        }
        String oper = currentOperName();
        for (Long id : ids)
        {
            SysMenu snap = menuMapper.selectMenuById(id);
            if (snap != null)
            {
                JSONObject o = new JSONObject();
                o.put("before", ScmMenuSnapshotHelper.toSnapshot(snap));
                insertMenuChangeLog(id, "D", oper, o.toJSONString());
            }
        }
        return menuMapper.deleteMenuById(menuId);
    }

    /**
     * 根据菜单ID查询信息
     * 
     * @param menuId 菜单ID
     * @return 菜单信息
     */
    @Override
    public SysMenu selectMenuById(Long menuId)
    {
        return menuMapper.selectMenuById(menuId);
    }

    /**
     * 查询子菜单数量
     * 
     * @param parentId 父级菜单ID
     * @return 结果
     */
    @Override
    public int selectCountMenuByParentId(Long parentId)
    {
        return menuMapper.selectCountMenuByParentId(parentId);
    }

    /**
     * 查询菜单使用数量
     * 
     * @param menuId 菜单ID
     * @return 结果
     */
    @Override
    public int selectCountRoleMenuByMenuId(Long menuId)
    {
        return roleMenuMapper.selectCountRoleMenuByMenuId(menuId);
    }

    /**
     * 新增保存菜单信息
     * 
     * @param menu 菜单信息
     * @return 结果
     */
    @Override
    public int insertMenu(SysMenu menu)
    {
        ScmMenuMetadataInferer.applyInference(menu, true);
        int rows = menuMapper.insertMenu(menu);
        if (rows > 0 && menu.getMenuId() != null)
        {
            SysMenu loaded = menuMapper.selectMenuById(menu.getMenuId());
            if (loaded != null)
            {
                insertMenuChangeLog(loaded.getMenuId(), "I", nz(menu.getCreateBy()), jsonAfterOnly(loaded));
            }
        }
        return rows;
    }

    /**
     * 修改保存菜单信息
     * 
     * @param menu 菜单信息
     * @return 结果
     */
    @Override
    public int updateMenu(SysMenu menu)
    {
        SysMenu before = menu.getMenuId() != null ? menuMapper.selectMenuById(menu.getMenuId()) : null;
        ScmMenuMetadataInferer.applyInference(menu, true);
        int rows = menuMapper.updateMenu(menu);
        if (rows > 0 && before != null)
        {
            SysMenu after = menuMapper.selectMenuById(menu.getMenuId());
            if (after != null)
            {
                insertMenuChangeLog(after.getMenuId(), "U", nz(menu.getUpdateBy()), jsonBeforeAfter(before, after));
            }
        }
        return rows;
    }

    @Override
    public SysMenu previewInferScmMenuMetadata(String perms, String url, String menuName)
    {
        SysMenu m = new SysMenu();
        m.setPerms(perms);
        m.setUrl(url);
        m.setMenuName(menuName);
        ScmMenuMetadataInferer.applyInference(m, false);
        return m;
    }

    /**
     * 保存菜单排序
     * 
     * @param menuIds 菜单ID
     * @param orderNums 排序ID
     */
    @Transactional
    public void updateMenuSort(String[] menuIds, String[] orderNums)
    {
        try
        {
            String oper = currentOperName();
            for (int i = 0; i < menuIds.length; i++)
            {
                Long mid = Convert.toLong(menuIds[i]);
                SysMenu before = menuMapper.selectMenuById(mid);
                SysMenu menu = new SysMenu();
                menu.setMenuId(mid);
                menu.setOrderNum(orderNums[i]);
                menuMapper.updateMenuSort(menu);
                SysMenu after = menuMapper.selectMenuById(mid);
                if (before != null && after != null
                    && !String.valueOf(before.getOrderNum()).equals(String.valueOf(after.getOrderNum())))
                {
                    insertMenuChangeLog(after.getMenuId(), "S", oper, jsonBeforeAfter(before, after));
                }
            }
        }
        catch (Exception e)
        {
            throw new ServiceException("保存排序异常，请联系管理员");
        }
    }

    @Override
    public List<SysMenuChangeLog> selectMenuChangeLogList(Long menuId)
    {
        if (menuId == null)
        {
            return new ArrayList<>();
        }
        return menuChangeLogMapper.selectByMenuIdOrderAsc(String.valueOf(menuId));
    }

    @Override
    public SysMenuChangeLog selectMenuChangeLogById(String logId)
    {
        return menuChangeLogMapper.selectByLogId(logId);
    }

    private void insertMenuChangeLog(Long menuId, String changeType, String operBy, String snapshotJson)
    {
        if (menuId == null || StringUtils.isEmpty(snapshotJson))
        {
            return;
        }
        SysMenuChangeLog row = new SysMenuChangeLog();
        row.setLogId(IdUtils.dashedUuid7());
        row.setMenuId(String.valueOf(menuId));
        row.setChangeType(changeType);
        row.setOperBy(operBy);
        row.setMenuSnapshot(snapshotJson);
        menuChangeLogMapper.insertSysMenuChangeLog(row);
    }

    private static String jsonAfterOnly(SysMenu after)
    {
        JSONObject o = new JSONObject();
        o.put("after", ScmMenuSnapshotHelper.toSnapshot(after));
        return o.toJSONString();
    }

    private static String jsonBeforeAfter(SysMenu before, SysMenu after)
    {
        JSONObject o = new JSONObject();
        o.put("before", ScmMenuSnapshotHelper.toSnapshot(before));
        o.put("after", ScmMenuSnapshotHelper.toSnapshot(after));
        return o.toJSONString();
    }

    private static String nz(String s)
    {
        return s == null ? "" : s;
    }

    private static String currentOperName()
    {
        try
        {
            return nz(ShiroUtils.getLoginName());
        }
        catch (Exception e)
        {
            return "";
        }
    }

    /**
     * 校验菜单名称是否唯一
     * 
     * @param menu 菜单信息
     * @return 结果
     */
    @Override
    public boolean checkMenuNameUnique(SysMenu menu)
    {
        Long menuId = StringUtils.isNull(menu.getMenuId()) ? -1L : menu.getMenuId();
        SysMenu info = menuMapper.checkMenuNameUnique(menu.getMenuName(), menu.getParentId());
        if (StringUtils.isNotNull(info) && info.getMenuId().longValue() != menuId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 根据父节点的ID获取所有子节点
     * 
     * @param list 分类表
     * @param parentId 传入的父节点ID
     * @return String
     */
    public List<SysMenu> getChildPerms(List<SysMenu> list, int parentId)
    {
        List<SysMenu> returnList = new ArrayList<SysMenu>();
        for (Iterator<SysMenu> iterator = list.iterator(); iterator.hasNext();)
        {
            SysMenu t = (SysMenu) iterator.next();
            // 一、根据传入的某个父节点ID,遍历该父节点的所有子节点
            if (t.getParentId() == parentId)
            {
                recursionFn(list, t);
                returnList.add(t);
            }
        }
        return returnList;
    }

    /**
     * 递归列表
     * 
     * @param list
     * @param t
     */
    private void recursionFn(List<SysMenu> list, SysMenu t)
    {
        // 得到子节点列表
        List<SysMenu> childList = getChildList(list, t);
        t.setChildren(childList);
        for (SysMenu tChild : childList)
        {
            if (hasChild(list, tChild))
            {
                recursionFn(list, tChild);
            }
        }
    }

    /**
     * 得到子节点列表
     */
    private List<SysMenu> getChildList(List<SysMenu> list, SysMenu t)
    {
        List<SysMenu> tlist = new ArrayList<SysMenu>();
        Iterator<SysMenu> it = list.iterator();
        while (it.hasNext())
        {
            SysMenu n = (SysMenu) it.next();
            if (n.getParentId().longValue() == t.getMenuId().longValue())
            {
                tlist.add(n);
            }
        }
        return tlist;
    }

    /**
     * 判断是否有子节点
     */
    private boolean hasChild(List<SysMenu> list, SysMenu t)
    {
        return getChildList(list, t).size() > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> grantMenuToAllHospitalsAndRoles(Long menuId, String operBy)
    {
        if (menuId == null || menuId <= 0)
        {
            throw new ServiceException("菜单ID无效");
        }
        if (menuMapper.selectMenuById(menuId) == null)
        {
            throw new ServiceException("菜单不存在");
        }
        Set<Long> grantMenus = Collections.singleton(menuId);
        Date now = DateUtils.getNowDate();
        String oper = StringUtils.isNotEmpty(operBy) ? operBy : "system";
        List<Long> hospitalIds = hospitalMapper.selectActiveHospitalIds();
        if (hospitalIds == null)
        {
            hospitalIds = new ArrayList<>();
        }
        Map<Long, Set<Long>> menusByHospital = new HashMap<>();
        List<MenuGrantHospitalAuthPair> authPairs = hospitalMenuAuthMapper.selectAuthPairsForActiveHospitals();
        if (authPairs != null)
        {
            for (MenuGrantHospitalAuthPair p : authPairs)
            {
                if (p.getHospitalId() == null || p.getMenuId() == null)
                {
                    continue;
                }
                menusByHospital.computeIfAbsent(p.getHospitalId(), k -> new HashSet<>()).add(p.getMenuId());
            }
        }
        Map<Long, Set<Long>> menusByRole = new HashMap<>();
        List<MenuGrantRoleMenuPair> rmPairs = roleMenuMapper.selectHospitalRoleMenuPairsForActiveHospitals();
        if (rmPairs != null)
        {
            for (MenuGrantRoleMenuPair p : rmPairs)
            {
                if (p.getRoleId() == null || p.getMenuId() == null)
                {
                    continue;
                }
                menusByRole.computeIfAbsent(p.getRoleId(), k -> new HashSet<>()).add(p.getMenuId());
            }
        }
        List<MenuGrantHospitalRoleRef> roleRefs = sysRoleMapper.selectActiveHospitalRoleRefs();
        if (roleRefs == null)
        {
            roleRefs = new ArrayList<>();
        }
        int authInserted = 0;
        int rmInserted = 0;
        List<ScmHospitalMenuAuth> authBuf = new ArrayList<>();
        for (Long hid : hospitalIds)
        {
            if (hid == null)
            {
                continue;
            }
            Set<Long> haveAuth = menusByHospital.computeIfAbsent(hid, k -> new HashSet<>());
            for (Long mid : grantMenus)
            {
                if (haveAuth.contains(mid))
                {
                    continue;
                }
                ScmHospitalMenuAuth row = new ScmHospitalMenuAuth();
                row.setId(IdUtils.simpleUuid7());
                row.setHospitalId(hid);
                row.setMenuId(mid);
                row.setCreateBy(oper);
                row.setCreateTime(now);
                authBuf.add(row);
                haveAuth.add(mid);
                if (authBuf.size() >= GLOBAL_GRANT_BATCH)
                {
                    authInserted += hospitalMenuAuthMapper.batchInsertIgnore(authBuf);
                    authBuf.clear();
                }
            }
        }
        if (!authBuf.isEmpty())
        {
            authInserted += hospitalMenuAuthMapper.batchInsertIgnore(authBuf);
        }
        List<SysRoleMenu> rmBuf = new ArrayList<>();
        for (MenuGrantHospitalRoleRef ref : roleRefs)
        {
            if (ref.getRoleId() == null || ref.getHospitalId() == null)
            {
                continue;
            }
            String hidStr = String.valueOf(ref.getHospitalId());
            Set<Long> haveRm = menusByRole.computeIfAbsent(ref.getRoleId(), k -> new HashSet<>());
            for (Long mid : grantMenus)
            {
                if (haveRm.contains(mid))
                {
                    continue;
                }
                SysRoleMenu rm = new SysRoleMenu();
                rm.setId(IdUtils.simpleUuid7());
                rm.setRoleId(ref.getRoleId());
                rm.setMenuId(mid);
                rm.setHospitalId(hidStr);
                rm.setSupplierId("");
                rmBuf.add(rm);
                haveRm.add(mid);
                if (rmBuf.size() >= GLOBAL_GRANT_BATCH)
                {
                    rmInserted += roleMenuMapper.batchRoleMenuIgnore(rmBuf);
                    rmBuf.clear();
                }
            }
        }
        if (!rmBuf.isEmpty())
        {
            rmInserted += roleMenuMapper.batchRoleMenuIgnore(rmBuf);
        }
        Map<String, Object> ret = new LinkedHashMap<>();
        ret.put("hospitalCount", hospitalIds.size());
        ret.put("hospitalAuthInserted", authInserted);
        ret.put("roleMenuInserted", rmInserted);
        ret.put("grantMenuCount", grantMenus.size());
        return ret;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> grantMenuToAllSuppliersAndRoles(Long menuId, String operBy)
    {
        if (menuId == null || menuId <= 0)
        {
            throw new ServiceException("菜单ID无效");
        }
        if (menuMapper.selectMenuById(menuId) == null)
        {
            throw new ServiceException("菜单不存在");
        }
        Set<Long> grantMenus = Collections.singleton(menuId);
        Date now = DateUtils.getNowDate();
        String oper = StringUtils.isNotEmpty(operBy) ? operBy : "system";
        List<Long> supplierIds = supplierMapper.selectActiveSupplierIds();
        if (supplierIds == null)
        {
            supplierIds = new ArrayList<>();
        }
        Map<Long, Set<Long>> menusBySupplier = new HashMap<>();
        List<MenuGrantSupplierAuthPair> authPairs = supplierMenuAuthMapper.selectGlobalAuthPairsForActiveSuppliers();
        if (authPairs != null)
        {
            for (MenuGrantSupplierAuthPair p : authPairs)
            {
                if (p.getSupplierId() == null || p.getMenuId() == null)
                {
                    continue;
                }
                menusBySupplier.computeIfAbsent(p.getSupplierId(), k -> new HashSet<>()).add(p.getMenuId());
            }
        }
        Map<Long, Set<Long>> menusByRole = new HashMap<>();
        List<MenuGrantRoleMenuPair> rmPairs = roleMenuMapper.selectSupplierRoleMenuPairsForActiveSuppliers();
        if (rmPairs != null)
        {
            for (MenuGrantRoleMenuPair p : rmPairs)
            {
                if (p.getRoleId() == null || p.getMenuId() == null)
                {
                    continue;
                }
                menusByRole.computeIfAbsent(p.getRoleId(), k -> new HashSet<>()).add(p.getMenuId());
            }
        }
        List<MenuGrantSupplierRoleRef> roleRefs = sysRoleMapper.selectActiveSupplierRoleRefs();
        if (roleRefs == null)
        {
            roleRefs = new ArrayList<>();
        }
        int authInserted = 0;
        int rmInserted = 0;
        List<ScmSupplierMenuAuth> authBuf = new ArrayList<>();
        for (Long sid : supplierIds)
        {
            if (sid == null)
            {
                continue;
            }
            Set<Long> haveAuth = menusBySupplier.computeIfAbsent(sid, k -> new HashSet<>());
            for (Long mid : grantMenus)
            {
                if (haveAuth.contains(mid))
                {
                    continue;
                }
                ScmSupplierMenuAuth row = new ScmSupplierMenuAuth();
                row.setId(IdUtils.simpleUuid7());
                row.setSupplierId(sid);
                row.setHospitalId(null);
                row.setMenuId(mid);
                row.setCreateBy(oper);
                row.setCreateTime(now);
                authBuf.add(row);
                haveAuth.add(mid);
                if (authBuf.size() >= GLOBAL_GRANT_BATCH)
                {
                    authInserted += supplierMenuAuthMapper.batchInsertIgnore(authBuf);
                    authBuf.clear();
                }
            }
        }
        if (!authBuf.isEmpty())
        {
            authInserted += supplierMenuAuthMapper.batchInsertIgnore(authBuf);
        }
        List<SysRoleMenu> rmBuf = new ArrayList<>();
        for (MenuGrantSupplierRoleRef ref : roleRefs)
        {
            if (ref.getRoleId() == null || ref.getSupplierId() == null)
            {
                continue;
            }
            String sidStr = String.valueOf(ref.getSupplierId());
            Set<Long> haveRm = menusByRole.computeIfAbsent(ref.getRoleId(), k -> new HashSet<>());
            for (Long mid : grantMenus)
            {
                if (haveRm.contains(mid))
                {
                    continue;
                }
                SysRoleMenu rm = new SysRoleMenu();
                rm.setId(IdUtils.simpleUuid7());
                rm.setRoleId(ref.getRoleId());
                rm.setMenuId(mid);
                rm.setHospitalId("");
                rm.setSupplierId(sidStr);
                rmBuf.add(rm);
                haveRm.add(mid);
                if (rmBuf.size() >= GLOBAL_GRANT_BATCH)
                {
                    rmInserted += roleMenuMapper.batchRoleMenuIgnore(rmBuf);
                    rmBuf.clear();
                }
            }
        }
        if (!rmBuf.isEmpty())
        {
            rmInserted += roleMenuMapper.batchRoleMenuIgnore(rmBuf);
        }
        Map<String, Object> ret = new LinkedHashMap<>();
        ret.put("supplierCount", supplierIds.size());
        ret.put("supplierAuthInserted", authInserted);
        ret.put("roleMenuInserted", rmInserted);
        ret.put("grantMenuCount", grantMenus.size());
        return ret;
    }
}
