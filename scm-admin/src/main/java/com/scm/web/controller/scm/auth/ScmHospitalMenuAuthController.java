package com.scm.web.controller.scm.auth;

import java.util.ArrayList;
import java.util.HashMap;
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
import com.scm.common.utils.StringUtils;
import com.scm.common.core.domain.entity.SysMenu;
import com.scm.common.enums.BusinessType;
import com.scm.system.domain.Hospital;
import com.scm.system.mapper.ScmHospitalMenuAuthMapper;
import com.scm.system.mapper.SysMenuMapper;
import com.scm.framework.shiro.util.AuthorizationUtils;
import com.scm.system.service.IHospitalService;
import com.scm.system.service.IScmScopeBootstrapService;
import com.scm.system.service.ISysMenuService;

/**
 * 医院菜单授权（白名单）
 */
@Controller
@RequestMapping("/scm/auth/hospitalMenu")
public class ScmHospitalMenuAuthController extends BaseController
{
    private static final String PREFIX = "scm/auth/hospital_menu";

    @Autowired
    private IHospitalService hospitalService;
    @Autowired
    private SysMenuMapper sysMenuMapper;
    @Autowired
    private ScmHospitalMenuAuthMapper hospitalMenuAuthMapper;
    @Autowired
    private IScmScopeBootstrapService scmScopeBootstrapService;
    @Autowired
    private ISysMenuService sysMenuService;

    @RequiresPermissions("scmAuth:hospitalMenu:view")
    @GetMapping()
    public String page()
    {
        return PREFIX;
    }

    /**
     * 医院菜单授权页：弹窗选择用候选列表（编码、名称、拼音简码）
     */
    @RequiresPermissions("scmAuth:hospitalMenu:view")
    @GetMapping("/candidates")
    @ResponseBody
    public AjaxResult hospitalCandidates()
    {
        Hospital q = new Hospital();
        q.setStatus("0");
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

    @RequiresPermissions("scmAuth:hospitalMenu:query")
    @GetMapping("/menuTree/{hospitalId}")
    @ResponseBody
    public List<Ztree> menuTree(@PathVariable("hospitalId") Long hospitalId)
    {
        Set<Long> scope = scmScopeBootstrapService.listAllScopeMenuIds(ScmAuthConstants.AUTH_HOSPITAL);
        List<SysMenu> all = sysMenuMapper.selectMenuAll();
        List<SysMenu> filtered = all.stream().filter(m -> scope.contains(m.getMenuId())).collect(Collectors.toList());
        List<Long> checked = hospitalMenuAuthMapper.selectMenuIdsByHospitalId(hospitalId);
        return sysMenuService.buildMenuTreeWithChecked(filtered, checked);
    }

    @RequiresPermissions("scmAuth:hospitalMenu:edit")
    @Log(title = "医院菜单授权", businessType = BusinessType.UPDATE)
    @PostMapping("/save")
    @ResponseBody
    public AjaxResult save(@RequestParam("hospitalId") Long hospitalId, @RequestParam(value = "menuIds", required = false) String menuIds)
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
        scmScopeBootstrapService.replaceHospitalMenuAuth(hospitalId, ids, getLoginName());
        AuthorizationUtils.clearAllCachedAuthorizationInfo();
        return AjaxResult.success();
    }

    @RequiresPermissions("scmAuth:hospitalMenu:reset")
    @Log(title = "医院菜单授权重置", businessType = BusinessType.UPDATE)
    @PostMapping("/reset")
    @ResponseBody
    public AjaxResult reset(@RequestParam("hospitalId") Long hospitalId)
    {
        scmScopeBootstrapService.resetHospitalMenuAuth(hospitalId, getLoginName());
        AuthorizationUtils.clearAllCachedAuthorizationInfo();
        return AjaxResult.success();
    }

    /** 重置本院白名单 + 医院管理员/医院职工 两角色菜单（不动其他自定义医院角色） */
    @RequiresPermissions("scmAuth:hospitalMenu:reset")
    @Log(title = "医院菜单权限内置角色重置", businessType = BusinessType.UPDATE)
    @PostMapping("/resetBuiltinRoles")
    @ResponseBody
    public AjaxResult resetBuiltinRoles(@RequestParam("hospitalId") Long hospitalId)
    {
        scmScopeBootstrapService.resetHospitalBuiltinRoleMenus(hospitalId, getLoginName());
        AuthorizationUtils.clearAllCachedAuthorizationInfo();
        return AjaxResult.success();
    }
}
