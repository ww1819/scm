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
import com.scm.system.domain.ScmHospitalSupplierApply;
import com.scm.system.service.IHospitalSupplierService;
import com.scm.system.service.IScmHospitalContextService;

/**
 * 医院关联申请审核
 */
@Controller
@RequestMapping("/hospital/associateAudit")
public class HospitalSupplierAssociateAuditController extends BaseController
{
    @Autowired
    private IHospitalSupplierService hospitalSupplierService;

    @Autowired
    private IScmHospitalContextService scmHospitalContextService;

    @RequiresPermissions("hospital:associateAudit:view")
    @GetMapping()
    public String index()
    {
        return "hospital/associate_audit";
    }

    @RequiresPermissions("hospital:associateAudit:view")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(@RequestParam(value = "auditStatus", required = false) String auditStatus,
        @RequestParam(value = "hospitalKeyword", required = false) String hospitalKeyword,
        @RequestParam(value = "supplierKeyword", required = false) String supplierKeyword)
    {
        Long hospitalCtx = scmHospitalContextService.resolveHospitalIdForUser(ShiroUtils.getUserId());
        ScmHospitalSupplierApply q = new ScmHospitalSupplierApply();
        // 已绑定医院：仅看本院申请；未绑定（平台管理员等）：查看全部医院-供应商关联申请
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
        List<ScmHospitalSupplierApply> list = hospitalSupplierService.selectAssociationApplyList(q);
        return getDataTable(list);
    }

    /**
     * 导出医院关联申请（与当前筛选条件一致，用于通知供应商等）
     */
    @RequiresPermissions("hospital:associateAudit:export")
    @Log(title = "医院关联申请审核", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(@RequestParam(value = "auditStatus", required = false) String auditStatus,
        @RequestParam(value = "hospitalKeyword", required = false) String hospitalKeyword,
        @RequestParam(value = "supplierKeyword", required = false) String supplierKeyword,
        HttpServletResponse response)
    {
        Long hospitalCtx = scmHospitalContextService.resolveHospitalIdForUser(ShiroUtils.getUserId());
        ScmHospitalSupplierApply q = new ScmHospitalSupplierApply();
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
        List<ScmHospitalSupplierApply> list = hospitalSupplierService.selectAssociationApplyList(q);
        ExcelUtil<ScmHospitalSupplierApply> util = new ExcelUtil<ScmHospitalSupplierApply>(ScmHospitalSupplierApply.class);
        util.exportExcel(response, list, "医院关联申请");
    }

    @RequiresPermissions("hospital:associateAudit:audit")
    @PostMapping("/audit")
    @ResponseBody
    public AjaxResult audit(@RequestParam("applyId") String applyId,
        @RequestParam("approved") String approved,
        @RequestParam(value = "auditRemark", required = false) String auditRemark)
    {
        try
        {
            Long hospitalCtx = scmHospitalContextService.resolveHospitalIdForUser(ShiroUtils.getUserId());
            String operBy = ShiroUtils.getLoginName();
            hospitalSupplierService.auditAssociationApply(applyId, approved, auditRemark, operBy, hospitalCtx);
            return success("1".equals(approved) ? "审核通过" : "审核拒绝");
        }
        catch (Exception e)
        {
            return error(e.getMessage());
        }
    }
}
