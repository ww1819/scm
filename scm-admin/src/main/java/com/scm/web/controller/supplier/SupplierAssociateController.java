package com.scm.web.controller.supplier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.scm.common.utils.DateUtils;
import com.scm.common.utils.ShiroUtils;
import com.scm.common.utils.StringUtils;
import com.scm.system.domain.Hospital;
import com.scm.system.domain.HospitalSupplier;
import com.scm.system.domain.ScmHospitalSupplierApply;
import com.scm.system.domain.ScmHospitalSupplierModifyApply;
import com.scm.system.domain.SupplierUser;
import com.scm.system.service.IHospitalService;
import com.scm.system.service.IHospitalSupplierService;
import com.scm.system.service.ISupplierUserService;

/**
 * 新增医院关联：供应商向医院提交关联申请，医院审核通过后建立医院供应商绑定关系
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
    public TableDataInfo myList(@RequestParam(value = "auditStatus", required = false) String auditStatus,
        @RequestParam(value = "hospitalKeyword", required = false) String hospitalKeyword,
        @RequestParam(value = "supplierKeyword", required = false) String supplierKeyword) {
        SupplierUser supplierUser = supplierUserService.selectSupplierUserByUserId(ShiroUtils.getUserId());
        startPage();
        List<ScmHospitalSupplierApply> list;
        // 已绑定供应商：仅看本供应商申请；未绑定（平台管理员等）：查看全部关联申请
        if (supplierUser != null && supplierUser.getSupplierId() != null) {
            list = hospitalSupplierService.selectSupplierApplyList(supplierUser.getSupplierId(), auditStatus,
                hospitalKeyword, supplierKeyword);
        } else {
            ScmHospitalSupplierApply q = new ScmHospitalSupplierApply();
            if (auditStatus != null && auditStatus.length() > 0) {
                q.setAuditStatus(auditStatus);
            }
            q.setHospitalKeyword(StringUtils.trimToNull(hospitalKeyword));
            q.setSupplierKeyword(StringUtils.trimToNull(supplierKeyword));
            list = hospitalSupplierService.selectAssociationApplyList(q);
        }
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

    /** 关联修改申请页 */
    @RequiresPermissions("supplier:associateModify:view")
    @GetMapping("/modify")
    public String modifyPage() {
        return "supplier/associate_modify";
    }

    /** 本供应商已审核通过的医院关联（供修改申请下拉引用） */
    @RequiresPermissions("supplier:associateModify:view")
    @GetMapping("/approvedRelations")
    @ResponseBody
    public AjaxResult approvedRelations() {
        SupplierUser supplierUser = supplierUserService.selectSupplierUserByUserId(ShiroUtils.getUserId());
        if (supplierUser == null || supplierUser.getSupplierId() == null) {
            return error("当前账号未绑定供应商");
        }
        HospitalSupplier q = new HospitalSupplier();
        q.setSupplierId(supplierUser.getSupplierId());
        q.setBindStatus("1");
        q.setAuditStatus("1");
        q.setStatus("0");
        List<HospitalSupplier> rels = hospitalSupplierService.selectHospitalSupplierList(q);
        List<Map<String, Object>> rows = new java.util.ArrayList<>();
        if (rels != null) {
            for (HospitalSupplier r : rels) {
                if (r == null || r.getRelationId() == null) {
                    continue;
                }
                if (!"0".equals(StringUtils.trimToEmpty(r.getDisableStatus()))) {
                    continue;
                }
                Map<String, Object> m = new HashMap<>();
                m.put("relationId", r.getRelationId());
                m.put("hospitalId", r.getHospitalId());
                m.put("hospitalName", StringUtils.nvl(r.getHospitalName(), ""));
                m.put("hospitalCode", StringUtils.nvl(r.getHospitalCode(), ""));
                m.put("supplyStartDate", r.getSupplyStartDate());
                m.put("supplyEndDate", r.getSupplyEndDate());
                m.put("supplyStartDateStr", r.getSupplyStartDate() == null ? "" : DateUtils.parseDateToStr("yyyy-MM-dd", r.getSupplyStartDate()));
                m.put("supplyEndDateStr", r.getSupplyEndDate() == null ? "" : DateUtils.parseDateToStr("yyyy-MM-dd", r.getSupplyEndDate()));
                m.put("remark", StringUtils.nvl(r.getRemark(), ""));
                rows.add(m);
            }
        }
        return success(rows);
    }

    /** 引用已审核关联预填数据 */
    @RequiresPermissions("supplier:associateModify:view")
    @GetMapping("/relationPrefill")
    @ResponseBody
    public AjaxResult relationPrefill(@RequestParam("relationId") Long relationId) {
        SupplierUser supplierUser = supplierUserService.selectSupplierUserByUserId(ShiroUtils.getUserId());
        if (supplierUser == null || supplierUser.getSupplierId() == null) {
            return error("当前账号未绑定供应商");
        }
        try {
            HospitalSupplier rel = hospitalSupplierService.assertSupplierApprovedRelationForModify(relationId,
                supplierUser.getSupplierId());
            Map<String, Object> m = new HashMap<>();
            m.put("relationId", rel.getRelationId());
            m.put("hospitalId", rel.getHospitalId());
            m.put("hospitalName", StringUtils.nvl(rel.getHospitalName(), ""));
            m.put("hospitalCode", StringUtils.nvl(rel.getHospitalCode(), ""));
            m.put("supplyStartDate", rel.getSupplyStartDate());
            m.put("supplyEndDate", rel.getSupplyEndDate());
            m.put("supplyStartDateStr", rel.getSupplyStartDate() == null ? "" : DateUtils.parseDateToStr("yyyy-MM-dd", rel.getSupplyStartDate()));
            m.put("supplyEndDateStr", rel.getSupplyEndDate() == null ? "" : DateUtils.parseDateToStr("yyyy-MM-dd", rel.getSupplyEndDate()));
            m.put("remark", StringUtils.nvl(rel.getRemark(), ""));
            return success(m);
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    @RequiresPermissions("supplier:associateModify:add")
    @PostMapping("/submitModify")
    @ResponseBody
    public AjaxResult submitModify(@RequestParam Long relationId,
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
            return error("当前账号未绑定供应商，无法发起修改申请");
        }
        try {
            String operBy = ShiroUtils.getLoginName();
            hospitalSupplierService.submitAssociationModifyFromSupplier(relationId, supplierUser.getSupplierId(),
                supplyStartDate, supplyEndDate, operBy, contractNo, applyReason, contactPerson, contactPhone);
            return success("提交成功，请等待医院审核");
        } catch (Exception e) {
            logger.error("提交关联修改申请失败", e);
            return error("提交失败：" + e.getMessage());
        }
    }

    @RequiresPermissions("supplier:associateModify:view")
    @PostMapping("/myModifyList")
    @ResponseBody
    public TableDataInfo myModifyList(@RequestParam(value = "auditStatus", required = false) String auditStatus,
        @RequestParam(value = "hospitalKeyword", required = false) String hospitalKeyword,
        @RequestParam(value = "supplierKeyword", required = false) String supplierKeyword) {
        SupplierUser supplierUser = supplierUserService.selectSupplierUserByUserId(ShiroUtils.getUserId());
        startPage();
        List<ScmHospitalSupplierModifyApply> list;
        if (supplierUser != null && supplierUser.getSupplierId() != null) {
            list = hospitalSupplierService.selectSupplierModifyApplyList(supplierUser.getSupplierId(), auditStatus,
                hospitalKeyword, supplierKeyword);
        } else {
            ScmHospitalSupplierModifyApply q = new ScmHospitalSupplierModifyApply();
            if (auditStatus != null && auditStatus.length() > 0) {
                q.setAuditStatus(auditStatus);
            }
            q.setHospitalKeyword(StringUtils.trimToNull(hospitalKeyword));
            q.setSupplierKeyword(StringUtils.trimToNull(supplierKeyword));
            list = hospitalSupplierService.selectAssociationModifyApplyList(q);
        }
        return getDataTable(list);
    }

    @RequiresPermissions("supplier:associateModify:withdraw")
    @PostMapping("/withdrawModify")
    @ResponseBody
    public AjaxResult withdrawModify(@RequestParam("modifyApplyId") String modifyApplyId) {
        SupplierUser supplierUser = supplierUserService.selectSupplierUserByUserId(ShiroUtils.getUserId());
        if (supplierUser == null || supplierUser.getSupplierId() == null) {
            return error("当前账号未绑定供应商，无法撤回");
        }
        try {
            hospitalSupplierService.withdrawAssociationModifyApply(modifyApplyId, supplierUser.getSupplierId(),
                ShiroUtils.getLoginName());
            return success("撤回成功");
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }
}
