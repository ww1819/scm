package com.scm.web.controller.certificate;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.scm.common.annotation.Log;
import com.scm.common.exception.ServiceException;
import com.scm.common.core.controller.BaseController;
import com.scm.common.core.domain.AjaxResult;
import com.scm.common.core.page.TableDataInfo;
import com.scm.common.enums.BusinessType;
import com.scm.common.utils.poi.ExcelUtil;
import com.scm.system.domain.Supplier;
import com.scm.system.domain.SupplierCertificate;
import com.scm.system.domain.CertificateType;
import com.scm.system.service.IScmSupplierContextService;
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

    @Autowired
    private IScmSupplierContextService scmSupplierContextService;

    /** 登记页：view 或 list 任一即可（与产品证件登记页策略一致） */
    @RequiresPermissions(value = { "certificate:supplier:view", "certificate:supplier:list" }, logical = Logical.OR)
    @GetMapping()
    public String supplierCertificate(ModelMap mmap)
    {
        Long bindSid = scmSupplierContextService.resolveSupplierIdForUser(getUserId());
        mmap.put("supplierSelfService", bindSid != null);
        mmap.put("bindSupplierId", bindSid);
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
     * 查询供应商证件列表：登记页用 view/list；资质审核页同样调本接口，需含 audit
     */
    @RequiresPermissions(value = { "certificate:supplier:view", "certificate:supplier:list", "certificate:supplier:audit" },
        logical = Logical.OR)
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(SupplierCertificate supplierCertificate, String supplierIds, Long hospitalId)
    {
        startPage();
        Long bindSid = scmSupplierContextService.resolveSupplierIdForUser(getUserId());
        if (bindSid != null)
        {
            supplierCertificate.setSupplierId(bindSid);
            List<SupplierCertificate> scoped = supplierCertificateService.selectSupplierCertificateList(supplierCertificate);
            return getDataTable(scoped);
        }
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
            // 医院筛选场景下，无论供应商数量多少都统一走按供应商ID列表+医院ID查询，
            // 避免单供应商时遗漏hospitalId过滤导致结果不一致。
            if (hospitalId != null && supplierIdList.size() > 0)
            {
                List<SupplierCertificate> list = supplierCertificateService.selectSupplierCertificateListBySupplierIds(supplierCertificate, supplierIdList, hospitalId);
                return getDataTable(list);
            }
            // 如果只有一个供应商ID，直接设置
            if (supplierIdList.size() == 1)
            {
                supplierCertificate.setSupplierId(supplierIdList.get(0));
            }
            else if (supplierIdList.size() > 1)
            {
                List<SupplierCertificate> list = supplierCertificateService.selectSupplierCertificateListBySupplierIds(supplierCertificate, supplierIdList, hospitalId);
                return getDataTable(list);
            }
        }
        List<SupplierCertificate> list = supplierCertificateService.selectSupplierCertificateList(supplierCertificate);
        return getDataTable(list);
    }

    /**
     * 查询过期预警的供应商证件列表（view 或 list）
     */
    @RequiresPermissions(value = { "certificate:supplier:view", "certificate:supplier:list" }, logical = Logical.OR)
    @PostMapping("/expiringList")
    @ResponseBody
    public TableDataInfo expiringList(SupplierCertificate supplierCertificate)
    {
        startPage();
        Long bindSid = scmSupplierContextService.resolveSupplierIdForUser(getUserId());
        if (bindSid != null)
        {
            supplierCertificate.setSupplierId(bindSid);
        }
        List<SupplierCertificate> list = supplierCertificateService.selectExpiringCertificateList(supplierCertificate);
        return getDataTable(list);
    }

    /**
     * 导出供应商证件列表
     */
    @RequiresPermissions("certificate:supplier:export")
    @Log(title = "供应商证件管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(SupplierCertificate supplierCertificate, HttpServletResponse response)
    {
        Long bindSid = scmSupplierContextService.resolveSupplierIdForUser(getUserId());
        if (bindSid != null)
        {
            supplierCertificate.setSupplierId(bindSid);
        }
        List<SupplierCertificate> list = supplierCertificateService.selectSupplierCertificateList(supplierCertificate);
        ExcelUtil<SupplierCertificate> util = new ExcelUtil<SupplierCertificate>(SupplierCertificate.class);
        util.exportExcel(response, list, "供应商证件数据");
    }

    /**
     * 新增供应商证件
     */
    @RequiresPermissions("certificate:supplier:add")
    @GetMapping("/add")
    public String add(@RequestParam(value = "supplierId", required = false) Long preSupplierId, ModelMap mmap)
    {
        fillSupplierCertificateFormModel(mmap, preSupplierId);
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
        fillSupplierCertificateFormModel(mmap, supplierCertificate != null ? supplierCertificate.getSupplierId() : null);
        return prefix + "/edit";
    }

    /**
     * 列表页「上传证照」：仅维护证件图片（登记页 edit；审核页 audit 亦可上传）
     */
    @RequiresPermissions(value = { "certificate:supplier:edit", "certificate:supplier:audit" }, logical = Logical.OR)
    @GetMapping("/upload/{certificateId}")
    public String uploadImages(@PathVariable("certificateId") Long certificateId, ModelMap mmap)
    {
        SupplierCertificate supplierCertificate = null;
        try
        {
            supplierCertificate = supplierCertificateService.selectSupplierCertificateById(certificateId);
        }
        catch (ServiceException e)
        {
            mmap.put("uploadLoadError", e.getMessage());
        }
        if (supplierCertificate == null && !mmap.containsKey("uploadLoadError"))
        {
            mmap.put("uploadLoadError", "证件不存在。");
        }
        mmap.put("supplierCertificate", supplierCertificate);
        return prefix + "/uploadImages";
    }

    /**
     * 保存证照图片（可清空）
     */
    @RequiresPermissions(value = { "certificate:supplier:edit", "certificate:supplier:audit" }, logical = Logical.OR)
    @Log(title = "供应商证件管理", businessType = BusinessType.UPDATE)
    @PostMapping("/updateCertificateFile")
    @ResponseBody
    public AjaxResult updateCertificateFile(Long certificateId, String certificateFile)
    {
        return toAjax(supplierCertificateService.updateCertificateFile(certificateId, certificateFile, getLoginName()));
    }

    private void fillSupplierCertificateFormModel(ModelMap mmap, Long preSupplierId)
    {
        Long bindSid = scmSupplierContextService.resolveSupplierIdForUser(getUserId());
        boolean selfService = bindSid != null;
        mmap.put("supplierSelfService", selfService);
        mmap.put("bindSupplierId", bindSid);
        if (selfService)
        {
            Supplier s = supplierService.selectSupplierById(bindSid);
            mmap.put("bindSupplierCompanyName", s != null ? s.getCompanyName() : "");
            mmap.put("preSupplierId", bindSid);
        }
        else
        {
            List<Supplier> supplierList = supplierService.selectSupplierList(new Supplier());
            mmap.put("supplierList", supplierList);
            if (preSupplierId == null && supplierList != null && supplierList.size() == 1)
            {
                preSupplierId = supplierList.get(0).getSupplierId();
            }
            mmap.put("preSupplierId", preSupplierId);
        }
        CertificateType certificateType = new CertificateType();
        certificateType.setTypeCategory("supplier");
        certificateType.setStatus("0");
        List<CertificateType> certificateTypeList = certificateTypeService.selectCertificateTypeList(certificateType);
        mmap.put("certificateTypeList", certificateTypeList);
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

