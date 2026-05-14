package com.scm.web.controller.hospital;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.scm.common.annotation.Log;
import com.scm.common.core.controller.BaseController;
import com.scm.common.core.domain.AjaxResult;
import com.scm.common.core.page.TableDataInfo;
import com.scm.common.enums.BusinessType;
import com.scm.common.utils.ShiroUtils;
import com.scm.common.utils.StringUtils;
import com.scm.common.utils.poi.ExcelUtil;
import com.scm.system.domain.ScmHospitalSupplierModifyApply;
import com.scm.system.service.IHospitalSupplierService;
import com.scm.system.service.IScmHospitalContextService;

/**
 * 医院审核供应商发起的「关联关系修改申请」
 */
@Controller
@RequestMapping("/hospital/associateModifyAudit")
public class HospitalSupplierAssociateModifyAuditController extends BaseController
{
    @Autowired
    private IHospitalSupplierService hospitalSupplierService;

    @Autowired
    private IScmHospitalContextService scmHospitalContextService;

    @RequiresPermissions("hospital:associateModify:view")
    @GetMapping()
    public String index()
    {
        return "hospital/associate_modify_audit";
    }

    @RequiresPermissions("hospital:associateModify:view")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(@RequestParam(value = "auditStatus", required = false) String auditStatus,
        @RequestParam(value = "hospitalKeyword", required = false) String hospitalKeyword,
        @RequestParam(value = "supplierKeyword", required = false) String supplierKeyword)
    {
        Long hospitalCtx = scmHospitalContextService.resolveHospitalIdForUser(ShiroUtils.getUserId());
        ScmHospitalSupplierModifyApply q = new ScmHospitalSupplierModifyApply();
        if (hospitalCtx != null)
        {
            q.setHospitalId(String.valueOf(hospitalCtx));
        }
        if (auditStatus != null && auditStatus.length() > 0)
        {
            q.setAuditStatus(auditStatus);
        }
        q.setHospitalKeyword(StringUtils.trimToNull(hospitalKeyword));
        q.setSupplierKeyword(StringUtils.trimToNull(supplierKeyword));
        startPage();
        List<ScmHospitalSupplierModifyApply> list = hospitalSupplierService.selectAssociationModifyApplyList(q);
        return getDataTable(list);
    }

    @RequiresPermissions("hospital:associateModify:export")
    @Log(title = "医院关联修改申请审核", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(@RequestParam(value = "auditStatus", required = false) String auditStatus,
        @RequestParam(value = "hospitalKeyword", required = false) String hospitalKeyword,
        @RequestParam(value = "supplierKeyword", required = false) String supplierKeyword,
        HttpServletResponse response)
    {
        Long hospitalCtx = scmHospitalContextService.resolveHospitalIdForUser(ShiroUtils.getUserId());
        ScmHospitalSupplierModifyApply q = new ScmHospitalSupplierModifyApply();
        if (hospitalCtx != null)
        {
            q.setHospitalId(String.valueOf(hospitalCtx));
        }
        if (auditStatus != null && auditStatus.length() > 0)
        {
            q.setAuditStatus(auditStatus);
        }
        q.setHospitalKeyword(StringUtils.trimToNull(hospitalKeyword));
        q.setSupplierKeyword(StringUtils.trimToNull(supplierKeyword));
        List<ScmHospitalSupplierModifyApply> list = hospitalSupplierService.selectAssociationModifyApplyList(q);
        ExcelUtil<ScmHospitalSupplierModifyApply> util = new ExcelUtil<>(ScmHospitalSupplierModifyApply.class);
        util.exportExcel(response, list, "医院关联修改申请");
    }

    @RequiresPermissions("hospital:associateModify:audit")
    @PostMapping("/audit")
    @ResponseBody
    public AjaxResult audit(@RequestParam("modifyApplyId") String modifyApplyId,
        @RequestParam("approved") String approved,
        @RequestParam(value = "auditRemark", required = false) String auditRemark)
    {
        try
        {
            Long hospitalCtx = scmHospitalContextService.resolveHospitalIdForUser(ShiroUtils.getUserId());
            String operBy = ShiroUtils.getLoginName();
            hospitalSupplierService.auditAssociationModifyApply(modifyApplyId, approved, auditRemark, operBy,
                hospitalCtx);
            return success("1".equals(approved) ? "审核通过，已更新关联数据" : "已拒绝该修改申请");
        }
        catch (Exception e)
        {
            return error(e.getMessage());
        }
    }
}
