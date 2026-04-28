package com.scm.web.controller.hospital;

import java.util.ArrayList;
import java.util.List;
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
import com.scm.common.core.page.TableDataInfo;
import com.scm.common.utils.ShiroUtils;
import com.scm.system.domain.ScmHospitalSupplierApply;
import com.scm.system.service.IHospitalSupplierService;
import com.scm.system.service.IScmHospitalContextService;

/**
 * 医院审核供应商关联申请
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
    public TableDataInfo list(@RequestParam(value = "auditStatus", required = false) String auditStatus)
    {
        Long hospitalCtx = scmHospitalContextService.resolveHospitalIdForUser(ShiroUtils.getUserId());
        if (hospitalCtx == null)
        {
            return getDataTable(new ArrayList<>());
        }
        ScmHospitalSupplierApply q = new ScmHospitalSupplierApply();
        q.setHospitalId(String.valueOf(hospitalCtx));
        if (auditStatus != null && auditStatus.length() > 0)
        {
            q.setAuditStatus(auditStatus);
        }
        startPage();
        List<ScmHospitalSupplierApply> list = hospitalSupplierService.selectAssociationApplyList(q);
        return getDataTable(list);
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
