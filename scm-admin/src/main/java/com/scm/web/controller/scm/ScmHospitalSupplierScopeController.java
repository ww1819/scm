package com.scm.web.controller.scm;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.scm.common.core.controller.BaseController;
import com.scm.common.core.domain.AjaxResult;
import com.scm.common.exception.ServiceException;
import com.scm.system.domain.Hospital;
import com.scm.system.domain.Supplier;
import com.scm.system.service.IScmHospitalSupplierMenuScopeService;

/**
 * 医院-供应商联合权限界面：下拉医院列表、下拉供应商列表（按当前角色与菜单授权联合键过滤）
 */
@RestController
@RequestMapping("/scm/scope/hospitalSupplier")
public class ScmHospitalSupplierScopeController extends BaseController
{
    @Autowired
    private IScmHospitalSupplierMenuScopeService scmHospitalSupplierMenuScopeService;

    /**
     * @param menuId 当前页面对应的 sys_menu.menu_id（须为 auth_type=hospital_supplier）
     */
    @GetMapping("/hospitals")
    public AjaxResult hospitals(@RequestParam("menuId") Long menuId)
    {
        try
        {
            List<Hospital> list = scmHospitalSupplierMenuScopeService.listHospitalsForDropdown(getUserId(), menuId);
            return AjaxResult.success(list);
        }
        catch (ServiceException e)
        {
            return error(e.getMessage());
        }
    }

    @GetMapping("/suppliers")
    public AjaxResult suppliers(@RequestParam("menuId") Long menuId)
    {
        try
        {
            List<Supplier> list = scmHospitalSupplierMenuScopeService.listSuppliersForDropdown(getUserId(), menuId);
            return AjaxResult.success(list);
        }
        catch (ServiceException e)
        {
            return error(e.getMessage());
        }
    }
}
