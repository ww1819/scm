package com.scm.web.controller.certificate;

import java.util.List;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.scm.common.core.controller.BaseController;
import com.scm.common.core.page.TableDataInfo;
import com.scm.system.domain.ScmSupplierCertChangeLog;
import com.scm.system.service.IScmSupplierCertChangeLogService;

/**
 * 供应商资质变更记录（医院查看绑定供应商的变更抄送）
 */
@Controller
@RequestMapping("/certificate/supplier/changeLog")
public class SupplierCertChangeLogController extends BaseController
{
    private String prefix = "certificate/supplier";

    @Autowired
    private IScmSupplierCertChangeLogService scmSupplierCertChangeLogService;

    @RequiresPermissions("certificate:supplierChange:view")
    @GetMapping()
    public String page()
    {
        return prefix + "/changeLog";
    }

    @RequiresPermissions("certificate:supplierChange:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ScmSupplierCertChangeLog query)
    {
        startPage();
        List<ScmSupplierCertChangeLog> list = scmSupplierCertChangeLogService.selectChangeLogList(query);
        return getDataTable(list);
    }
}
