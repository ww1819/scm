package com.scm.web.controller.supplier;

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
import com.scm.common.core.domain.entity.SysUser;
import com.scm.common.core.page.TableDataInfo;
import com.scm.common.core.text.Convert;
import com.scm.common.enums.BusinessType;
import com.scm.common.utils.DateUtils;
import com.scm.common.utils.StringUtils;
import com.scm.common.utils.poi.ExcelUtil;
import com.scm.system.domain.Supplier;
import com.scm.system.domain.SupplierUser;
import com.scm.system.service.ISupplierService;
import com.scm.system.service.ISupplierUserService;
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
        // 供应商账号仅允许维护自身供应商用户
        Supplier query = new Supplier();
        Long currentSupplierId = getCurrentLoginSupplierId();
        if (currentSupplierId != null)
        {
            query.setSupplierId(currentSupplierId);
        }
        List<Supplier> supplierList = supplierService.selectSupplierList(query);
        mmap.put("supplierList", supplierList);
        return prefix + "/user/add";
    }

    /**
     * 新增保存供应商用户
     */
    @RequiresPermissions("supplier:user:add")
    @Log(title = "企业用户管理", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(@Validated SupplierUser supplierUser)
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

        // 检查该用户是否已经关联到其他供应商
        SupplierUser existUser = supplierUserService.selectSupplierUserByUserId(supplierUser.getUserId());
        if (existUser != null)
        {
            return error("该用户已经关联到其他供应商，无法重复关联");
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

        supplierUser.setCreateBy(getLoginName());
        supplierUser.setCreateTime(DateUtils.getNowDate());
        if (supplierUser.getStatus() == null)
        {
            supplierUser.setStatus("0");
        }
        return toAjax(supplierUserService.insertSupplierUser(supplierUser));
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
        // 供应商账号仅允许维护自身供应商用户
        Supplier query = new Supplier();
        Long currentSupplierId = getCurrentLoginSupplierId();
        if (currentSupplierId != null)
        {
            query.setSupplierId(currentSupplierId);
        }
        List<Supplier> supplierList = supplierService.selectSupplierList(query);
        mmap.put("supplierList", supplierList);
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
        Long currentSupplierId = getCurrentLoginSupplierId();
        if (currentSupplierId != null)
        {
            supplierUser.setSupplierId(currentSupplierId);
        }

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
        return toAjax(supplierUserService.updateSupplierUser(supplierUser));
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
     * 选择用户页面
     */
    @RequiresPermissions("supplier:user:add")
    @GetMapping("/selectUser")
    public String selectUser()
    {
        return prefix + "/user/selectUser";
    }

    /**
     * 查询未关联的用户列表（用于选择用户）
     */
    @RequiresPermissions("supplier:user:add")
    @PostMapping("/selectUserList")
    @ResponseBody
    public TableDataInfo selectUserList(SysUser user)
    {
        startPage();
        // 查询所有正常状态的用户
        user.setStatus("0");
        List<SysUser> list = userService.selectUserList(user);
        // 过滤掉已经关联到供应商的用户
        List<SupplierUser> supplierUsers = supplierUserService.selectSupplierUserList(new SupplierUser());
        for (SupplierUser su : supplierUsers)
        {
            list.removeIf(u -> u.getUserId().equals(su.getUserId()));
        }
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
}

