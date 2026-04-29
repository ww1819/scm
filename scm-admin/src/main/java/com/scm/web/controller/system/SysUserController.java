package com.scm.web.controller.system;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import com.scm.common.annotation.Log;
import com.scm.common.core.controller.BaseController;
import com.scm.common.core.domain.AjaxResult;
import com.scm.common.core.domain.Ztree;
import com.scm.common.core.domain.entity.SysDept;
import com.scm.common.core.domain.entity.SysRole;
import com.scm.common.core.domain.entity.SysUser;
import com.scm.common.core.page.TableDataInfo;
import com.scm.common.core.text.Convert;
import com.scm.common.enums.BusinessType;
import com.scm.common.utils.DateUtils;
import com.scm.common.utils.ShiroUtils;
import com.scm.common.utils.StringUtils;
import com.scm.common.utils.poi.ExcelUtil;
import com.scm.framework.shiro.service.SysPasswordService;
import com.scm.framework.shiro.util.AuthorizationUtils;
import com.scm.system.domain.Hospital;
import com.scm.system.domain.HospitalUser;
import com.scm.system.domain.Supplier;
import com.scm.system.domain.SupplierUser;
import com.scm.system.mapper.HospitalUserMapper;
import com.scm.system.service.IHospitalService;
import com.scm.system.service.ISupplierService;
import com.scm.system.service.ISupplierUserService;
import com.scm.system.service.ISysDeptService;
import com.scm.system.service.ISysPostService;
import com.scm.system.service.ISysRoleService;
import com.scm.system.service.ISysUserService;

/**
 * 用户信息
 * 
 * @author scm
 */
@Controller
@RequestMapping("/system/user")
public class SysUserController extends BaseController
{
    private String prefix = "system/user";

    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysRoleService roleService;
    
    @Autowired
    private ISysDeptService deptService;

    @Autowired
    private ISysPostService postService;

    @Autowired
    private SysPasswordService passwordService;

    @Autowired
    private ISupplierService supplierService;

    @Autowired
    private ISupplierUserService supplierUserService;

    @Autowired
    private IHospitalService hospitalService;

    @Autowired
    private HospitalUserMapper hospitalUserMapper;

    @RequiresPermissions("system:user:view")
    @GetMapping()
    public String user()
    {
        return prefix + "/user";
    }

    @RequiresPermissions("system:user:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(SysUser user)
    {
        startPage();
        List<SysUser> list = userService.selectUserList(user);
        return getDataTable(list);
    }

    @Log(title = "用户管理", businessType = BusinessType.EXPORT)
    @RequiresPermissions("system:user:export")
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(SysUser user)
    {
        List<SysUser> list = userService.selectUserList(user);
        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
        return util.exportExcel(list, "用户数据");
    }

    @Log(title = "用户管理", businessType = BusinessType.IMPORT)
    @RequiresPermissions("system:user:import")
    @PostMapping("/importData")
    @ResponseBody
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception
    {
        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
        List<SysUser> userList = util.importExcel(file.getInputStream());
        String message = userService.importUser(userList, updateSupport, getLoginName());
        return AjaxResult.success(message);
    }

    @RequiresPermissions("system:user:view")
    @GetMapping("/importTemplate")
    @ResponseBody
    public AjaxResult importTemplate()
    {
        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
        return util.importTemplateExcel("用户数据");
    }

    /**
     * 新增用户
     */
    @RequiresPermissions("system:user:add")
    @GetMapping("/add")
    public String add(ModelMap mmap)
    {
        mmap.put("posts", postService.selectPostAll());
        Hospital hospitalQuery = new Hospital();
        hospitalQuery.setStatus("0");
        mmap.put("hospitals", hospitalService.selectHospitalList(hospitalQuery));
        return prefix + "/add";
    }

    /**
     * 新增保存用户
     */
    @RequiresPermissions("system:user:add")
    @Log(title = "用户管理", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(@Validated SysUser user)
    {
        deptService.checkDeptDataScope(user.getDeptId());
        roleService.checkRoleDataScope(user.getRoleIds());
        if (!userService.checkLoginNameUnique(user))
        {
            return error("新增用户'" + user.getLoginName() + "'失败，登录账号已存在");
        }
        else if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user))
        {
            return error("新增用户'" + user.getLoginName() + "'失败，手机号码已存在");
        }
        else if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user))
        {
            return error("新增用户'" + user.getLoginName() + "'失败，邮箱账号已存在");
        }
        user.setSalt(ShiroUtils.randomSalt());
        user.setPassword(passwordService.encryptPassword(user.getLoginName(), user.getPassword(), user.getSalt()));
        user.setPwdUpdateDate(DateUtils.getNowDate());
        user.setCreateBy(getLoginName());
        return toAjax(userService.insertUser(user));
    }

    /**
     * 修改用户
     */
    @RequiresPermissions("system:user:edit")
    @GetMapping("/edit/{userId}")
    public String edit(@PathVariable("userId") Long userId, ModelMap mmap)
    {
        userService.checkUserDataScope(userId);
        List<SysRole> roles = roleService.selectRolesByUserId(userId);
        SysUser user = userService.selectUserById(userId);
        // 超级管理员账号默认归属一级根组织「医承云配」（若库中无该名称则取第一个 parent_id=0 的根部门）
        if (SysUser.isAdmin(userId))
        {
            SysDept rootDept = deptService.selectPreferredRootDept();
            if (rootDept != null && rootDept.getDeptId() != null
                && (user.getDeptId() == null || !rootDept.getDeptId().equals(user.getDeptId())))
            {
                SysUser sync = new SysUser(userId);
                sync.setDeptId(rootDept.getDeptId());
                sync.setUpdateBy(getLoginName());
                userService.updateUserInfo(sync);
                user = userService.selectUserById(userId);
            }
        }
        mmap.put("user", user);
        List<SysRole> rolesForPage = SysUser.isAdmin(userId) ? roles : roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList());
        Long selectedRoleId = null;
        for (SysRole r : rolesForPage)
        {
            if (r.isFlag())
            {
                selectedRoleId = r.getRoleId();
                break;
            }
        }
        mmap.put("pickerInitialRoleId", selectedRoleId);
        mmap.put("pickerInitialRoleKey", "");
        mmap.put("pickerInitialRoleName", "");
        mmap.put("pickerInitialDisplay", "");
        mmap.put("pickerInitialRoleType", "");
        if (selectedRoleId != null)
        {
            SysRole probe = new SysRole();
            probe.setRoleId(selectedRoleId);
            List<SysRole> picked = roleService.selectRoleList(probe);
            if (picked != null && !picked.isEmpty())
            {
                SysRole sr = picked.get(0);
                mmap.put("pickerInitialRoleKey", StringUtils.nvl(sr.getRoleKey(), ""));
                mmap.put("pickerInitialRoleName", StringUtils.nvl(sr.getRoleName(), ""));
                mmap.put("pickerInitialRoleType", StringUtils.nvl(sr.getRoleType(), ""));
                mmap.put("pickerInitialDisplay", formatRolePickerDisplay(sr));
            }
            else
            {
                for (SysRole r : rolesForPage)
                {
                    if (selectedRoleId.equals(r.getRoleId()))
                    {
                        mmap.put("pickerInitialRoleKey", StringUtils.nvl(r.getRoleKey(), ""));
                        mmap.put("pickerInitialRoleName", StringUtils.nvl(r.getRoleName(), ""));
                        mmap.put("pickerInitialRoleType", StringUtils.nvl(r.getRoleType(), ""));
                        mmap.put("pickerInitialDisplay", formatRolePickerDisplay(r));
                        break;
                    }
                }
            }
        }
        mmap.put("posts", postService.selectPostsByUserId(userId));
        Hospital hospitalQuery = new Hospital();
        hospitalQuery.setStatus("0");
        mmap.put("hospitals", hospitalService.selectHospitalList(hospitalQuery));
        SupplierUser supplierUser = supplierUserService.selectSupplierUserByUserId(userId);
        if (supplierUser != null && supplierUser.getSupplierId() != null)
        {
            user.setMaintainSupplierId(supplierUser.getSupplierId());
            mmap.put("maintainSupplierLabel", StringUtils.nvl(supplierUser.getSupplierName(), ""));
        }
        else
        {
            mmap.put("maintainSupplierLabel", "");
        }
        HospitalUser hospitalUser = hospitalUserMapper.selectHospitalUserByUserId(userId);
        if (hospitalUser != null && hospitalUser.getHospitalId() != null)
        {
            user.setMaintainHospitalId(hospitalUser.getHospitalId());
        }
        return prefix + "/edit";
    }

    /**
     * 查询用户详细
     */
    @RequiresPermissions("system:user:list")
    @GetMapping("/view/{userId}")
    public String view(@PathVariable("userId") Long userId, ModelMap mmap)
    {
        userService.checkUserDataScope(userId);
        mmap.put("user", userService.selectUserById(userId));
        mmap.put("roleGroup", userService.selectUserRoleGroup(userId));
        mmap.put("postGroup", userService.selectUserPostGroup(userId));
        return prefix + "/view";
    }

    /**
     * 修改保存用户
     */
    @RequiresPermissions("system:user:edit")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(@Validated SysUser user)
    {
        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.getUserId());
        deptService.checkDeptDataScope(user.getDeptId());
        roleService.checkRoleDataScope(user.getRoleIds());
        if (!userService.checkLoginNameUnique(user))
        {
            return error("修改用户'" + user.getLoginName() + "'失败，登录账号已存在");
        }
        else if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user))
        {
            return error("修改用户'" + user.getLoginName() + "'失败，手机号码已存在");
        }
        else if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user))
        {
            return error("修改用户'" + user.getLoginName() + "'失败，邮箱账号已存在");
        }
        user.setUpdateBy(getLoginName());
        AuthorizationUtils.clearAllCachedAuthorizationInfo();
        return toAjax(userService.updateUser(user));
    }

    @RequiresPermissions("system:user:resetPwd")
    @GetMapping("/resetPwd/{userId}")
    public String resetPwd(@PathVariable("userId") Long userId, ModelMap mmap)
    {
        userService.checkUserDataScope(userId);
        mmap.put("user", userService.selectUserById(userId));
        return prefix + "/resetPwd";
    }

    @RequiresPermissions("system:user:resetPwd")
    @Log(title = "重置密码", businessType = BusinessType.UPDATE)
    @PostMapping("/resetPwd")
    @ResponseBody
    public AjaxResult resetPwdSave(SysUser user)
    {
        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.getUserId());
        user.setSalt(ShiroUtils.randomSalt());
        user.setPassword(passwordService.encryptPassword(user.getLoginName(), user.getPassword(), user.getSalt()));
        if (userService.resetUserPwd(user) > 0)
        {
            if (ShiroUtils.getUserId().longValue() == user.getUserId().longValue())
            {
                setSysUser(userService.selectUserById(user.getUserId()));
            }
            return success();
        }
        return error();
    }

    /**
     * 进入授权角色页
     */
    @RequiresPermissions("system:user:edit")
    @GetMapping("/authRole/{userId}")
    public String authRole(@PathVariable("userId") Long userId, ModelMap mmap)
    {
        userService.checkUserDataScope(userId);
        SysUser user = userService.selectUserById(userId);
        // 获取用户所属的角色列表
        List<SysRole> roles = roleService.selectRolesByUserId(userId);
        mmap.put("user", user);
        mmap.put("roles", SysUser.isAdmin(userId) ? roles : roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));
        return prefix + "/authRole";
    }

    /**
     * 用户授权角色
     */
    @RequiresPermissions("system:user:edit")
    @Log(title = "用户管理", businessType = BusinessType.GRANT)
    @PostMapping("/authRole/insertAuthRole")
    @ResponseBody
    public AjaxResult insertAuthRole(Long userId, Long[] roleIds)
    {
        userService.checkUserDataScope(userId);
        roleService.checkRoleDataScope(roleIds);
        userService.insertUserAuth(userId, roleIds);
        AuthorizationUtils.clearAllCachedAuthorizationInfo();
        return success();
    }

    @RequiresPermissions("system:user:remove")
    @Log(title = "用户管理", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        if (ArrayUtils.contains(Convert.toLongArray(ids), getUserId()))
        {
            return error("当前用户不能删除");
        }
        return toAjax(userService.deleteUserByIds(ids));
    }

    /**
     * 校验用户名
     */
    @PostMapping("/checkLoginNameUnique")
    @ResponseBody
    public boolean checkLoginNameUnique(SysUser user)
    {
        return userService.checkLoginNameUnique(user);
    }

    /**
     * 校验手机号码
     */
    @PostMapping("/checkPhoneUnique")
    @ResponseBody
    public boolean checkPhoneUnique(SysUser user)
    {
        return userService.checkPhoneUnique(user);
    }

    /**
     * 校验email邮箱
     */
    @PostMapping("/checkEmailUnique")
    @ResponseBody
    public boolean checkEmailUnique(SysUser user)
    {
        return userService.checkEmailUnique(user);
    }

    /**
     * 用户状态修改
     */
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @RequiresPermissions("system:user:edit")
    @PostMapping("/changeStatus")
    @ResponseBody
    public AjaxResult changeStatus(SysUser user)
    {
        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.getUserId());
        return toAjax(userService.changeStatus(user));
    }

    /**
     * 加载部门列表树
     */
    @RequiresPermissions("system:user:list")
    @GetMapping("/deptTreeData")
    @ResponseBody
    public List<Ztree> deptTreeData()
    {
        List<Ztree> ztrees = deptService.selectDeptTree(new SysDept());
        return ztrees;
    }

    /**
     * 选择部门树
     * 
     * @param deptId 部门ID
     */
    @RequiresPermissions("system:user:list")
    @GetMapping("/selectDeptTree/{deptId}")
    public String selectDeptTree(@PathVariable("deptId") Long deptId, ModelMap mmap)
    {
        mmap.put("dept", deptService.selectDeptById(deptId));
        return prefix + "/deptTree";
    }

    /**
     * 新增/编辑用户：弹窗内分页查询可选角色（含绑定医院、供应商；支持名称筛选）
     */
    @RequiresPermissions(value = { "system:user:add", "system:user:edit" }, logical = Logical.OR)
    @PostMapping("/rolePicker/list")
    @ResponseBody
    public TableDataInfo rolePickerList(SysRole role,
        @RequestParam(value = "pickerHospitalName", required = false) String pickerHospitalName,
        @RequestParam(value = "pickerSupplierName", required = false) String pickerSupplierName)
    {
        role.setStatus("0");
        role.getParams().put("excludeAdminRole", "1");
        if (StringUtils.isNotEmpty(pickerHospitalName))
        {
            role.getParams().put("hospitalName", pickerHospitalName.trim());
        }
        if (StringUtils.isNotEmpty(pickerSupplierName))
        {
            role.getParams().put("supplierCompanyName", pickerSupplierName.trim());
        }
        startPage();
        List<SysRole> list = roleService.selectRoleList(role);
        return getDataTable(list);
    }

    private static String formatRolePickerDisplay(SysRole r)
    {
        if (r == null)
        {
            return "";
        }
        String hn = StringUtils.isNotEmpty(r.getHospitalName()) ? r.getHospitalName() : "—";
        String sn = StringUtils.isNotEmpty(r.getSupplierCompanyName()) ? r.getSupplierCompanyName() : "—";
        String type = roleTypeDisplayLabel(r.getRoleType());
        String oa = "1".equals(StringUtils.trimToEmpty(r.getOrgAdmin())) ? "是" : "否";
        return r.getRoleName() + "（类型：" + type + "，医院：" + hn + "，供应商：" + sn + "，机构管理员：" + oa + "）";
    }

    private static String roleTypeDisplayLabel(String roleType)
    {
        if (StringUtils.isEmpty(roleType))
        {
            return "未标注";
        }
        if ("platform".equalsIgnoreCase(roleType))
        {
            return "平台";
        }
        if ("hospital".equalsIgnoreCase(roleType))
        {
            return "医院";
        }
        if ("supplier".equalsIgnoreCase(roleType))
        {
            return "供应商";
        }
        return roleType;
    }

    /**
     * 维护供应商：按关键字检索（用于用户新增/修改页输入联想）
     */
    @RequiresPermissions(value = { "system:user:add", "system:user:edit" }, logical = Logical.OR)
    @GetMapping("/maintainSupplierOptions")
    @ResponseBody
    public AjaxResult maintainSupplierOptions(@RequestParam(value = "keyword", required = false) String keyword)
    {
        Supplier q = new Supplier();
        q.setDelFlag("0");
        if (StringUtils.isNotEmpty(keyword))
        {
            q.setCompanyName(keyword);
        }
        List<Supplier> list = supplierService.selectSupplierList(q);
        List<Map<String, Object>> options = new ArrayList<>();
        int n = Math.min(list.size(), 50);
        for (int i = 0; i < n; i++)
        {
            Supplier s = list.get(i);
            Map<String, Object> row = new HashMap<>(2);
            row.put("id", s.getSupplierId());
            row.put("text", s.getCompanyName());
            options.add(row);
        }
        return AjaxResult.success(options);
    }
}