package com.scm.web.controller.supplier;

import java.beans.PropertyEditorSupport;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.scm.common.core.controller.BaseController;
import com.scm.common.core.domain.AjaxResult;
import com.scm.common.core.domain.entity.SysDept;
import com.scm.common.core.domain.entity.SysUser;
import com.scm.common.utils.LoginNameUtils;
import com.scm.common.utils.PinyinUtils;
import com.scm.common.utils.StringUtils;
import com.scm.system.domain.Supplier;
import com.scm.system.service.ISupplierRegisterService;
import com.scm.system.service.ISupplierService;
import com.scm.system.service.ISysConfigService;
import com.scm.system.service.ISysDeptService;

/**
 * 供应商注册：供应商 / 业务员 两种注册入口
 */
@Controller
@RequestMapping("/supplier/register")
public class SupplierRegisterController extends BaseController {

    /** 供应商/业务员注册页登录账号最大长度（与页面 maxlength 一致） */
    private static final int SUPPLIER_REGISTER_LOGIN_NAME_MAX = 30;

    @Autowired
    private ISysConfigService configService;
    @Autowired
    private ISupplierService supplierService;
    @Autowired
    private ISupplierRegisterService supplierRegisterService;
    @Autowired
    private ISysDeptService deptService;

    /**
     * 注册资金：空串按 null；去除千分位逗号；非法数字给出明确绑定错误，避免默认类型转换异常信息难懂
     */
    @InitBinder("supplier")
    public void initSupplierBinder(WebDataBinder binder) {
        binder.registerCustomEditor(BigDecimal.class, "registeredCapital", new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                if (text == null || text.trim().isEmpty()) {
                    setValue(null);
                    return;
                }
                String s = text.trim().replace(",", "").replace("，", "");
                try {
                    BigDecimal bd = new BigDecimal(s);
                    if (bd.compareTo(BigDecimal.ZERO) < 0) {
                        throw new IllegalArgumentException("注册资金不能为负数");
                    }
                    setValue(bd);
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("注册资金格式不正确，请填写数字（万元）");
                }
            }
        });
    }

    @GetMapping()
    public String register() {
        return "supplier/register";
    }

    /**
     * 根据供应商名称生成拼音首字母简码（如「河北」→ HB），供注册页自动填充公司简码
     */
    @GetMapping("/companyShortCode")
    @ResponseBody
    public AjaxResult companyShortCode(@RequestParam(value = "name", required = false) String name) {
        if (!("true".equals(configService.selectConfigByKey("sys.account.registerUser")))) {
            return error("当前系统未开启注册功能");
        }
        if (StringUtils.isEmpty(name)) {
            return AjaxResult.success("操作成功", "");
        }
        String raw = PinyinUtils.getShortCode(name.trim());
        String code = raw != null ? raw.toUpperCase() : "";
        return AjaxResult.success("操作成功", code);
    }

    /**
     * 注册页省/市/区县级联：数据与部门管理一致。不传 parentId 时返回「医承云配」直属子部门作为省；传 parentId 时返回该部门的直属子部门。
     */
    @GetMapping("/deptRegionOptions")
    @ResponseBody
    public AjaxResult deptRegionOptions(@RequestParam(value = "parentId", required = false) Long parentId) {
        if (!("true".equals(configService.selectConfigByKey("sys.account.registerUser")))) {
            return error("当前系统未开启注册功能");
        }
        List<SysDept> list = deptService.listDeptChildrenForSupplierRegister(parentId);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (SysDept d : list) {
            Map<String, Object> m = new HashMap<>();
            m.put("deptId", d.getDeptId());
            m.put("deptName", d.getDeptName());
            rows.add(m);
        }
        return AjaxResult.success("操作成功", rows);
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
        String loginNameErr = LoginNameUtils.validateLoginName(user.getLoginName(), SUPPLIER_REGISTER_LOGIN_NAME_MAX);
        if (loginNameErr != null) {
            return error(loginNameErr);
        }
        if (StringUtils.isEmpty(user.getPassword())) {
            return error("密码不能为空");
        }
        String supplierRealName = StringUtils.trim(user.getRealName());
        if (StringUtils.isEmpty(supplierRealName)) {
            return error("用户姓名不能为空");
        }
        if (supplierRealName.length() > 50) {
            return error("用户姓名不能超过50个字符");
        }
        user.setRealName(supplierRealName);
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
        String loginNameErr = LoginNameUtils.validateLoginName(user.getLoginName(), SUPPLIER_REGISTER_LOGIN_NAME_MAX);
        if (loginNameErr != null) {
            return error(loginNameErr);
        }
        if (StringUtils.isEmpty(user.getPassword())) {
            return error("密码不能为空");
        }
        String salesRealName = StringUtils.trim(user.getRealName());
        if (StringUtils.isEmpty(salesRealName)) {
            return error("用户姓名不能为空");
        }
        if (salesRealName.length() > 50) {
            return error("用户姓名不能超过50个字符");
        }
        user.setRealName(salesRealName);
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

