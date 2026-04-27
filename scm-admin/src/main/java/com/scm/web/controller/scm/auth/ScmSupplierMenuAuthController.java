package com.scm.web.controller.scm.auth;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
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
import com.scm.system.domain.Supplier;
import com.scm.system.mapper.ScmSupplierMenuAuthMapper;
import com.scm.system.mapper.SysMenuMapper;
import com.scm.system.service.IScmScopeBootstrapService;
import com.scm.system.service.ISupplierService;
import com.scm.system.service.ISysMenuService;

/**
 * 供应商菜单授权（白名单）
 */
@Controller
@RequestMapping("/scm/auth/supplierMenu")
public class ScmSupplierMenuAuthController extends BaseController
{
    private static final String PREFIX = "scm/auth/supplier_menu";

    @Autowired
    private ISupplierService supplierService;
    @Autowired
    private SysMenuMapper sysMenuMapper;
    @Autowired
    private ScmSupplierMenuAuthMapper supplierMenuAuthMapper;
    @Autowired
    private IScmScopeBootstrapService scmScopeBootstrapService;
    @Autowired
    private ISysMenuService sysMenuService;

    @RequiresPermissions("scmAuth:supplierMenu:view")
    @GetMapping()
    public String page(ModelMap mmap)
    {
        Supplier q = new Supplier();
        q.setStatus("0");
        mmap.put("suppliers", supplierService.selectSupplierList(q));
        return PREFIX;
    }

    @RequiresPermissions("scmAuth:supplierMenu:query")
    @GetMapping("/menuTree/{supplierId}")
    @ResponseBody
    public List<Ztree> menuTree(@PathVariable("supplierId") Long supplierId)
    {
        Set<Long> scope = scmScopeBootstrapService.listAllScopeMenuIds(ScmAuthConstants.AUTH_SUPPLIER);
        List<SysMenu> all = sysMenuMapper.selectMenuAll();
        List<SysMenu> filtered = all.stream().filter(m -> scope.contains(m.getMenuId())).collect(Collectors.toList());
        List<Long> checked = supplierMenuAuthMapper.selectMenuIdsBySupplierId(supplierId);
        return sysMenuService.buildMenuTreeWithChecked(filtered, checked);
    }

    @RequiresPermissions("scmAuth:supplierMenu:edit")
    @Log(title = "供应商菜单授权", businessType = BusinessType.UPDATE)
    @PostMapping("/save")
    @ResponseBody
    public AjaxResult save(@RequestParam("supplierId") Long supplierId, @RequestParam(value = "menuIds", required = false) String menuIds)
    {
        Long[] arr = com.scm.common.utils.StringUtils.isEmpty(menuIds) ? new Long[0]
            : com.scm.common.core.text.Convert.toLongArray(menuIds);
        List<Long> ids = new ArrayList<>();
        for (Long id : arr)
        {
            if (id != null)
            {
                ids.add(id);
            }
        }
        scmScopeBootstrapService.replaceSupplierMenuAuth(supplierId, ids, getLoginName());
        return AjaxResult.success();
    }

    @RequiresPermissions("scmAuth:supplierMenu:reset")
    @Log(title = "供应商菜单授权重置", businessType = BusinessType.UPDATE)
    @PostMapping("/reset")
    @ResponseBody
    public AjaxResult reset(@RequestParam("supplierId") Long supplierId)
    {
        scmScopeBootstrapService.resetSupplierMenuAuth(supplierId, getLoginName());
        return AjaxResult.success();
    }
}
