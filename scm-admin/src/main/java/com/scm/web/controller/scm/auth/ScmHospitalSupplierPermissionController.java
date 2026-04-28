package com.scm.web.controller.scm.auth;

import java.util.List;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.scm.common.annotation.Log;
import com.scm.common.core.controller.BaseController;
import com.scm.common.core.domain.AjaxResult;
import com.scm.common.core.page.TableDataInfo;
import com.scm.common.enums.BusinessType;
import com.scm.system.domain.ScmHospitalSupplierPermission;
import com.scm.system.service.IScmHospitalSupplierPermissionService;

/**
 * 医院-供应商数据权限黑名单
 */
@Controller
@RequestMapping("/scm/auth/hospitalSupplierPerm")
public class ScmHospitalSupplierPermissionController extends BaseController
{
    private static final String PREFIX = "scm/auth/hospital_supplier_perm";

    @Autowired
    private IScmHospitalSupplierPermissionService permissionService;

    @RequiresPermissions("scmAuth:hospitalSupplier:view")
    @GetMapping()
    public String page()
    {
        return PREFIX;
    }

    @RequiresPermissions("scmAuth:hospitalSupplier:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ScmHospitalSupplierPermission query)
    {
        startPage();
        List<ScmHospitalSupplierPermission> list = permissionService.selectList(query);
        return getDataTable(list);
    }

    @RequiresPermissions("scmAuth:hospitalSupplier:add")
    @Log(title = "医院供应商数据权限", businessType = BusinessType.INSERT)
    @PostMapping("/save")
    @ResponseBody
    public AjaxResult save(ScmHospitalSupplierPermission row)
    {
        return toAjax(permissionService.saveOrUpdate(row, getLoginName()));
    }

    @RequiresPermissions("scmAuth:hospitalSupplier:remove")
    @Log(title = "医院供应商数据权限", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String id)
    {
        return toAjax(permissionService.removeLogical(id, getLoginName()));
    }
}
