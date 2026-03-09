package com.scm.web.controller.tenant;

import java.util.List;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.scm.common.annotation.Log;
import com.scm.common.core.controller.BaseController;
import com.scm.common.core.domain.AjaxResult;
import com.scm.common.core.page.TableDataInfo;
import com.scm.common.enums.BusinessType;
import com.scm.common.utils.poi.ExcelUtil;
import com.scm.common.utils.StringUtils;
import com.scm.common.core.domain.Ztree;
import com.scm.system.domain.ScmTenant;
import com.scm.system.domain.ScmTenantModifyLog;
import com.scm.system.domain.ScmTenantStatusLog;
import com.scm.system.domain.ScmTenantStatusPeriod;
import com.scm.system.service.IScmTenantService;
import com.scm.system.service.ISysMenuService;
import com.scm.system.service.IScmTenantMenuService;

/**
 * 客户管理（租户）
 */
@Controller
@RequestMapping("/tenant/tenant")
public class ScmTenantController extends BaseController
{
    private String prefix = "tenant/tenant";

    @Autowired
    private IScmTenantService scmTenantService;
    @Autowired
    private IScmTenantMenuService scmTenantMenuService;
    @Autowired
    private ISysMenuService menuService;

    @RequiresPermissions("tenant:tenant:view")
    @GetMapping()
    public String index()
    {
        return prefix + "/tenant";
    }

    @RequiresPermissions("tenant:tenant:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ScmTenant query)
    {
        startPage();
        List<ScmTenant> list = scmTenantService.selectScmTenantList(query);
        return getDataTable(list);
    }

    @RequiresPermissions("tenant:tenant:export")
    @Log(title = "客户管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(ScmTenant query)
    {
        List<ScmTenant> list = scmTenantService.selectScmTenantList(query);
        ExcelUtil<ScmTenant> util = new ExcelUtil<>(ScmTenant.class);
        return util.exportExcel(list, "客户数据");
    }

    @RequiresPermissions("tenant:tenant:add")
    @GetMapping("/add")
    public String add()
    {
        return prefix + "/add";
    }

    @RequiresPermissions("tenant:tenant:add")
    @Log(title = "客户管理", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(@Validated ScmTenant tenant)
    {
        if (!scmTenantService.checkTenantCodeUnique(tenant))
            return error("租户编码已存在");
        tenant.setCreateBy(getLoginName());
        return toAjax(scmTenantService.insertScmTenant(tenant, getLoginName()));
    }

    @RequiresPermissions("tenant:tenant:edit")
    @GetMapping("/edit/{tenantId}")
    public String edit(@PathVariable String tenantId, ModelMap mmap)
    {
        ScmTenant tenant = scmTenantService.selectScmTenantById(tenantId);
        mmap.put("tenant", tenant);
        return prefix + "/edit";
    }

    @RequiresPermissions("tenant:tenant:edit")
    @Log(title = "客户管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(@Validated ScmTenant tenant)
    {
        if (!scmTenantService.checkTenantCodeUnique(tenant))
            return error("租户编码已存在");
        tenant.setUpdateBy(getLoginName());
        return toAjax(scmTenantService.updateScmTenant(tenant, getLoginName()));
    }

    @RequiresPermissions("tenant:tenant:view")
    @GetMapping("/view/{tenantId}")
    public String view(@PathVariable String tenantId, ModelMap mmap)
    {
        ScmTenant tenant = scmTenantService.selectScmTenantById(tenantId);
        mmap.put("tenant", tenant);
        List<ScmTenantStatusPeriod> periods = scmTenantService.selectStatusPeriodsByTenantId(tenantId);
        List<ScmTenantStatusLog> statusLogs = scmTenantService.selectStatusLogsByTenantId(tenantId);
        List<ScmTenantModifyLog> modifyLogs = scmTenantService.selectModifyLogsByTenantId(tenantId);
        mmap.put("periods", periods);
        mmap.put("statusLogs", statusLogs);
        mmap.put("modifyLogs", modifyLogs);
        return prefix + "/view";
    }

    @RequiresPermissions("tenant:tenant:remove")
    @Log(title = "客户管理", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        return toAjax(scmTenantService.deleteScmTenantByIds(ids, getLoginName()));
    }

    /** 授权：分配功能菜单 */
    @RequiresPermissions("tenant:tenant:auth")
    @GetMapping("/authMenu/{tenantId}")
    public String authMenu(@PathVariable String tenantId, ModelMap mmap)
    {
        ScmTenant tenant = scmTenantService.selectScmTenantById(tenantId);
        mmap.put("tenant", tenant);
        return prefix + "/authMenu";
    }

    @RequiresPermissions("tenant:tenant:auth")
    @GetMapping("/authMenu/treeData/{tenantId}")
    @ResponseBody
    public List<Ztree> treeData(@PathVariable String tenantId)
    {
        List<Long> checkedIds = scmTenantMenuService.selectMenuIdsByTenantId(tenantId);
        return menuService.menuTreeDataWithChecked(getUserId(), checkedIds);
    }

    /** 保存授权菜单 */
    @RequiresPermissions("tenant:tenant:auth")
    @Log(title = "客户授权", businessType = BusinessType.GRANT)
    @PostMapping("/authMenu/save")
    @ResponseBody
    public AjaxResult saveAuthMenu(String tenantId, String menuIds)
    {
        if (StringUtils.isEmpty(tenantId)) return error("客户不能为空");
        Long[] ids = StringUtils.isEmpty(menuIds) ? new Long[0] : com.scm.common.core.text.Convert.toLongArray(menuIds);
        scmTenantMenuService.saveTenantMenus(tenantId, ids, getLoginName());
        return success();
    }

    /** 重置：检查并创建医院管理员角色与用户 */
    @RequiresPermissions("tenant:tenant:reset")
    @Log(title = "客户管理-重置", businessType = BusinessType.UPDATE)
    @PostMapping("/reset")
    @ResponseBody
    public AjaxResult reset(String tenantId)
    {
        if (StringUtils.isEmpty(tenantId)) return error("客户不能为空");
        scmTenantService.resetTenantAdmin(tenantId, getLoginName());
        return success();
    }
}
