package com.scm.web.controller.certificate;

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
import com.scm.system.domain.ProductCertificate;
import com.scm.system.domain.Supplier;
import com.scm.system.service.IProductCertificateService;
import com.scm.system.service.ISupplierService;
import com.scm.system.service.IMaterialDictService;

/**
 * 产品证件信息
 * 
 * @author scm
 */
@Controller
@RequestMapping("/certificate/product")
public class ProductCertificateController extends BaseController
{
    private String prefix = "certificate/product";

    @Autowired
    private IProductCertificateService productCertificateService;
    
    @Autowired
    private ISupplierService supplierService;
    
    @Autowired
    private IMaterialDictService materialDictService;

    @RequiresPermissions("certificate:product:view")
    @GetMapping()
    public String productCertificate()
    {
        return prefix + "/product";
    }

    /**
     * 产品证件审核页面
     */
    @RequiresPermissions("certificate:product:audit")
    @GetMapping("/audit")
    public String productCertificateAudit()
    {
        return prefix + "/auditList";
    }


    /**
     * 查询产品证件列表
     */
    @RequiresPermissions("certificate:product:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ProductCertificate productCertificate)
    {
        startPage();
        List<ProductCertificate> list = productCertificateService.selectProductCertificateList(productCertificate);
        return getDataTable(list);
    }

    /**
     * 查询过期预警的产品证件列表
     */
    @RequiresPermissions("certificate:product:view")
    @PostMapping("/expiringList")
    @ResponseBody
    public TableDataInfo expiringList(ProductCertificate productCertificate)
    {
        startPage();
        List<ProductCertificate> list = productCertificateService.selectExpiringCertificateList(productCertificate);
        return getDataTable(list);
    }

    /**
     * 导出产品证件列表
     */
    @RequiresPermissions("certificate:product:export")
    @Log(title = "产品证件管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(ProductCertificate productCertificate)
    {
        List<ProductCertificate> list = productCertificateService.selectProductCertificateList(productCertificate);
        ExcelUtil<ProductCertificate> util = new ExcelUtil<ProductCertificate>(ProductCertificate.class);
        return util.exportExcel(list, "产品证件数据");
    }

    /**
     * 新增产品证件
     */
    @RequiresPermissions("certificate:product:add")
    @GetMapping("/add")
    public String add(ModelMap mmap)
    {
        // 查询所有供应商列表
        Supplier supplier = new Supplier();
        supplier.setStatus("0"); // 只查询启用状态的供应商
        List<Supplier> supplierList = supplierService.selectSupplierList(supplier);
        mmap.put("supplierList", supplierList);
        return prefix + "/add";
    }

    /**
     * 新增保存产品证件
     */
    @RequiresPermissions("certificate:product:add")
    @Log(title = "产品证件管理", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(@Validated ProductCertificate productCertificate,
                              @org.springframework.web.bind.annotation.RequestParam(required = false) String specification,
                              @org.springframework.web.bind.annotation.RequestParam(required = false) String model,
                              @org.springframework.web.bind.annotation.RequestParam(required = false) String unit,
                              @org.springframework.web.bind.annotation.RequestParam(required = false) String manufacturerName,
                              @org.springframework.web.bind.annotation.RequestParam(required = false) java.math.BigDecimal purchasePrice)
    {
        try
        {
            productCertificate.setCreateBy(getLoginName());
            // 将额外的产品信息传递给Service
            int result = productCertificateService.insertProductCertificate(productCertificate, 
                specification, model, unit, manufacturerName, purchasePrice);
            return toAjax(result);
        }
        catch (Exception e)
        {
            logger.error("保存产品证件失败", e);
            return error("保存失败：" + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }
    }

    /**
     * 修改产品证件
     */
    @RequiresPermissions("certificate:product:edit")
    @GetMapping("/edit/{certificateId}")
    public String edit(@PathVariable("certificateId") Long certificateId, ModelMap mmap)
    {
        ProductCertificate productCertificate = productCertificateService.selectProductCertificateById(certificateId);
        mmap.put("productCertificate", productCertificate);
        // 查询所有供应商列表
        Supplier supplier = new Supplier();
        supplier.setStatus("0"); // 只查询启用状态的供应商
        List<Supplier> supplierList = supplierService.selectSupplierList(supplier);
        mmap.put("supplierList", supplierList);
        // 如果materialId不为空，查询MaterialDict信息用于显示规格、型号、单位、生产厂家、采购价格
        if (productCertificate.getMaterialId() != null && productCertificate.getMaterialId() > 0)
        {
            com.scm.system.domain.MaterialDict materialDict = materialDictService.selectMaterialDictById(productCertificate.getMaterialId());
            if (materialDict != null)
            {
                mmap.put("materialDict", materialDict);
            }
        }
        return prefix + "/edit";
    }

    /**
     * 修改保存产品证件
     */
    @RequiresPermissions("certificate:product:edit")
    @Log(title = "产品证件管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(@Validated ProductCertificate productCertificate,
                              @org.springframework.web.bind.annotation.RequestParam(required = false) String specification,
                              @org.springframework.web.bind.annotation.RequestParam(required = false) String model,
                              @org.springframework.web.bind.annotation.RequestParam(required = false) String unit,
                              @org.springframework.web.bind.annotation.RequestParam(required = false) String manufacturerName,
                              @org.springframework.web.bind.annotation.RequestParam(required = false) java.math.BigDecimal purchasePrice)
    {
        try
        {
            productCertificate.setUpdateBy(getLoginName());
            // 将额外的产品信息传递给Service
            int result = productCertificateService.updateProductCertificate(productCertificate, 
                specification, model, unit, manufacturerName, purchasePrice);
            return toAjax(result);
        }
        catch (Exception e)
        {
            logger.error("保存产品证件失败", e);
            return error("保存失败：" + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }
    }

    /**
     * 删除产品证件
     */
    @RequiresPermissions("certificate:product:remove")
    @Log(title = "产品证件管理", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        return toAjax(productCertificateService.deleteProductCertificateByIds(ids));
    }

    /**
     * 审核产品证件
     */
    @RequiresPermissions("certificate:product:audit")
    @GetMapping("/audit/{certificateId}")
    public String audit(@PathVariable("certificateId") Long certificateId, ModelMap mmap)
    {
        ProductCertificate productCertificate = productCertificateService.selectProductCertificateById(certificateId);
        mmap.put("productCertificate", productCertificate);
        return prefix + "/audit";
    }

    /**
     * 审核保存产品证件
     */
    @RequiresPermissions("certificate:product:audit")
    @Log(title = "产品证件审核", businessType = BusinessType.UPDATE)
    @PostMapping("/audit")
    @ResponseBody
    public AjaxResult auditSave(ProductCertificate productCertificate)
    {
        productCertificate.setAuditBy(getLoginName());
        return toAjax(productCertificateService.auditProductCertificate(productCertificate));
    }

    /**
     * 检查并更新证件过期状态
     */
    @RequiresPermissions("certificate:product:edit")
    @PostMapping("/checkExpired")
    @ResponseBody
    public AjaxResult checkExpired()
    {
        productCertificateService.checkAndUpdateExpiredStatus();
        return success("证件过期状态检查完成");
    }
}

