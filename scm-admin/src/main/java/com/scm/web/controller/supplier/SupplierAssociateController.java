package com.scm.web.controller.supplier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.scm.common.core.controller.BaseController;
import com.scm.common.core.domain.AjaxResult;
import com.scm.common.utils.ShiroUtils;
import com.scm.system.domain.Supplier;
import com.scm.system.service.ISupplierRegisterService;
import com.scm.system.service.ISupplierService;

/**
 * 新增供应商关联：注册用户选择供应商提交关联申请，供应商管理员审核通过后添加供应商业务员角色
 */
@Controller
@RequestMapping("/supplier/associate")
public class SupplierAssociateController extends BaseController {

    @Autowired
    private ISupplierService supplierService;
    @Autowired
    private ISupplierRegisterService supplierRegisterService;

    @RequiresPermissions("supplier:associate:view")
    @GetMapping()
    public String index() {
        return "supplier/associate";
    }

    /** 可选供应商列表（供下拉选择） */
    @RequiresPermissions("supplier:associate:view")
    @GetMapping("/listSuppliers")
    @ResponseBody
    public AjaxResult listSuppliers() {
        List<Supplier> list = supplierService.selectSupplierList(new Supplier());
        List<Map<String, Object>> rows = list.stream().map(s -> {
            Map<String, Object> m = new HashMap<>();
            m.put("supplierId", s.getSupplierId());
            m.put("companyName", s.getCompanyName());
            return m;
        }).collect(Collectors.toList());
        return success(rows);
    }

    /** 提交关联申请（当前登录用户） */
    @RequiresPermissions("supplier:associate:add")
    @PostMapping("/submit")
    @ResponseBody
    public AjaxResult submit(@RequestParam Long supplierId) {
        Long userId = ShiroUtils.getUserId();
        if (userId == null) {
            return error("请先登录");
        }
        try {
            String operBy = ShiroUtils.getLoginName();
            supplierRegisterService.submitSupplierAssociate(supplierId, userId, operBy);
            return success("提交成功，请等待供应商管理员审核");
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            logger.error("提交供应商关联申请失败", e);
            return error("提交失败：" + e.getMessage());
        }
    }
}
