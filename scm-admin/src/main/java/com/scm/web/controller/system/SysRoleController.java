package com.scm.web.controller.system;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
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
import com.scm.common.core.domain.Ztree;
import com.scm.common.core.domain.entity.SysRole;
import com.scm.common.core.domain.entity.SysUser;
import com.scm.common.core.page.TableDataInfo;
import com.scm.common.enums.BusinessType;
import com.scm.common.utils.StringUtils;
import com.scm.common.utils.poi.ExcelUtil;
import com.scm.framework.shiro.util.AuthorizationUtils;
import com.scm.system.domain.Hospital;
import com.scm.system.domain.Supplier;
import com.scm.system.domain.SysUserRole;
import com.scm.system.service.IHospitalService;
import com.scm.system.service.ISupplierService;
import com.scm.system.service.ISysDeptService;
import com.scm.system.service.ISysRoleService;
import com.scm.system.service.ISysUserService;

/**
 * 角色信息
 * 
 * @author scm
 */
@Controller
@RequestMapping("/system/role")
public class SysRoleController extends BaseController
{
    private String prefix = "system/role";

    @Autowired
    private ISysRoleService roleService;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysDeptService deptService;

    @Autowired
    private IHospitalService hospitalService;

    @Autowired
    private ISupplierService supplierService;

    @RequiresPermissions("system:role:view")
    @GetMapping()
    public String role()
    {
        return prefix + "/role";
    }

    @RequiresPermissions("system:role:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(SysRole role)
    {
        startPage();
        List<SysRole> list = roleService.selectRoleList(role);
        return getDataTable(list);
    }

    @Log(title = "角色管理", businessType = BusinessType.EXPORT)
    @RequiresPermissions("system:role:export")
    @PostMapping("/export")
    public void export(SysRole role, HttpServletResponse response)
    {
        List<SysRole> list = roleService.selectRoleList(role);
        ExcelUtil<SysRole> util = new ExcelUtil<SysRole>(SysRole.class);
        util.exportExcel(response, list, "角色数据");
    }

    /**
     * 新增角色
     */
    @RequiresPermissions("system:role:add")
    @GetMapping("/add")
    public String add(ModelMap mmap)
    {
        Hospital hq = new Hospital();
        hq.setStatus("0");
        mmap.put("hospitals", hospitalService.selectHospitalList(hq));
        Supplier sq = new Supplier();
        sq.setStatus("0");
        mmap.put("suppliers", supplierService.selectSupplierList(sq));
        return prefix + "/add";
    }

    /**
     * 新增保存角色
     */
    @RequiresPermissions("system:role:add")
    @Log(title = "角色管理", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(@Validated SysRole role)
    {
        if (!roleService.checkRoleNameUnique(role))
        {
            return error("新增角色'" + role.getRoleName() + "'失败，角色名称已存在");
        }
        else if (!roleService.checkRoleKeyUnique(role))
        {
            return error("新增角色'" + role.getRoleName() + "'失败，角色权限已存在");
        }
        role.setCreateBy(getLoginName());
        AuthorizationUtils.clearAllCachedAuthorizationInfo();
        return toAjax(roleService.insertRole(role));

    }

    /**
     * 修改角色
     */
    @RequiresPermissions("system:role:edit")
    @GetMapping("/edit/{roleId}")
    public String edit(@PathVariable("roleId") Long roleId, ModelMap mmap)
    {
        roleService.checkRoleDataScope(roleId);
        SysRole role = roleService.selectRoleById(roleId);
        normalizeRoleTypeForEditForm(role);
        mmap.put("role", role);
        mmap.put("hospitals", loadHospitalsForRoleForm(role != null ? role.getHospitalId() : null));
        mmap.put("suppliers", loadSuppliersForRoleForm(role != null ? role.getSupplierId() : null));
        return prefix + "/edit";
    }

    /**
     * 修改保存角色
     */
    @RequiresPermissions("system:role:edit")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(@Validated SysRole role)
    {
        roleService.checkRoleAllowed(role);
        roleService.checkRoleDataScope(role.getRoleId());
        if (!roleService.checkRoleNameUnique(role))
        {
            return error("修改角色'" + role.getRoleName() + "'失败，角色名称已存在");
        }
        else if (!roleService.checkRoleKeyUnique(role))
        {
            return error("修改角色'" + role.getRoleName() + "'失败，角色权限已存在");
        }
        role.setUpdateBy(getLoginName());
        AuthorizationUtils.clearAllCachedAuthorizationInfo();
        return toAjax(roleService.updateRole(role));
    }

    /**
     * 角色分配数据权限
     */
    @GetMapping("/authDataScope/{roleId}")
    public String authDataScope(@PathVariable("roleId") Long roleId, ModelMap mmap)
    {
        roleService.checkRoleDataScope(roleId);
        mmap.put("role", roleService.selectRoleById(roleId));
        return prefix + "/dataScope";
    }

    /**
     * 保存角色分配数据权限
     */
    @RequiresPermissions("system:role:edit")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PostMapping("/authDataScope")
    @ResponseBody
    public AjaxResult authDataScopeSave(SysRole role)
    {
        roleService.checkRoleAllowed(role);
        roleService.checkRoleDataScope(role.getRoleId());
        role.setUpdateBy(getLoginName());
        if (roleService.authDataScope(role) > 0)
        {
            setSysUser(userService.selectUserById(getUserId()));
            return success();
        }
        return error();
    }

    @RequiresPermissions("system:role:remove")
    @Log(title = "角色管理", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        return toAjax(roleService.deleteRoleByIds(ids));
    }

    /**
     * 校验角色名称
     */
    @PostMapping("/checkRoleNameUnique")
    @ResponseBody
    public boolean checkRoleNameUnique(SysRole role)
    {
        return roleService.checkRoleNameUnique(role);
    }

    /**
     * 校验角色权限
     */
    @PostMapping("/checkRoleKeyUnique")
    @ResponseBody
    public boolean checkRoleKeyUnique(SysRole role)
    {
        return roleService.checkRoleKeyUnique(role);
    }

    /**
     * 选择菜单树
     */
    @GetMapping("/selectMenuTree")
    public String selectMenuTree()
    {
        return prefix + "/tree";
    }

    /**
     * 角色状态修改
     */
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @RequiresPermissions("system:role:edit")
    @PostMapping("/changeStatus")
    @ResponseBody
    public AjaxResult changeStatus(SysRole role)
    {
        roleService.checkRoleAllowed(role);
        roleService.checkRoleDataScope(role.getRoleId());
        return toAjax(roleService.changeStatus(role));
    }

    /**
     * 分配用户
     */
    @RequiresPermissions("system:role:edit")
    @GetMapping("/authUser/{roleId}")
    public String authUser(@PathVariable("roleId") Long roleId, ModelMap mmap)
    {
        roleService.checkRoleDataScope(roleId);
        mmap.put("role", roleService.selectRoleById(roleId));
        return prefix + "/authUser";
    }

    /**
     * 查询已分配用户角色列表
     */
    @RequiresPermissions("system:role:list")
    @PostMapping("/authUser/allocatedList")
    @ResponseBody
    public TableDataInfo allocatedList(SysUser user)
    {
        startPage();
        List<SysUser> list = userService.selectAllocatedList(user);
        return getDataTable(list);
    }

    /**
     * 取消授权
     */
    @RequiresPermissions("system:role:edit")
    @Log(title = "角色管理", businessType = BusinessType.GRANT)
    @PostMapping("/authUser/cancel")
    @ResponseBody
    public AjaxResult cancelAuthUser(SysUserRole userRole)
    {
        return toAjax(roleService.deleteAuthUser(userRole));
    }

    /**
     * 批量取消授权
     */
    @RequiresPermissions("system:role:edit")
    @Log(title = "角色管理", businessType = BusinessType.GRANT)
    @PostMapping("/authUser/cancelAll")
    @ResponseBody
    public AjaxResult cancelAuthUserAll(Long roleId, String userIds)
    {
        return toAjax(roleService.deleteAuthUsers(roleId, userIds));
    }

    /**
     * 选择用户
     */
    @RequiresPermissions("system:role:list")
    @GetMapping("/authUser/selectUser/{roleId}")
    public String selectUser(@PathVariable("roleId") Long roleId, ModelMap mmap)
    {
        mmap.put("role", roleService.selectRoleById(roleId));
        return prefix + "/selectUser";
    }

    /**
     * 查询未分配用户角色列表
     */
    @RequiresPermissions("system:role:list")
    @PostMapping("/authUser/unallocatedList")
    @ResponseBody
    public TableDataInfo unallocatedList(SysUser user)
    {
        startPage();
        List<SysUser> list = userService.selectUnallocatedList(user);
        return getDataTable(list);
    }

    /**
     * 批量选择用户授权
     */
    @RequiresPermissions("system:role:edit")
    @Log(title = "角色管理", businessType = BusinessType.GRANT)
    @PostMapping("/authUser/selectAll")
    @ResponseBody
    public AjaxResult selectAuthUserAll(Long roleId, String userIds)
    {
        roleService.checkRoleDataScope(roleId);
        return toAjax(roleService.insertAuthUsers(roleId, userIds));
    }

    /**
     * 加载角色部门（数据权限）列表树
     */
    @RequiresPermissions("system:role:edit")
    @GetMapping("/deptTreeData")
    @ResponseBody
    public List<Ztree> deptTreeData(SysRole role)
    {
        List<Ztree> ztrees = deptService.roleDeptTreeData(role);
        return ztrees;
    }

    /** 历史数据 role_type 为空时，按已绑定机构推断类型，保证编辑页下拉与回显一致 */
    private static void normalizeRoleTypeForEditForm(SysRole role)
    {
        if (role == null || StringUtils.isNotEmpty(role.getRoleType()))
        {
            return;
        }
        if (role.getSupplierId() != null && role.getSupplierId() > 0)
        {
            role.setRoleType("supplier");
        }
        else if (role.getHospitalId() != null && role.getHospitalId() > 0)
        {
            role.setRoleType("hospital");
        }
    }

    /**
     * 角色表单医院下拉：在营医院 + 当前角色已绑定医院（避免编辑时选项缺失导致无法回显）
     */
    private List<Hospital> loadHospitalsForRoleForm(Long boundHospitalId)
    {
        Hospital hq = new Hospital();
        hq.setStatus("0");
        List<Hospital> list = new ArrayList<>(hospitalService.selectHospitalList(hq));
        appendBoundHospitalIfMissing(list, boundHospitalId);
        return list;
    }

    /**
     * 角色表单供应商下拉：在营供应商 + 当前角色已绑定供应商（避免编辑时选项缺失导致无法回显）
     */
    private List<Supplier> loadSuppliersForRoleForm(Long boundSupplierId)
    {
        Supplier sq = new Supplier();
        sq.setStatus("0");
        List<Supplier> list = new ArrayList<>(supplierService.selectSupplierList(sq));
        appendBoundSupplierIfMissing(list, boundSupplierId);
        return list;
    }

    private void appendBoundHospitalIfMissing(List<Hospital> list, Long boundHospitalId)
    {
        if (boundHospitalId == null || boundHospitalId <= 0 || list == null)
        {
            return;
        }
        for (Hospital h : list)
        {
            if (h != null && boundHospitalId.equals(h.getHospitalId()))
            {
                return;
            }
        }
        Hospital bound = hospitalService.selectHospitalById(boundHospitalId);
        if (bound != null)
        {
            list.add(0, bound);
        }
    }

    private void appendBoundSupplierIfMissing(List<Supplier> list, Long boundSupplierId)
    {
        if (boundSupplierId == null || boundSupplierId <= 0 || list == null)
        {
            return;
        }
        for (Supplier s : list)
        {
            if (s != null && boundSupplierId.equals(s.getSupplierId()))
            {
                return;
            }
        }
        Supplier bound = supplierService.selectSupplierById(boundSupplierId);
        if (bound != null)
        {
            list.add(0, bound);
        }
    }
}