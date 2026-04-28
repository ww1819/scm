package com.scm.system.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.scm.common.constant.ScmAuthConstants;
import com.scm.common.core.domain.entity.SysMenu;
import com.scm.common.exception.ServiceException;
import com.scm.system.domain.Hospital;
import com.scm.system.domain.ScmHospitalSupplierScopePair;
import com.scm.system.domain.Supplier;
import com.scm.system.mapper.ScmHospitalSupplierScopeMapper;
import com.scm.system.mapper.SysMenuMapper;
import com.scm.system.service.IHospitalService;
import com.scm.system.service.IScmHospitalContextService;
import com.scm.system.service.IScmHospitalSupplierMenuScopeService;
import com.scm.system.service.IScmHospitalSupplierPermissionService;
import com.scm.system.service.IScmSupplierContextService;
import com.scm.system.service.ISupplierService;

@Service
public class ScmHospitalSupplierMenuScopeServiceImpl implements IScmHospitalSupplierMenuScopeService
{
    @Autowired
    private SysMenuMapper sysMenuMapper;

    @Autowired
    private ScmHospitalSupplierScopeMapper scmHospitalSupplierScopeMapper;

    @Autowired
    private IScmHospitalContextService scmHospitalContextService;

    @Autowired
    private IScmSupplierContextService scmSupplierContextService;

    @Autowired
    private IScmHospitalSupplierPermissionService hospitalSupplierPermissionService;

    @Autowired
    private IHospitalService hospitalService;

    @Autowired
    private ISupplierService supplierService;

    @Override
    public List<ScmHospitalSupplierScopePair> listAuthorizedPairs(Long userId, Long menuId)
    {
        if (userId == null || menuId == null)
        {
            return Collections.emptyList();
        }
        SysMenu menu = requireHsMenu(menuId);
        if (!userHasMenu(userId, menuId))
        {
            return Collections.emptyList();
        }

        Long hospitalCtx = scmHospitalContextService.resolveHospitalIdForUser(userId);
        Long supplierCtx = scmSupplierContextService.resolveSupplierIdForUser(userId);
        boolean grant = "1".equals(menu.getHospitalGrantSupplierFlag());

        if (hospitalCtx != null)
        {
            if (grant)
            {
                return nullToEmpty(scmHospitalSupplierScopeMapper.selectPairsHospitalGrantMenu(hospitalCtx, menuId));
            }
            return nullToEmpty(scmHospitalSupplierScopeMapper.selectPairsHospitalGlobalSupplierMenu(hospitalCtx, menuId));
        }

        if (supplierCtx != null)
        {
            List<ScmHospitalSupplierScopePair> raw = grant
                ? scmHospitalSupplierScopeMapper.selectPairsSupplierGrantMenu(supplierCtx, menuId)
                : scmHospitalSupplierScopeMapper.selectPairsSupplierGlobalMenu(supplierCtx, menuId);
            List<Long> forbid = hospitalSupplierPermissionService.listForbidSubmitHospitalIds(supplierCtx);
            if (forbid == null || forbid.isEmpty())
            {
                return nullToEmpty(raw);
            }
            Set<Long> fb = new LinkedHashSet<>(forbid);
            return nullToEmpty(raw).stream()
                .filter(p -> p.getHospitalId() == null || !fb.contains(p.getHospitalId()))
                .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    @Override
    public List<Hospital> listHospitalsForDropdown(Long userId, Long menuId)
    {
        requireHsMenu(menuId);
        Long hospitalCtx = scmHospitalContextService.resolveHospitalIdForUser(userId);
        Long supplierCtx = scmSupplierContextService.resolveSupplierIdForUser(userId);

        if (hospitalCtx != null)
        {
            Hospital h = hospitalService.selectHospitalById(hospitalCtx);
            if (h == null || !"0".equals(h.getStatus()))
            {
                return Collections.emptyList();
            }
            if (!userHasMenu(userId, menuId))
            {
                return Collections.emptyList();
            }
            return Collections.singletonList(h);
        }

        if (supplierCtx != null)
        {
            List<ScmHospitalSupplierScopePair> pairs = listAuthorizedPairs(userId, menuId);
            if (pairs.isEmpty())
            {
                return Collections.emptyList();
            }
            Set<Long> ids = pairs.stream().map(ScmHospitalSupplierScopePair::getHospitalId).filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new));
            List<Hospital> list = new ArrayList<>();
            for (Long hid : ids)
            {
                Hospital h = hospitalService.selectHospitalById(hid);
                if (h != null && "0".equals(h.getStatus()))
                {
                    list.add(h);
                }
            }
            return list;
        }

        assertPlatformMenu(userId, menuId);
        Hospital q = new Hospital();
        q.setStatus("0");
        return hospitalService.selectHospitalList(q);
    }

    @Override
    public List<Supplier> listSuppliersForDropdown(Long userId, Long menuId)
    {
        requireHsMenu(menuId);
        Long hospitalCtx = scmHospitalContextService.resolveHospitalIdForUser(userId);
        Long supplierCtx = scmSupplierContextService.resolveSupplierIdForUser(userId);

        if (supplierCtx != null)
        {
            Supplier s = supplierService.selectSupplierById(supplierCtx);
            if (s == null || !"0".equals(s.getStatus()))
            {
                return Collections.emptyList();
            }
            if (!userHasMenu(userId, menuId))
            {
                return Collections.emptyList();
            }
            List<ScmHospitalSupplierScopePair> pairs = listAuthorizedPairs(userId, menuId);
            if (pairs.isEmpty())
            {
                return Collections.emptyList();
            }
            return Collections.singletonList(s);
        }

        if (hospitalCtx != null)
        {
            List<ScmHospitalSupplierScopePair> pairs = listAuthorizedPairs(userId, menuId);
            if (pairs.isEmpty())
            {
                return Collections.emptyList();
            }
            Set<Long> ids = pairs.stream().map(ScmHospitalSupplierScopePair::getSupplierId).filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new));
            List<Supplier> list = new ArrayList<>();
            for (Long sid : ids)
            {
                Supplier s = supplierService.selectSupplierById(sid);
                if (s != null && "0".equals(s.getStatus()))
                {
                    list.add(s);
                }
            }
            return list;
        }

        assertPlatformMenu(userId, menuId);
        Supplier q = new Supplier();
        q.setStatus("0");
        return supplierService.selectSupplierList(q);
    }

    @Override
    public void applyMenuPairScopeToParams(Map<String, Object> params, Long userId)
    {
        if (params == null || userId == null)
        {
            return;
        }
        Long menuId = parseLongObject(params.get("scopeMenuId"));
        if (menuId == null)
        {
            return;
        }
        Long hospitalCtx = scmHospitalContextService.resolveHospitalIdForUser(userId);
        Long supplierCtx = scmSupplierContextService.resolveSupplierIdForUser(userId);
        if (hospitalCtx == null && supplierCtx == null)
        {
            return;
        }
        List<ScmHospitalSupplierScopePair> pairs;
        try
        {
            pairs = listAuthorizedPairs(userId, menuId);
        }
        catch (ServiceException ex)
        {
            params.put("scopePairBlock", Boolean.TRUE);
            return;
        }
        if (pairs.isEmpty())
        {
            params.put("scopePairBlock", Boolean.TRUE);
            return;
        }
        List<Map<String, Object>> forMybatis = new ArrayList<>();
        for (ScmHospitalSupplierScopePair p : pairs)
        {
            if (p.getHospitalId() == null || p.getSupplierId() == null)
            {
                continue;
            }
            Map<String, Object> row = new HashMap<>();
            row.put("hospitalId", p.getHospitalId());
            row.put("supplierId", p.getSupplierId());
            forMybatis.add(row);
        }
        if (forMybatis.isEmpty())
        {
            params.put("scopePairBlock", Boolean.TRUE);
            return;
        }
        params.put("scopePairs", forMybatis);
    }

    private void assertPlatformMenu(Long userId, Long menuId)
    {
        if (!userHasMenu(userId, menuId))
        {
            throw new ServiceException("无该菜单权限");
        }
    }

    private SysMenu requireHsMenu(Long menuId)
    {
        SysMenu menu = sysMenuMapper.selectMenuById(menuId);
        if (menu == null || !ScmAuthConstants.AUTH_HOSPITAL_SUPPLIER.equals(menu.getAuthType()))
        {
            throw new ServiceException("菜单不存在或不是医院-供应商类型");
        }
        return menu;
    }

    private boolean userHasMenu(Long userId, Long menuId)
    {
        List<SysMenu> menus = sysMenuMapper.selectMenusByUserId(userId);
        if (menus == null)
        {
            return false;
        }
        for (SysMenu m : menus)
        {
            if (m != null && menuId.equals(m.getMenuId()))
            {
                return true;
            }
        }
        return false;
    }

    private static List<ScmHospitalSupplierScopePair> nullToEmpty(List<ScmHospitalSupplierScopePair> list)
    {
        return list != null ? list : Collections.emptyList();
    }

    private static Long parseLongObject(Object v)
    {
        if (v == null)
        {
            return null;
        }
        if (v instanceof Long)
        {
            return (Long) v;
        }
        if (v instanceof Number)
        {
            return ((Number) v).longValue();
        }
        try
        {
            return Long.parseLong(v.toString().trim());
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }
}
