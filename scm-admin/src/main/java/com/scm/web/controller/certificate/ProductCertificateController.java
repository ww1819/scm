package com.scm.web.controller.certificate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import com.scm.common.core.controller.BaseController;
import com.scm.common.core.domain.AjaxResult;
import com.scm.common.core.page.TableDataInfo;
import com.scm.common.enums.BusinessType;
import com.scm.common.exception.ServiceException;
import com.scm.common.utils.StringUtils;
import com.scm.common.utils.poi.ExcelUtil;
import com.scm.system.domain.HospitalSupplier;
import com.scm.system.domain.MaterialDict;
import com.scm.system.domain.ProductCertificate;
import com.scm.system.domain.Supplier;
import com.scm.system.domain.SupplierUser;
import com.scm.system.domain.vo.ProductMaterialArchiveVo;
import com.scm.system.service.IHospitalSupplierService;
import com.scm.system.service.ISupplierUserService;
import com.scm.system.service.IProductCertificateService;
import com.scm.system.service.IScmSupplierContextService;
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

    @Autowired
    private ISupplierUserService supplierUserService;

    @Autowired
    private IScmSupplierContextService scmSupplierContextService;

    @Autowired
    private IHospitalSupplierService hospitalSupplierService;

    /** 进入登记页：有「登记」菜单(view)或「证件查询」(list)任一即可，避免只配子按钮未配父权限时无法打开页面 */
    @RequiresPermissions(value = { "certificate:product:view", "certificate:product:list" }, logical = Logical.OR)
    @GetMapping()
    public String productCertificate()
    {
        return prefix + "/product";
    }

    /**
     * 供应商视角：按关联医院树维护该院下的产品档案及证件
     */
    @RequiresPermissions(value = { "certificate:product:view", "certificate:product:list" }, logical = Logical.OR)
    @GetMapping("/supplierHospital")
    public String supplierHospital(ModelMap mmap)
    {
        Long bindSid = scmSupplierContextService.resolveSupplierIdForUser(getUserId());
        mmap.put("supplierSelfService", bindSid != null);
        mmap.put("bindSupplierId", bindSid);
        return prefix + "/supplierHospital";
    }

    /**
     * 左侧医院树（根节点 + 当前供应商已关联且有效的医院）
     */
    @RequiresPermissions(value = { "certificate:product:view", "certificate:product:list" }, logical = Logical.OR)
    @GetMapping("/hospitalTreeData")
    @ResponseBody
    public List<Map<String, Object>> hospitalTreeData()
    {
        List<Map<String, Object>> nodes = new ArrayList<>();
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("id", 0L);
        root.put("pId", 0L);
        root.put("name", "关联医院");
        root.put("open", true);
        root.put("hospitalCode", "");
        root.put("nocheck", true);
        nodes.add(root);
        Long bindSid = scmSupplierContextService.resolveSupplierIdForUser(getUserId());
        if (bindSid == null)
        {
            return nodes;
        }
        List<HospitalSupplier> list = hospitalSupplierService.selectSupplierLinkedHospitalsForProduct(bindSid);
        if (list != null)
        {
            for (HospitalSupplier hs : list)
            {
                if (hs.getHospitalId() == null)
                {
                    continue;
                }
                Map<String, Object> n = new LinkedHashMap<>();
                n.put("id", hs.getHospitalId());
                n.put("pId", 0L);
                String code = hs.getHospitalCode();
                String name = hs.getHospitalName() != null ? hs.getHospitalName() : "医院";
                n.put("name", name + (StringUtils.isNotEmpty(code) ? " (" + code + ")" : ""));
                n.put("title", name);
                n.put("hospitalCode", code != null ? code : "");
                n.put("open", false);
                nodes.add(n);
            }
        }
        return nodes;
    }

    /**
     * 某医院下已存在产品证件的物资聚合列表（分页）
     */
    @RequiresPermissions(value = { "certificate:product:view", "certificate:product:list" }, logical = Logical.OR)
    @PostMapping("/materialArchiveList")
    @ResponseBody
    public TableDataInfo materialArchiveList(String hospitalCode)
    {
        Long bindSid = scmSupplierContextService.resolveSupplierIdForUser(getUserId());
        if (bindSid == null || StringUtils.isEmpty(hospitalCode))
        {
            return getDataTable(new ArrayList<ProductMaterialArchiveVo>());
        }
        try
        {
            productCertificateService.ensureProductMaterialArchiveAccess(hospitalCode.trim());
        }
        catch (ServiceException e)
        {
            return getDataTable(new ArrayList<ProductMaterialArchiveVo>());
        }
        startPage();
        List<ProductMaterialArchiveVo> list = productCertificateService.selectMaterialArchiveSummaryData(bindSid, hospitalCode.trim());
        return getDataTable(list);
    }

    /**
     * 医院 + 产品（物资）维度下的证件列表；未选产品时返回空表
     */
    @RequiresPermissions(value = { "certificate:product:view", "certificate:product:list" }, logical = Logical.OR)
    @PostMapping("/listForSelectedArchive")
    @ResponseBody
    public TableDataInfo listForSelectedArchive(ProductCertificate productCertificate)
    {
        Long bindSid = scmSupplierContextService.resolveSupplierIdForUser(getUserId());
        if (bindSid == null || productCertificate.getMaterialId() == null
            || StringUtils.isEmpty(productCertificate.getHospitalCode()))
        {
            return getDataTable(new ArrayList<ProductCertificate>());
        }
        startPage();
        List<ProductCertificate> list = productCertificateService.selectProductCertificateList(productCertificate);
        return getDataTable(list);
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
     * 查询产品证件列表：登记页用 view/list；审核页同样调本接口，需同时具备 audit 时才不报错（仅勾「产品证件审核」未勾登记菜单时常见）
     */
    @RequiresPermissions(value = { "certificate:product:view", "certificate:product:list", "certificate:product:audit" },
        logical = Logical.OR)
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ProductCertificate productCertificate)
    {
        startPage();
        List<ProductCertificate> list = productCertificateService.selectProductCertificateList(productCertificate);
        return getDataTable(list);
    }

    /**
     * 产品证件审核页左侧供应商列表（同审核权限域，避免跨模块取数失败）
     */
    @RequiresPermissions("certificate:product:audit")
    @GetMapping("/auditSuppliers")
    @ResponseBody
    public AjaxResult auditSuppliers()
    {
        Supplier supplier = new Supplier();
        SupplierUser supplierUser = supplierUserService.selectSupplierUserByUserId(getUserId());
        if (supplierUser != null && supplierUser.getSupplierId() != null)
        {
            supplier.setSupplierId(supplierUser.getSupplierId());
        }
        List<Supplier> list = supplierService.selectSupplierList(supplier);
        return success(list);
    }

    /**
     * 查询过期预警的产品证件列表（与列表接口一致：view 或 list）
     */
    @RequiresPermissions(value = { "certificate:product:view", "certificate:product:list" }, logical = Logical.OR)
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
    public String add(@RequestParam(value = "hospitalCode", required = false) String hospitalCode,
        @RequestParam(value = "materialId", required = false) Long materialId,
        ModelMap mmap)
    {
        mmap.put("preHospitalCode", hospitalCode != null ? hospitalCode : "");
        mmap.put("preMaterialDict", null);
        if (materialId != null)
        {
            MaterialDict d = materialDictService.selectMaterialDictById(materialId);
            mmap.put("preMaterialDict", d);
        }
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

    /**
     * 上传 / 维护产品证照图片（仅更新 certificate_file）
     */
    @RequiresPermissions("certificate:product:edit")
    @GetMapping("/upload/{certificateId}")
    public String uploadImages(@PathVariable("certificateId") Long certificateId, ModelMap mmap)
    {
        ProductCertificate productCertificate = null;
        try
        {
            productCertificate = productCertificateService.selectProductCertificateById(certificateId);
        }
        catch (ServiceException e)
        {
            mmap.put("uploadLoadError", e.getMessage());
        }
        if (productCertificate == null && !mmap.containsKey("uploadLoadError"))
        {
            mmap.put("uploadLoadError", "证件不存在。");
        }
        mmap.put("productCertificate", productCertificate);
        return prefix + "/productUploadImages";
    }

    /**
     * 保存产品证照图片
     */
    @RequiresPermissions("certificate:product:edit")
    @Log(title = "产品证件管理", businessType = BusinessType.UPDATE)
    @PostMapping("/updateCertificateFile")
    @ResponseBody
    public AjaxResult updateCertificateFile(Long certificateId, String certificateFile)
    {
        return toAjax(productCertificateService.updateProductCertificateFile(certificateId, certificateFile, getLoginName()));
    }
}

