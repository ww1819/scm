package com.scm.web.controller.supplier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.scm.common.core.controller.BaseController;
import com.scm.common.core.domain.AjaxResult;
import com.scm.common.core.domain.entity.SysUser;
import com.scm.common.utils.StringUtils;
import com.scm.system.domain.Supplier;
import com.scm.system.service.ISupplierRegisterService;
import com.scm.system.service.ISupplierService;
import com.scm.system.service.ISysConfigService;

/**
 * 供应商注册：供应商 / 业务员 两种注册入口
 */
@Controller
@RequestMapping("/supplier/register")
public class SupplierRegisterController extends BaseController {

    @Autowired
    private ISysConfigService configService;
    @Autowired
    private ISupplierService supplierService;
    @Autowired
    private ISupplierRegisterService supplierRegisterService;

    @GetMapping()
    public String register() {
        return "supplier/register";
    }

    /** 注册页下拉用：获取供应商列表（id、公司名称） */
    @GetMapping("/listSuppliers")
    @ResponseBody
    public AjaxResult listSuppliers() {
        if (!("true".equals(configService.selectConfigByKey("sys.account.registerUser")))) {
            return error("当前系统未开启注册功能");
        }
        Supplier q = new Supplier();
        q.setDelFlag("0");
        List<Supplier> list = supplierService.selectSupplierList(q);
        List<Map<String, Object>> rows = list.stream().map(s -> {
            Map<String, Object> m = new HashMap<>();
            m.put("supplierId", s.getSupplierId());
            m.put("companyName", s.getCompanyName());
            return m;
        }).collect(Collectors.toList());
        return success(rows);
    }

    /**
     * 供应商注册：填写供应商信息 + 用户信息，校验公司不重复后创建供应商、两角色及管理员用户
     */
    @PostMapping("/supplier")
    @ResponseBody
    public AjaxResult registerSupplier(SysUser user, Supplier supplier) {
        if (!("true".equals(configService.selectConfigByKey("sys.account.registerUser")))) {
            return error("当前系统没有开启注册功能！");
        }
        if (StringUtils.isEmpty(user.getLoginName())) {
            return error("登录账号不能为空");
        }
        if (StringUtils.isEmpty(user.getPassword())) {
            return error("密码不能为空");
        }
        if (user.getPassword().length() < 6) {
            return error("密码长度至少6位");
        }
        boolean hasDigit = false, hasLetter = false;
        for (char c : user.getPassword().toCharArray()) {
            if (Character.isDigit(c)) hasDigit = true;
            if (Character.isLetter(c)) hasLetter = true;
        }
        if (!hasDigit || !hasLetter) {
            return error("密码必须包含数字和字母");
        }
        if (StringUtils.isEmpty(supplier.getCompanyName())) {
            return error("公司名称不能为空");
        }
        try {
            Long supplierId = supplierRegisterService.registerSupplier(supplier, user, null);
            return AjaxResult.success("注册成功", supplierId);
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            logger.error("供应商注册失败", e);
            return error("注册失败：" + e.getMessage());
        }
    }

    /**
     * 业务员注册：选择供应商 + 填写用户信息，生成待审核申请
     */
    @PostMapping("/salesperson")
    @ResponseBody
    public AjaxResult registerSalesperson(@RequestParam Long supplierId, SysUser user) {
        if (!("true".equals(configService.selectConfigByKey("sys.account.registerUser")))) {
            return error("当前系统没有开启注册功能！");
        }
        if (StringUtils.isEmpty(user.getLoginName())) {
            return error("登录账号不能为空");
        }
        if (StringUtils.isEmpty(user.getPassword())) {
            return error("密码不能为空");
        }
        if (user.getPassword().length() < 6) {
            return error("密码长度至少6位");
        }
        boolean hasDigit = false, hasLetter = false;
        for (char c : user.getPassword().toCharArray()) {
            if (Character.isDigit(c)) hasDigit = true;
            if (Character.isLetter(c)) hasLetter = true;
        }
        if (!hasDigit || !hasLetter) {
            return error("密码必须包含数字和字母");
        }
        try {
            supplierRegisterService.registerSalesperson(supplierId, user, null);
            return success("注册成功！待管理员审核授权。");
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            logger.error("业务员注册失败", e);
            return error("注册失败：" + e.getMessage());
        }
    }
}

