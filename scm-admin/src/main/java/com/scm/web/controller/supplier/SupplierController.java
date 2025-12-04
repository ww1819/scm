package com.scm.web.controller.supplier;

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
import com.scm.common.utils.StringUtils;
import com.scm.system.domain.Hospital;
import com.scm.system.domain.HospitalSupplier;
import com.scm.system.domain.Supplier;
import com.scm.system.domain.SupplierCertificate;
import com.scm.system.service.IHospitalService;
import com.scm.system.service.IHospitalSupplierService;
import com.scm.system.service.ISupplierService;
import com.scm.system.service.ISupplierCertificateService;

/**
 * 供应商信息
 * 
 * @author scm
 */
@Controller
@RequestMapping("/supplier/supplier")
public class SupplierController extends BaseController
{
    private String prefix = "supplier";

    @Autowired
    private ISupplierService supplierService;

    @Autowired
    private ISupplierCertificateService supplierCertificateService;

    @Autowired
    private IHospitalSupplierService hospitalSupplierService;

    @Autowired
    private IHospitalService hospitalService;

    @RequiresPermissions("supplier:supplier:view")
    @GetMapping()
    public String supplier()
    {
        return prefix + "/supplier";
    }

    /**
     * 查询供应商信息列表
     */
    @RequiresPermissions("supplier:supplier:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(Supplier supplier)
    {
        startPage();
        List<Supplier> list = supplierService.selectSupplierList(supplier);
        return getDataTable(list);
    }

    /**
     * 导出供应商信息列表
     */
    @RequiresPermissions("supplier:supplier:export")
    @Log(title = "供应商管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(Supplier supplier)
    {
        List<Supplier> list = supplierService.selectSupplierList(supplier);
        ExcelUtil<Supplier> util = new ExcelUtil<Supplier>(Supplier.class);
        return util.exportExcel(list, "供应商数据");
    }

    /**
     * 新增供应商信息
     */
    @RequiresPermissions("supplier:supplier:add")
    @GetMapping("/add")
    public String add()
    {
        return prefix + "/add";
    }

    /**
     * 新增保存供应商信息
     */
    @RequiresPermissions("supplier:supplier:add")
    @Log(title = "供应商管理", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(@Validated Supplier supplier, String hospitalIds)
    {
        if (!supplierService.checkSupplierCodeUnique(supplier))
        {
            return error("新增供应商'" + supplier.getCompanyName() + "'失败，供应商编码已存在");
        }
        supplier.setCreateBy(getLoginName());
        int result = supplierService.insertSupplier(supplier);
        
        // 保存配送公司关联关系
        if (result > 0 && supplier.getSupplierId() != null && StringUtils.isNotEmpty(hospitalIds))
        {
            String[] hospitalIdArray = hospitalIds.split(",");
            hospitalSupplierService.saveSupplierHospitals(supplier.getSupplierId(), hospitalIdArray, getLoginName());
        }
        
        return toAjax(result);
    }

    /**
     * 查看供应商详情
     */
    @RequiresPermissions("supplier:supplier:view")
    @GetMapping("/view/{supplierId}")
    public String view(@PathVariable("supplierId") Long supplierId, ModelMap mmap)
    {
        Supplier supplier = supplierService.selectSupplierById(supplierId);
        mmap.put("supplier", supplier);
        // 查询该供应商的证件列表
        SupplierCertificate certificate = new SupplierCertificate();
        certificate.setSupplierId(supplierId);
        List<SupplierCertificate> certificateList = supplierCertificateService.selectSupplierCertificateList(certificate);
        mmap.put("certificateList", certificateList);
        return prefix + "/view";
    }

    /**
     * 获取所有供应商列表（不分页，用于下拉选择等场景）
     */
    @GetMapping("/all")
    @ResponseBody
    public AjaxResult getAllSuppliers()
    {
        try
        {
            Supplier supplier = new Supplier();
            // 不限制状态，获取所有供应商（包括正常和停用的）
            // 注意：这里不使用startPage()，直接获取所有数据
            List<Supplier> list = supplierService.selectSupplierList(supplier);
            
            // 确保返回非null列表
            List<Supplier> resultList = list != null ? list : new ArrayList<>();
            return success(resultList);
        }
        catch (Exception e)
        {
            logger.error("获取所有供应商列表失败", e);
            return error("获取供应商列表失败：" + e.getMessage());
        }
    }

    /**
     * 查询供应商证件列表（用于供应商详情页面）
     */
    @RequiresPermissions("supplier:supplier:view")
    @PostMapping("/certificateList/{supplierId}")
    @ResponseBody
    public TableDataInfo certificateList(@PathVariable("supplierId") Long supplierId)
    {
        startPage();
        SupplierCertificate certificate = new SupplierCertificate();
        certificate.setSupplierId(supplierId);
        List<SupplierCertificate> list = supplierCertificateService.selectSupplierCertificateList(certificate);
        return getDataTable(list);
    }

    /**
     * 修改供应商信息
     */
    @RequiresPermissions("supplier:supplier:edit")
    @GetMapping("/edit/{supplierId}")
    public String edit(@PathVariable("supplierId") Long supplierId, ModelMap mmap)
    {
        Supplier supplier = supplierService.selectSupplierById(supplierId);
        mmap.put("supplier", supplier);
        return prefix + "/edit";
    }

    /**
     * 获取供应商关联的医院列表
     */
    @RequiresPermissions("supplier:supplier:view")
    @GetMapping("/hospitals/{supplierId}")
    @ResponseBody
    public AjaxResult getSupplierHospitals(@PathVariable("supplierId") Long supplierId)
    {
        List<HospitalSupplier> relations = hospitalSupplierService.selectHospitalSupplierBySupplierId(supplierId);
        List<Hospital> hospitals = new java.util.ArrayList<>();
        for (HospitalSupplier relation : relations)
        {
            Hospital hospital = hospitalService.selectHospitalById(relation.getHospitalId());
            if (hospital != null)
            {
                hospitals.add(hospital);
            }
        }
        return success(hospitals);
    }

    /**
     * 修改保存供应商信息
     */
    @RequiresPermissions("supplier:supplier:edit")
    @Log(title = "供应商管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(@Validated Supplier supplier, String hospitalIds)
    {
        if (!supplierService.checkSupplierCodeUnique(supplier))
        {
            return error("修改供应商'" + supplier.getCompanyName() + "'失败，供应商编码已存在");
        }
        supplier.setUpdateBy(getLoginName());
        int result = supplierService.updateSupplier(supplier);
        
        // 保存配送公司关联关系
        if (result > 0 && StringUtils.isNotEmpty(hospitalIds))
        {
            String[] hospitalIdArray = hospitalIds.split(",");
            hospitalSupplierService.saveSupplierHospitals(supplier.getSupplierId(), hospitalIdArray, getLoginName());
        }
        else if (result > 0 && StringUtils.isEmpty(hospitalIds))
        {
            // 如果hospitalIds为空，删除所有关联
            hospitalSupplierService.deleteHospitalSupplierBySupplierId(supplier.getSupplierId());
        }
        
        return toAjax(result);
    }

    /**
     * 删除供应商信息
     */
    @RequiresPermissions("supplier:supplier:remove")
    @Log(title = "供应商管理", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        return toAjax(supplierService.deleteSupplierByIds(ids));
    }

    /**
     * 审核供应商
     */
    @RequiresPermissions("supplier:supplier:audit")
    @GetMapping("/audit/{supplierId}")
    public String audit(@PathVariable("supplierId") Long supplierId, ModelMap mmap)
    {
        Supplier supplier = supplierService.selectSupplierById(supplierId);
        mmap.put("supplier", supplier);
        return prefix + "/audit";
    }

    /**
     * 审核保存供应商
     */
    @RequiresPermissions("supplier:supplier:audit")
    @Log(title = "供应商审核", businessType = BusinessType.UPDATE)
    @PostMapping("/audit")
    @ResponseBody
    public AjaxResult auditSave(Supplier supplier)
    {
        supplier.setAuditBy(getLoginName());
        return toAjax(supplierService.auditSupplier(supplier));
    }

    /**
     * 启用/停用供应商
     */
    @RequiresPermissions("supplier:supplier:edit")
    @Log(title = "供应商管理", businessType = BusinessType.UPDATE)
    @PostMapping("/changeStatus")
    @ResponseBody
    public AjaxResult changeStatus(Supplier supplier)
    {
        supplier.setUpdateBy(getLoginName());
        return toAjax(supplierService.updateSupplier(supplier));
    }
}

