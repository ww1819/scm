package com.scm.web.controller.supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.scm.common.core.controller.BaseController;
import com.scm.common.core.domain.AjaxResult;
import com.scm.common.core.domain.entity.SysUser;
import com.scm.common.utils.DateUtils;
import com.scm.common.utils.StringUtils;
import com.scm.common.utils.ShiroUtils;
import com.scm.framework.shiro.service.SysPasswordService;
import com.scm.system.domain.Supplier;
import com.scm.system.service.ISupplierService;
import com.scm.system.service.ISysConfigService;
import com.scm.system.service.ISysUserService;

/**
 * 供应商注册验证
 * 
 * @author scm
 */
@Controller
@RequestMapping("/supplier/register")
public class SupplierRegisterController extends BaseController
{
    @Autowired
    private ISysConfigService configService;

    @Autowired
    private ISupplierService supplierService;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private SysPasswordService passwordService;

    @GetMapping()
    public String register()
    {
        return "supplier/register";
    }

    /**
     * 供应商注册
     */
    @PostMapping()
    @ResponseBody
    @Transactional
    public AjaxResult ajaxRegister(SysUser user, Supplier supplier)
    {
        if (!("true".equals(configService.selectConfigByKey("sys.account.registerUser"))))
        {
            return error("当前系统没有开启注册功能！");
        }

        // 验证用户信息
        String loginName = user.getLoginName();
        String password = user.getPassword();
        
        if (StringUtils.isEmpty(loginName))
        {
            return error("用户名不能为空");
        }
        if (StringUtils.isEmpty(password))
        {
            return error("用户密码不能为空");
        }
        
        // 密码验证：数字+字母，至少6位
        if (password.length() < 6)
        {
            return error("密码长度至少6位");
        }
        boolean hasDigit = false;
        boolean hasLetter = false;
        for (char c : password.toCharArray())
        {
            if (Character.isDigit(c))
            {
                hasDigit = true;
            }
            if (Character.isLetter(c))
            {
                hasLetter = true;
            }
        }
        if (!hasDigit || !hasLetter)
        {
            return error("密码必须包含数字和字母");
        }

        // 验证供应商信息
        if (StringUtils.isEmpty(supplier.getCompanyName()))
        {
            return error("公司名称不能为空");
        }

        // 检查用户名是否已存在
        if (!userService.checkLoginNameUnique(user))
        {
            return error("保存用户'" + loginName + "'失败，注册账号已存在");
        }

        try
        {
            // 创建用户
            user.setPwdUpdateDate(DateUtils.getNowDate());
            user.setUserName(loginName);
            user.setSalt(ShiroUtils.randomSalt());
            user.setPassword(passwordService.encryptPassword(loginName, password, user.getSalt()));
            user.setUserType("01"); // 注册用户
            user.setStatus("0"); // 待审核
            
            boolean regFlag = userService.registerUser(user);
            if (!regFlag)
            {
                return error("注册失败,请联系系统管理人员");
            }

            // 创建供应商信息
            supplier.setStatus("0"); // 待审核
            supplier.setAuditStatus("0"); // 待审核
            supplier.setCreateBy(loginName);
            supplier.setCreateTime(DateUtils.getNowDate());
            
            int supplierResult = supplierService.insertSupplier(supplier);
            if (supplierResult <= 0)
            {
                return error("供应商信息保存失败");
            }

            // 关联供应商和用户（这里需要创建scm_supplier_user表记录）
            // 暂时先返回成功，后续可以完善关联逻辑

            return success("注册成功，请等待管理员审核");
        }
        catch (Exception e)
        {
            logger.error("供应商注册失败", e);
            return error("注册失败：" + e.getMessage());
        }
    }
}

