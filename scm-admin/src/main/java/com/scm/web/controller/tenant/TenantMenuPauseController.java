package com.scm.web.controller.tenant;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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
import com.scm.system.domain.ScmTenantMenuPauseLogVo;
import com.scm.system.domain.ScmTenantMenuPauseManageVo;
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

    /** 客户菜单功能管理入口页（下拉选择客户后列出菜单权限与暂停记录） */
    @RequiresPermissions("tenant:menuPause:view")
    @GetMapping()
    public String select()
    {
        return prefix + "/select";
    }

    /** 客户下拉选项（用于菜单功能管理页选择客户） */
    @RequiresPermissions("tenant:menuPause:view")
    @GetMapping("/tenantOptions")
    @ResponseBody
    public AjaxResult tenantOptions()
    {
        List<ScmTenant> list = scmTenantService.selectScmTenantList(new ScmTenant());
        List<Map<String, String>> options = list.stream().map(t -> {
            Map<String, String> m = new HashMap<>(2);
            m.put("tenantId", t.getTenantId());
            m.put("tenantName", t.getTenantName() != null ? t.getTenantName() : t.getTenantId());
            return m;
        }).collect(Collectors.toList());
        return success(options);
    }

    /** 带客户ID 时重定向到选择页并预选该客户（便于从客户维护「菜单功能」进入） */
    @RequiresPermissions("tenant:menuPause:view")
    @GetMapping("/index/{tenantId}")
    public String index(@PathVariable String tenantId)
    {
        return "redirect:/tenant/menuPause?tenantId=" + com.scm.common.utils.ServletUtils.urlEncode(tenantId);
    }

    /** 客户菜单功能管理：列出该客户所有菜单权限及暂停状态 */
    @RequiresPermissions("tenant:menuPause:list")
    @PostMapping("/list/{tenantId}")
    @ResponseBody
    public TableDataInfo list(@PathVariable String tenantId)
    {
        List<ScmTenantMenuPauseManageVo> list = menuPauseService.listMenusWithStatusByTenantId(tenantId);
        return getDataTable(list);
    }

    /** 客户菜单暂停记录（含菜单名称） */
    @RequiresPermissions("tenant:menuPause:list")
    @PostMapping("/logList/{tenantId}")
    @ResponseBody
    public TableDataInfo logList(@PathVariable String tenantId)
    {
        startPage();
        List<ScmTenantMenuPauseLogVo> list = menuPauseService.listPauseLogsWithMenuNameByTenantId(tenantId);
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
