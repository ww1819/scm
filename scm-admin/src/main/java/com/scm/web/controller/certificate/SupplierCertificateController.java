package com.scm.web.controller.certificate;

import java.util.ArrayList;
import java.util.List;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.scm.common.annotation.Log;
import com.scm.common.core.controller.BaseController;
import com.scm.common.core.domain.AjaxResult;
import com.scm.common.core.page.TableDataInfo;
import com.scm.common.enums.BusinessType;
import com.scm.common.utils.poi.ExcelUtil;
import com.scm.system.domain.Supplier;
import com.scm.system.domain.SupplierCertificate;
import com.scm.system.domain.CertificateType;
import com.scm.system.service.ISupplierCertificateService;
import com.scm.system.service.ICertificateTypeService;
import com.scm.system.service.ISupplierService;

/**
 * 供应商证件信息
 * 
 * @author scm
 */
@Controller
@RequestMapping("/certificate/supplier")
public class SupplierCertificateController extends BaseController
{
    private String prefix = "certificate/supplier";

    @Autowired
    private ISupplierCertificateService supplierCertificateService;

    @Autowired
    private ISupplierService supplierService;

    @Autowired
    private ICertificateTypeService certificateTypeService;

    @RequiresPermissions("certificate:supplier:view")
    @GetMapping()
    public String supplierCertificate()
    {
        return prefix + "/supplier";
    }

    /**
     * 供应商资质审核页面
     */
    @RequiresPermissions("certificate:supplier:audit")
    @GetMapping("/audit")
    public String supplierCertificateAudit()
    {
        return prefix + "/audit";
    }

    /**
     * 查询供应商证件列表
     */
    @RequiresPermissions("certificate:supplier:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(SupplierCertificate supplierCertificate, String supplierIds, Long hospitalId)
    {
        startPage();
        // 如果传入了供应商ID列表（逗号分隔），则只查询这些供应商的证件
        if (supplierIds != null && !supplierIds.isEmpty())
        {
            String[] idArray = supplierIds.split(",");
            List<Long> supplierIdList = new ArrayList<>();
            for (String id : idArray)
            {
                if (!id.isEmpty())
                {
                    try
                    {
                        supplierIdList.add(Long.parseLong(id));
                    }
                    catch (NumberFormatException e)
                    {
                        // 忽略无效的ID
                    }
                }
            }
            // 如果只有一个供应商ID，直接设置
            if (supplierIdList.size() == 1)
            {
                supplierCertificate.setSupplierId(supplierIdList.get(0));
            }
            else if (supplierIdList.size() > 1)
            {
                // 多个供应商ID，需要在Service层处理，同时传递医院ID用于过滤
                List<SupplierCertificate> list = supplierCertificateService.selectSupplierCertificateListBySupplierIds(supplierCertificate, supplierIdList, hospitalId);
                return getDataTable(list);
            }
        }
        List<SupplierCertificate> list = supplierCertificateService.selectSupplierCertificateList(supplierCertificate);
        return getDataTable(list);
    }

    /**
     * 查询过期预警的供应商证件列表
     */
    @RequiresPermissions("certificate:supplier:view")
    @PostMapping("/expiringList")
    @ResponseBody
    public TableDataInfo expiringList(SupplierCertificate supplierCertificate)
    {
        startPage();
        List<SupplierCertificate> list = supplierCertificateService.selectExpiringCertificateList(supplierCertificate);
        return getDataTable(list);
    }

    /**
     * 导出供应商证件列表
     */
    @RequiresPermissions("certificate:supplier:export")
    @Log(title = "供应商证件管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(SupplierCertificate supplierCertificate)
    {
        List<SupplierCertificate> list = supplierCertificateService.selectSupplierCertificateList(supplierCertificate);
        ExcelUtil<SupplierCertificate> util = new ExcelUtil<SupplierCertificate>(SupplierCertificate.class);
        return util.exportExcel(list, "供应商证件数据");
    }

    /**
     * 新增供应商证件
     */
    @RequiresPermissions("certificate:supplier:add")
    @GetMapping("/add")
    public String add(ModelMap mmap)
    {
        // 查询所有供应商列表
        List<Supplier> supplierList = supplierService.selectSupplierList(new Supplier());
        mmap.put("supplierList", supplierList);
        
        // 查询企业证件类型列表（类型分类为supplier）
        CertificateType certificateType = new CertificateType();
        certificateType.setTypeCategory("supplier");
        certificateType.setStatus("0"); // 只查询启用状态的
        List<CertificateType> certificateTypeList = certificateTypeService.selectCertificateTypeList(certificateType);
        mmap.put("certificateTypeList", certificateTypeList);
        
        return prefix + "/add";
    }

    /**
     * 新增保存供应商证件
     */
    @RequiresPermissions("certificate:supplier:add")
    @Log(title = "供应商证件管理", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(@Validated SupplierCertificate supplierCertificate)
    {
        supplierCertificate.setCreateBy(getLoginName());
        return toAjax(supplierCertificateService.insertSupplierCertificate(supplierCertificate));
    }

    /**
     * 修改供应商证件
     */
    @RequiresPermissions("certificate:supplier:edit")
    @GetMapping("/edit/{certificateId}")
    public String edit(@PathVariable("certificateId") Long certificateId, ModelMap mmap)
    {
        SupplierCertificate supplierCertificate = supplierCertificateService.selectSupplierCertificateById(certificateId);
        mmap.put("supplierCertificate", supplierCertificate);
        // 查询所有供应商列表
        List<Supplier> supplierList = supplierService.selectSupplierList(new Supplier());
        mmap.put("supplierList", supplierList);
        
        // 查询企业证件类型列表（类型分类为supplier）
        CertificateType certificateType = new CertificateType();
        certificateType.setTypeCategory("supplier");
        certificateType.setStatus("0"); // 只查询启用状态的
        List<CertificateType> certificateTypeList = certificateTypeService.selectCertificateTypeList(certificateType);
        mmap.put("certificateTypeList", certificateTypeList);
        
        return prefix + "/edit";
    }

    /**
     * 修改保存供应商证件
     */
    @RequiresPermissions("certificate:supplier:edit")
    @Log(title = "供应商证件管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(@Validated SupplierCertificate supplierCertificate)
    {
        supplierCertificate.setUpdateBy(getLoginName());
        return toAjax(supplierCertificateService.updateSupplierCertificate(supplierCertificate));
    }

    /**
     * 删除供应商证件
     */
    @RequiresPermissions("certificate:supplier:remove")
    @Log(title = "供应商证件管理", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        return toAjax(supplierCertificateService.deleteSupplierCertificateByIds(ids));
    }

    /**
     * 审核供应商证件页面
     */
    @RequiresPermissions("certificate:supplier:audit")
    @GetMapping("/audit/{certificateId}")
    public String audit(@PathVariable("certificateId") Long certificateId, ModelMap mmap)
    {
        SupplierCertificate supplierCertificate = supplierCertificateService.selectSupplierCertificateById(certificateId);
        mmap.put("supplierCertificate", supplierCertificate);
        return prefix + "/auditDetail";
    }

    /**
     * 审核保存供应商证件
     */
    @RequiresPermissions("certificate:supplier:audit")
    @Log(title = "供应商证件审核", businessType = BusinessType.UPDATE)
    @PostMapping("/audit")
    @ResponseBody
    public AjaxResult auditSave(SupplierCertificate supplierCertificate)
    {
        supplierCertificate.setAuditBy(getLoginName());
        return toAjax(supplierCertificateService.auditSupplierCertificate(supplierCertificate));
    }

    /**
     * 检查并更新证件过期状态
     */
    @RequiresPermissions("certificate:supplier:edit")
    @PostMapping("/checkExpired")
    @ResponseBody
    public AjaxResult checkExpired()
    {
        supplierCertificateService.checkAndUpdateExpiredStatus();
        return success("证件过期状态检查完成");
    }
}

