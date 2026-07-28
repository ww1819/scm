package com.scm.web.controller.supplier;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.scm.common.annotation.Log;
import com.scm.common.constant.ScmAuthConstants;
import com.scm.common.constant.UserConstants;
import com.scm.common.core.controller.BaseController;
import com.scm.common.core.domain.AjaxResult;
import com.scm.common.core.domain.entity.SysRole;
import com.scm.common.core.domain.entity.SysUser;
import com.scm.common.core.page.TableDataInfo;
import com.scm.common.core.text.Convert;
import com.scm.common.enums.BusinessType;
import com.scm.common.utils.DateUtils;
import com.scm.common.utils.ShiroUtils;
import com.scm.common.utils.StringUtils;
import com.scm.common.utils.poi.ExcelUtil;
import com.scm.common.utils.security.Md5Utils;
import com.scm.system.domain.HospitalSupplier;
import com.scm.system.domain.Supplier;
import com.scm.system.domain.SupplierUser;
import com.scm.system.mapper.SysRoleMapper;
import com.scm.system.service.IHospitalSupplierService;
import com.scm.system.service.ISupplierService;
import com.scm.system.service.ISupplierUserService;
import com.scm.system.service.ISysConfigService;
import com.scm.system.service.ISysUserService;

/**
 * 企业用户维护
 * 
 * @author scm
 */
@Controller
@RequestMapping("/supplier/user")
public class SupplierUserController extends BaseController
{
    private String prefix = "supplier";

    @Autowired
    private ISupplierUserService supplierUserService;

    @Autowired
    private ISupplierService supplierService;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private IHospitalSupplierService hospitalSupplierService;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private SysRoleMapper roleMapper;

    @RequiresPermissions("supplier:user:view")
    @GetMapping()
    public String user()
    {
        return prefix + "/user";
    }

    /**
     * 查询供应商用户列表
     */
    @RequiresPermissions("supplier:user:view")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(SupplierUser supplierUser)
    {
        applyCurrentUserSupplierScope(supplierUser);
        startPage();
        List<SupplierUser> list = supplierUserService.selectSupplierUserList(supplierUser);
        return getDataTable(list);
    }

    /**
     * 导出供应商用户列表
     */
    @RequiresPermissions("supplier:user:export")
    @Log(title = "企业用户管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(SupplierUser supplierUser, HttpServletResponse response)
    {
        applyCurrentUserSupplierScope(supplierUser);
        List<SupplierUser> list = supplierUserService.selectSupplierUserList(supplierUser);
        ExcelUtil<SupplierUser> util = new ExcelUtil<SupplierUser>(SupplierUser.class);
        util.exportExcel(response, list, "企业用户数据");
    }

    /**
     * 新增供应商用户
     */
    @RequiresPermissions("supplier:user:add")
    @GetMapping("/add")
    public String add(ModelMap mmap)
    {
        Long currentSupplierId = getCurrentLoginSupplierId();
        if (currentSupplierId != null)
        {
            // 供应商账号：默认当前所属供应商，页面只读展示
            mmap.put("currentSupplier", supplierService.selectSupplierById(currentSupplierId));
        }
        else
        {
            // 平台账号：可选供应商列表
            mmap.put("supplierList", supplierService.selectSupplierList(new Supplier()));
        }
        return prefix + "/user/add";
    }

    /**
     * 新增保存供应商用户
     */
    @RequiresPermissions("supplier:user:add")
    @Log(title = "企业用户管理", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(@Validated SupplierUser supplierUser, @RequestParam("hospitalIds") String hospitalIds)
    {
        Long currentSupplierId = getCurrentLoginSupplierId();
        if (currentSupplierId != null)
        {
            supplierUser.setSupplierId(currentSupplierId);
        }
        if (supplierUser.getSupplierId() == null)
        {
            return error("未识别到供应商信息，无法新增企业用户");
        }
        if (StringUtils.isEmpty(supplierUser.getLoginName()))
        {
            return error("登录名称不能为空");
        }
        if (StringUtils.isEmpty(hospitalIds))
        {
            return error("请至少选择一家医院");
        }

        Long[] hospitalIdArr = Convert.toLongArray(hospitalIds);
        if (hospitalIdArr == null || hospitalIdArr.length == 0)
        {
            return error("请至少选择一家医院");
        }
        String hospitalCheck = validateSupplierHospitals(supplierUser.getSupplierId(), hospitalIdArr);
        if (hospitalCheck != null)
        {
            return error(hospitalCheck);
        }

        // 新增默认非主账号（主账号给其他账号授权）
        if (StringUtils.isEmpty(supplierUser.getIsMain()))
        {
            supplierUser.setIsMain("0");
        }

        // 如果设置为主账号，需要检查该供应商是否已有主账号
        if ("1".equals(supplierUser.getIsMain()))
        {
            List<SupplierUser> mainUsers = supplierUserService.selectSupplierUserListBySupplierId(supplierUser.getSupplierId());
            for (SupplierUser su : mainUsers)
            {
                if ("1".equals(su.getIsMain()))
                {
                    return error("该供应商已存在主账号，请先取消现有主账号");
                }
            }
        }

        SysUser checkUser = new SysUser();
        checkUser.setLoginName(supplierUser.getLoginName().trim());
        if (!userService.checkLoginNameUnique(checkUser))
        {
            return error("登录名称已存在");
        }

        String initPassword = configService.selectConfigByKey("sys.user.initPassword");
        if (StringUtils.isEmpty(initPassword))
        {
            initPassword = "123456";
        }
        SysRole salesRole = roleMapper.selectGlobalScmRoleByKey(ScmAuthConstants.ROLE_KEY_SUPPLIER_SALES);
        if (salesRole == null)
        {
            return error("全局供应商业务员角色未初始化");
        }

        SysUser newUser = new SysUser();
        newUser.setLoginName(supplierUser.getLoginName().trim());
        newUser.setUserName(StringUtils.isNotEmpty(supplierUser.getUserName()) ? supplierUser.getUserName().trim()
            : newUser.getLoginName());
        newUser.setPhonenumber(supplierUser.getPhonenumber());
        newUser.setEmail(supplierUser.getEmail());
        newUser.setSex(supplierUser.getSex());
        newUser.setIdCard(supplierUser.getIdCard());
        newUser.setUserType(UserConstants.REGISTER_USER_TYPE);
        newUser.setStatus("0");
        newUser.setSalt(ShiroUtils.randomSalt());
        newUser.setPassword(Md5Utils.hash(newUser.getLoginName() + initPassword + newUser.getSalt()));
        newUser.setPwdPlain(initPassword);
        newUser.setCreateBy(getLoginName());
        if (!userService.registerUser(newUser) || newUser.getUserId() == null)
        {
            return error("创建用户失败");
        }

        supplierUser.setUserId(newUser.getUserId());
        supplierUser.setCreateBy(getLoginName());
        supplierUser.setCreateTime(DateUtils.getNowDate());
        if (supplierUser.getStatus() == null)
        {
            supplierUser.setStatus("0");
        }
        int rows = supplierUserService.insertSupplierUser(supplierUser);
        if (rows > 0)
        {
            // 先绑定供应商，再授权业务员角色（insertUserAuth 会校验供应商绑定）
            userService.insertUserAuth(newUser.getUserId(), new Long[] { salesRole.getRoleId() });
        }
        return toAjax(rows);
    }

    /**
     * 修改供应商用户
     */
    @RequiresPermissions("supplier:user:edit")
    @GetMapping("/edit/{supplierUserId}")
    public String edit(@PathVariable("supplierUserId") Long supplierUserId, ModelMap mmap)
    {
        SupplierUser supplierUser = supplierUserService.selectSupplierUserById(supplierUserId);
        if (!canAccessSupplierUser(supplierUser))
        {
            return prefix + "/user";
        }
        mmap.put("supplierUser", supplierUser);
        return prefix + "/user/edit";
    }

    /**
     * 修改保存供应商用户
     */
    @RequiresPermissions("supplier:user:edit")
    @Log(title = "企业用户管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(@Validated SupplierUser supplierUser)
    {
        SupplierUser dbSupplierUser = supplierUserService.selectSupplierUserById(supplierUser.getSupplierUserId());
        if (!canAccessSupplierUser(dbSupplierUser))
        {
            return error("无权限操作该企业用户");
        }
        // 供应商不可修改，始终以库中关联为准
        supplierUser.setSupplierId(dbSupplierUser.getSupplierId());
        supplierUser.setUserId(dbSupplierUser.getUserId());

        // 如果设置为主账号，需要检查该供应商是否已有其他主账号
        if ("1".equals(supplierUser.getIsMain()))
        {
            List<SupplierUser> mainUsers = supplierUserService.selectSupplierUserListBySupplierId(supplierUser.getSupplierId());
            for (SupplierUser su : mainUsers)
            {
                if ("1".equals(su.getIsMain()) && !su.getSupplierUserId().equals(supplierUser.getSupplierUserId()))
                {
                    return error("该供应商已存在主账号，请先取消现有主账号");
                }
            }
        }

        supplierUser.setUpdateBy(getLoginName());
        supplierUser.setUpdateTime(DateUtils.getNowDate());
        int rows = supplierUserService.updateSupplierUser(supplierUser);
        if (rows > 0 && dbSupplierUser.getUserId() != null)
        {
            SysUser sysUser = new SysUser();
            sysUser.setUserId(dbSupplierUser.getUserId());
            sysUser.setUserName(supplierUser.getUserName());
            sysUser.setPhonenumber(supplierUser.getPhonenumber());
            sysUser.setEmail(supplierUser.getEmail());
            sysUser.setSex(supplierUser.getSex());
            sysUser.setIdCard(supplierUser.getIdCard());
            userService.updateUserInfo(sysUser);
        }
        return toAjax(rows);
    }

    /**
     * 删除供应商用户
     */
    @RequiresPermissions("supplier:user:remove")
    @Log(title = "企业用户管理", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        Long currentSupplierId = getCurrentLoginSupplierId();
        if (currentSupplierId != null && StringUtils.isNotEmpty(ids))
        {
            String[] idArray = Convert.toStrArray(ids);
            for (String idStr : idArray)
            {
                SupplierUser dbSupplierUser = supplierUserService.selectSupplierUserById(Long.valueOf(idStr));
                if (!canAccessSupplierUser(dbSupplierUser))
                {
                    return error("包含无权限删除的企业用户数据");
                }
            }
        }
        return toAjax(supplierUserService.deleteSupplierUserByIds(ids));
    }

    /**
     * 选择医院页面（仅展示与供应商有配送关系的医院，支持多选）
     */
    @RequiresPermissions("supplier:user:add")
    @GetMapping("/selectHospital")
    public String selectHospital(@RequestParam("supplierId") Long supplierId, ModelMap mmap)
    {
        Long currentSupplierId = getCurrentLoginSupplierId();
        if (currentSupplierId != null)
        {
            supplierId = currentSupplierId;
        }
        mmap.put("supplierId", supplierId);
        return prefix + "/user/selectHospital";
    }

    /**
     * 查询供应商已有配送关系的医院列表
     */
    @RequiresPermissions("supplier:user:add")
    @PostMapping("/selectHospitalList")
    @ResponseBody
    public TableDataInfo selectHospitalList(HospitalSupplier query)
    {
        Long currentSupplierId = getCurrentLoginSupplierId();
        if (currentSupplierId != null)
        {
            query.setSupplierId(currentSupplierId);
        }
        if (query.getSupplierId() == null)
        {
            return getDataTable(Collections.emptyList());
        }
        // 已审核通过、启用中的配送/供货关联
        query.setStatus("0");
        query.setAuditStatus("1");
        query.setDisableStatus("0");
        startPage();
        List<HospitalSupplier> list = hospitalSupplierService.selectHospitalSupplierList(query);
        return getDataTable(list);
    }

    /**
     * 将供应商用户查询范围收敛到当前登录账号所属供应商。
     */
    private void applyCurrentUserSupplierScope(SupplierUser supplierUser)
    {
        if (supplierUser == null)
        {
            return;
        }
        Long currentSupplierId = getCurrentLoginSupplierId();
        if (currentSupplierId != null)
        {
            supplierUser.setSupplierId(currentSupplierId);
        }
    }

    /**
     * 获取当前登录用户所属供应商ID（优先主账号关系，兜底任意关联关系）。
     */
    private Long getCurrentLoginSupplierId()
    {
        Long userId = getUserId();
        Long supplierId = supplierUserService.getManagedSupplierId(userId);
        if (supplierId != null)
        {
            return supplierId;
        }
        SupplierUser supplierUser = supplierUserService.selectSupplierUserByUserId(userId);
        return supplierUser != null ? supplierUser.getSupplierId() : null;
    }

    /**
     * 校验当前登录账号是否可访问指定企业用户数据。
     */
    private boolean canAccessSupplierUser(SupplierUser supplierUser)
    {
        if (supplierUser == null)
        {
            return false;
        }
        Long currentSupplierId = getCurrentLoginSupplierId();
        if (currentSupplierId == null)
        {
            return true;
        }
        return currentSupplierId.equals(supplierUser.getSupplierId());
    }

    /**
     * 校验所选医院是否均与供应商存在有效配送关系。
     */
    private String validateSupplierHospitals(Long supplierId, Long[] hospitalIds)
    {
        HospitalSupplier q = new HospitalSupplier();
        q.setSupplierId(supplierId);
        q.setStatus("0");
        q.setAuditStatus("1");
        q.setDisableStatus("0");
        List<HospitalSupplier> linked = hospitalSupplierService.selectHospitalSupplierList(q);
        Set<Long> allowed = new HashSet<>();
        if (linked != null)
        {
            for (HospitalSupplier hs : linked)
            {
                if (hs.getHospitalId() != null)
                {
                    allowed.add(hs.getHospitalId());
                }
            }
        }
        for (Long hospitalId : hospitalIds)
        {
            if (hospitalId == null || !allowed.contains(hospitalId))
            {
                return "所选医院中存在与当前供应商无配送关系的医院";
            }
        }
        return null;
    }
}
