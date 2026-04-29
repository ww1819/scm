package com.scm.web.controller.scm.auth;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.scm.common.annotation.Log;
import com.scm.common.constant.ScmAuthConstants;
import com.scm.common.core.controller.BaseController;
import com.scm.common.core.domain.AjaxResult;
import com.scm.common.core.domain.Ztree;
import com.scm.common.core.domain.entity.SysMenu;
import com.scm.common.enums.BusinessType;
import com.scm.common.exception.ServiceException;
import com.scm.common.utils.DateUtils;
import com.scm.common.utils.uuid.IdUtils;
import com.scm.common.utils.StringUtils;
import com.scm.system.domain.Hospital;
import com.scm.system.domain.HospitalSupplier;
import com.scm.system.domain.ScmSupplierMenuAuth;
import com.scm.system.domain.Supplier;
import com.scm.system.mapper.ScmHospitalMenuAuthMapper;
import com.scm.system.mapper.ScmSupplierMenuAuthMapper;
import com.scm.system.mapper.SysMenuMapper;
import com.scm.system.service.IHospitalService;
import com.scm.system.service.IHospitalSupplierService;
import com.scm.system.service.IScmHospitalContextService;
import com.scm.system.service.IScmScopeBootstrapService;
import com.scm.system.service.ISupplierService;
import com.scm.system.service.ISysMenuService;

/**
 * 医院授予供应商菜单授权（仅 auth_type=hospital_supplier 且 hospital_grant_supplier_flag=1）
 */
@Controller
@RequestMapping("/scm/auth/hospitalSupplierMenu")
public class ScmHospitalSupplierMenuAuthController extends BaseController
{
    private static final String PREFIX = "scm/auth/hospital_supplier_menu";

    @Autowired
    private IHospitalService hospitalService;
    @Autowired
    private ISupplierService supplierService;
    @Autowired
    private IHospitalSupplierService hospitalSupplierService;
    @Autowired
    private IScmHospitalContextService scmHospitalContextService;
    @Autowired
    private SysMenuMapper sysMenuMapper;
    @Autowired
    private ScmHospitalMenuAuthMapper hospitalMenuAuthMapper;
    @Autowired
    private ScmSupplierMenuAuthMapper supplierMenuAuthMapper;
    @Autowired
    private IScmScopeBootstrapService scmScopeBootstrapService;
    @Autowired
    private ISysMenuService sysMenuService;

    @RequiresPermissions("scmAuth:hospitalSupplierMenu:view")
    @GetMapping()
    public String page()
    {
        return PREFIX;
    }

    /**
     * 弹窗选择医院：编码、名称、拼音简码（受医院上下文限制时仅返回本院）
     */
    @RequiresPermissions("scmAuth:hospitalSupplierMenu:view")
    @GetMapping("/candidates")
    @ResponseBody
    public AjaxResult hospitalCandidates()
    {
        Long hospitalCtx = scmHospitalContextService.resolveHospitalIdForUser(getUserId());
        Hospital q = new Hospital();
        q.setStatus("0");
        if (hospitalCtx != null)
        {
            q.setHospitalId(hospitalCtx);
        }
        List<Hospital> list = hospitalService.selectHospitalList(q);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Hospital h : list)
        {
            Map<String, Object> m = new HashMap<>();
            m.put("id", h.getHospitalId());
            m.put("code", StringUtils.nvl(h.getHospitalCode(), ""));
            m.put("name", StringUtils.nvl(h.getHospitalName(), ""));
            m.put("pinyin", StringUtils.nvl(h.getPinyinCode(), ""));
            rows.add(m);
        }
        return AjaxResult.success(rows);
    }

    /**
     * 弹窗选择供应商：当前医院已关联且启用的供应商，编码、名称、拼音简码
     */
    @RequiresPermissions("scmAuth:hospitalSupplierMenu:view")
    @GetMapping("/supplierCandidates/{hospitalId}")
    @ResponseBody
    public AjaxResult supplierCandidates(@PathVariable("hospitalId") Long hospitalId)
    {
        assertHospitalInScope(hospitalId);
        HospitalSupplier q = new HospitalSupplier();
        q.setHospitalId(hospitalId);
        List<HospitalSupplier> rels = hospitalSupplierService.selectHospitalSupplierList(q);
        Set<Long> ids = new HashSet<>();
        List<Map<String, Object>> rows = new ArrayList<>();
        for (HospitalSupplier rel : rels)
        {
            if (rel.getSupplierId() != null && ids.add(rel.getSupplierId()))
            {
                Supplier s = supplierService.selectSupplierById(rel.getSupplierId());
                if (s != null && "0".equals(s.getStatus()))
                {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", s.getSupplierId());
                    m.put("code", StringUtils.nvl(s.getSupplierCode(), ""));
                    m.put("name", StringUtils.nvl(s.getCompanyName(), ""));
                    String py = StringUtils.isNotEmpty(s.getCompanyShortName()) ? s.getCompanyShortName()
                        : StringUtils.nvl(s.getPinyinCode(), "");
                    m.put("pinyin", py);
                    rows.add(m);
                }
            }
        }
        return AjaxResult.success(rows);
    }

    @RequiresPermissions("scmAuth:hospitalSupplierMenu:query")
    @GetMapping("/suppliers/{hospitalId}")
    @ResponseBody
    public AjaxResult suppliers(@PathVariable("hospitalId") Long hospitalId)
    {
        assertHospitalInScope(hospitalId);
        HospitalSupplier q = new HospitalSupplier();
        q.setHospitalId(hospitalId);
        List<HospitalSupplier> rels = hospitalSupplierService.selectHospitalSupplierList(q);
        Set<Long> ids = new HashSet<>();
        List<Supplier> list = new ArrayList<>();
        for (HospitalSupplier rel : rels)
        {
            if (rel.getSupplierId() != null && ids.add(rel.getSupplierId()))
            {
                Supplier s = supplierService.selectSupplierById(rel.getSupplierId());
                if (s != null && "0".equals(s.getStatus()))
                {
                    list.add(s);
                }
            }
        }
        return AjaxResult.success(list);
    }

    @RequiresPermissions("scmAuth:hospitalSupplierMenu:query")
    @GetMapping("/menuTree/{hospitalId}/{supplierId}")
    @ResponseBody
    public List<Ztree> menuTree(@PathVariable("hospitalId") Long hospitalId, @PathVariable("supplierId") Long supplierId)
    {
        assertHospitalInScope(hospitalId);
        assertSupplierUnderHospital(hospitalId, supplierId);

        Set<Long> scope = scmScopeBootstrapService.listAllScopeMenuIds(ScmAuthConstants.AUTH_HOSPITAL_SUPPLIER);
        List<Long> hospitalOwned = hospitalMenuAuthMapper.selectMenuIdsByHospitalId(hospitalId);
        Set<Long> allowedSet = new HashSet<>(scope);
        allowedSet.retainAll(new HashSet<>(hospitalOwned));
        List<SysMenu> all = sysMenuMapper.selectMenuAll();
        List<SysMenu> filtered = all.stream().filter(m -> allowedSet.contains(m.getMenuId())).collect(Collectors.toList());

        List<Long> checked = supplierMenuAuthMapper.selectMenuIdsBySupplierAndHospital(supplierId, hospitalId);
        return sysMenuService.buildMenuTreeWithChecked(filtered, checked);
    }

    @RequiresPermissions("scmAuth:hospitalSupplierMenu:edit")
    @Log(title = "医院授予供应商菜单", businessType = BusinessType.UPDATE)
    @PostMapping("/save")
    @ResponseBody
    public AjaxResult save(@RequestParam("hospitalId") Long hospitalId, @RequestParam("supplierId") Long supplierId,
        @RequestParam(value = "menuIds", required = false) String menuIds)
    {
        assertHospitalInScope(hospitalId);
        assertSupplierUnderHospital(hospitalId, supplierId);

        Long[] ids = StringUtils.isEmpty(menuIds) ? new Long[0] : com.scm.common.core.text.Convert.toLongArray(menuIds);
        Set<Long> selected = new HashSet<>();
        for (Long id : ids)
        {
            if (id != null)
            {
                selected.add(id);
            }
        }

        Set<Long> scope = scmScopeBootstrapService.listAllScopeMenuIds(ScmAuthConstants.AUTH_HOSPITAL_SUPPLIER);
        List<Long> hospitalOwned = hospitalMenuAuthMapper.selectMenuIdsByHospitalId(hospitalId);
        Set<Long> allowed = new HashSet<>(scope);
        allowed.retainAll(new HashSet<>(hospitalOwned));
        selected.retainAll(allowed);

        supplierMenuAuthMapper.deleteBySupplierAndHospital(supplierId, hospitalId);
        if (!selected.isEmpty())
        {
            Date now = DateUtils.getNowDate();
            List<ScmSupplierMenuAuth> rows = new ArrayList<>();
            for (Long menuId : selected)
            {
                ScmSupplierMenuAuth row = new ScmSupplierMenuAuth();
                row.setId(IdUtils.simpleUuid7());
                row.setSupplierId(supplierId);
                row.setHospitalId(hospitalId);
                row.setMenuId(menuId);
                row.setCreateBy(getLoginName());
                row.setCreateTime(now);
                rows.add(row);
            }
            supplierMenuAuthMapper.batchInsert(rows);
        }
        return AjaxResult.success();
    }

    private void assertHospitalInScope(Long hospitalId)
    {
        Long ctx = scmHospitalContextService.resolveHospitalIdForUser(getUserId());
        if (ctx != null && !ctx.equals(hospitalId))
        {
            throw new ServiceException("仅允许操作当前医院数据");
        }
    }

    private void assertSupplierUnderHospital(Long hospitalId, Long supplierId)
    {
        HospitalSupplier q = new HospitalSupplier();
        q.setHospitalId(hospitalId);
        q.setSupplierId(supplierId);
        List<HospitalSupplier> rels = hospitalSupplierService.selectHospitalSupplierList(q);
        if (rels == null || rels.isEmpty())
        {
            throw new ServiceException("供应商不在该医院名下");
        }
    }
}
