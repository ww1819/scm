package com.scm.web.controller.tenant;

import java.util.List;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
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
import com.scm.common.core.domain.Ztree;
import com.scm.system.domain.ScmTenant;
import com.scm.system.domain.ScmTenantMenuPause;
import com.scm.system.domain.ScmTenantMenuPauseLog;
import com.scm.system.service.IScmTenantMenuPauseService;
import com.scm.system.service.IScmTenantService;
import com.scm.system.service.ISysMenuService;

/**
 * 客户菜单功能管理（暂停/恢复及记录）
 */
@Controller
@RequestMapping("/tenant/menuPause")
public class TenantMenuPauseController extends BaseController
{
    private String prefix = "tenant/menuPause";

    @Autowired
    private IScmTenantMenuPauseService menuPauseService;
    @Autowired
    private IScmTenantService scmTenantService;
    @Autowired
    private ISysMenuService menuService;

    @RequiresPermissions("tenant:menuPause:view")
    @GetMapping("/index/{tenantId}")
    public String index(@PathVariable String tenantId, ModelMap mmap)
    {
        ScmTenant tenant = scmTenantService.selectScmTenantById(tenantId);
        mmap.put("tenant", tenant);
        return prefix + "/index";
    }

    @RequiresPermissions("tenant:menuPause:list")
    @PostMapping("/list/{tenantId}")
    @ResponseBody
    public TableDataInfo list(@PathVariable String tenantId)
    {
        startPage();
        List<ScmTenantMenuPause> list = menuPauseService.selectByTenantId(tenantId);
        return getDataTable(list);
    }

    @RequiresPermissions("tenant:menuPause:list")
    @PostMapping("/logList/{tenantId}")
    @ResponseBody
    public TableDataInfo logList(@PathVariable String tenantId)
    {
        startPage();
        List<ScmTenantMenuPauseLog> list = menuPauseService.selectPauseLogsByTenantId(tenantId);
        return getDataTable(list);
    }

    @RequiresPermissions("tenant:menuPause:edit")
    @Log(title = "客户菜单暂停", businessType = BusinessType.UPDATE)
    @PostMapping("/pause")
    @ResponseBody
    public AjaxResult pause(String tenantId, Long menuId, String remark)
    {
        if (tenantId == null || menuId == null) return error("参数缺失");
        return toAjax(menuPauseService.pauseMenu(tenantId, menuId, getLoginName(), remark));
    }

    @RequiresPermissions("tenant:menuPause:edit")
    @Log(title = "客户菜单恢复", businessType = BusinessType.UPDATE)
    @PostMapping("/resume")
    @ResponseBody
    public AjaxResult resume(String tenantId, Long menuId, String remark)
    {
        if (tenantId == null || menuId == null) return error("参数缺失");
        return toAjax(menuPauseService.resumeMenu(tenantId, menuId, getLoginName(), remark));
    }

    @RequiresPermissions("tenant:menuPause:view")
    @GetMapping("/menuTree")
    @ResponseBody
    public List<Ztree> menuTree()
    {
        return menuService.menuTreeData(getUserId());
    }
}
