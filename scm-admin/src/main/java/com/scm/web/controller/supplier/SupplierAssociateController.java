package com.scm.web.controller.supplier;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.scm.common.core.controller.BaseController;
import com.scm.common.core.domain.AjaxResult;
import com.scm.common.core.page.TableDataInfo;
import com.scm.common.utils.ShiroUtils;
import com.scm.system.domain.Hospital;
import com.scm.system.domain.ScmHospitalSupplierApply;
import com.scm.system.domain.SupplierUser;
import com.scm.system.service.IHospitalService;
import com.scm.system.service.IHospitalSupplierService;
import com.scm.system.service.ISupplierUserService;

/**
 * 新增供应商关联：注册用户选择供应商提交关联申请，供应商管理员审核通过后添加供应商业务员角色
 */
@Controller
@RequestMapping("/supplier/associate")
public class SupplierAssociateController extends BaseController {

    @Autowired
    private IHospitalService hospitalService;

    @Autowired
    private IHospitalSupplierService hospitalSupplierService;

    @Autowired
    private ISupplierUserService supplierUserService;

    @RequiresPermissions("supplier:associate:view")
    @GetMapping()
    public String index() {
        return "supplier/associate";
    }

    /** 可选医院列表（供下拉选择） */
    @RequiresPermissions("supplier:associate:view")
    @GetMapping("/listHospitals")
    @ResponseBody
    public AjaxResult listHospitals() {
        Hospital q = new Hospital();
        q.setStatus("0");
        List<java.util.Map<String, Object>> rows = hospitalService.selectHospitalList(q).stream().map(h -> {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("hospitalId", h.getHospitalId());
            m.put("hospitalName", h.getHospitalName());
            return m;
        }).collect(Collectors.toList());
        return success(rows);
    }

    /** 供应商向医院提交关联申请（当前登录用户所属供应商） */
    @RequiresPermissions("supplier:associate:add")
    @PostMapping("/submit")
    @ResponseBody
    public AjaxResult submit(@RequestParam Long hospitalId,
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") java.util.Date supplyStartDate,
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") java.util.Date supplyEndDate,
        @RequestParam(value = "contractNo", required = false) String contractNo,
        @RequestParam(value = "applyReason", required = false) String applyReason,
        @RequestParam(value = "contactPerson", required = false) String contactPerson,
        @RequestParam(value = "contactPhone", required = false) String contactPhone) {
        Long userId = ShiroUtils.getUserId();
        if (userId == null) {
            return error("请先登录");
        }
        SupplierUser supplierUser = supplierUserService.selectSupplierUserByUserId(userId);
        if (supplierUser == null || supplierUser.getSupplierId() == null) {
            return error("当前账号未绑定供应商，无法发起关联");
        }
        try {
            String operBy = ShiroUtils.getLoginName();
            hospitalSupplierService.submitAssociationFromSupplier(supplierUser.getSupplierId(), hospitalId, supplyStartDate,
                supplyEndDate, operBy, contractNo, applyReason, contactPerson, contactPhone);
            return success("提交成功，请等待医院审核");
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            logger.error("提交医院关联申请失败", e);
            return error("提交失败：" + e.getMessage());
        }
    }

    @RequiresPermissions("supplier:associate:view")
    @PostMapping("/myList")
    @ResponseBody
    public TableDataInfo myList(@RequestParam(value = "auditStatus", required = false) String auditStatus) {
        SupplierUser supplierUser = supplierUserService.selectSupplierUserByUserId(ShiroUtils.getUserId());
        if (supplierUser == null || supplierUser.getSupplierId() == null) {
            return getDataTable(new java.util.ArrayList<ScmHospitalSupplierApply>());
        }
        startPage();
        List<ScmHospitalSupplierApply> list =
            hospitalSupplierService.selectSupplierApplyList(supplierUser.getSupplierId(), auditStatus);
        return getDataTable(list);
    }

    @RequiresPermissions("supplier:associate:withdraw")
    @PostMapping("/withdraw")
    @ResponseBody
    public AjaxResult withdraw(@RequestParam("applyId") String applyId) {
        SupplierUser supplierUser = supplierUserService.selectSupplierUserByUserId(ShiroUtils.getUserId());
        if (supplierUser == null || supplierUser.getSupplierId() == null) {
            return error("当前账号未绑定供应商，无法撤回");
        }
        try {
            hospitalSupplierService.withdrawAssociationApply(applyId, supplierUser.getSupplierId(), ShiroUtils.getLoginName());
            return success("撤回成功");
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }
}
