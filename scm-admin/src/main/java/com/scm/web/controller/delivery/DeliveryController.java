package com.scm.web.controller.delivery;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scm.common.annotation.Log;
import com.scm.common.core.controller.BaseController;
import com.scm.common.core.domain.AjaxResult;
import com.scm.common.core.page.TableDataInfo;
import com.scm.common.enums.BusinessType;
import com.scm.common.utils.poi.ExcelUtil;
import com.scm.system.domain.Delivery;
import com.scm.system.domain.DeliveryDetail;
import com.scm.system.domain.Hospital;
import com.scm.system.domain.Order;
import com.scm.system.domain.Supplier;
import com.scm.system.service.IDeliveryService;
import com.scm.system.service.IHospitalService;
import com.scm.system.service.ISupplierService;

/**
 * 配送单信息
 * 
 * @author scm
 */
@Controller
@RequestMapping("/delivery/delivery")
public class DeliveryController extends BaseController
{
    private String prefix = "delivery";

    @Autowired
    private IDeliveryService deliveryService;
    
    @Autowired
    private IHospitalService hospitalService;
    
    @Autowired
    private ISupplierService supplierService;

    @RequiresPermissions("delivery:delivery:view")
    @GetMapping()
    public String delivery()
    {
        return prefix + "/delivery";
    }

    /**
     * 查询配送单列表
     */
    @RequiresPermissions("delivery:delivery:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(Delivery delivery)
    {
        startPage();
        List<Delivery> list = deliveryService.selectDeliveryList(delivery);
        return getDataTable(list);
    }

    /**
     * 导出配送单列表
     */
    @RequiresPermissions("delivery:delivery:export")
    @Log(title = "配送单管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(Delivery delivery)
    {
        List<Delivery> list = deliveryService.selectDeliveryList(delivery);
        ExcelUtil<Delivery> util = new ExcelUtil<Delivery>(Delivery.class);
        return util.exportExcel(list, "配送单数据");
    }

    /**
     * 新增配送单
     */
    @RequiresPermissions("delivery:delivery:add")
    @GetMapping("/add")
    public String add(ModelMap mmap)
    {
        // 查询所有医院列表
        Hospital hospital = new Hospital();
        hospital.setStatus("0"); // 只查询启用状态的医院
        List<Hospital> hospitalList = hospitalService.selectHospitalList(hospital);
        mmap.put("hospitalList", hospitalList);
        // 查询所有供应商列表
        Supplier supplier = new Supplier();
        supplier.setStatus("0"); // 只查询启用状态的供应商
        List<Supplier> supplierList = supplierService.selectSupplierList(supplier);
        mmap.put("supplierList", supplierList);
        return prefix + "/add";
    }

    /**
     * 引用订单
     */
    @RequiresPermissions("delivery:delivery:add")
    @GetMapping("/selectOrder/{orderId}")
    @ResponseBody
    public AjaxResult selectOrder(@PathVariable("orderId") Long orderId)
    {
        Order order = deliveryService.selectOrderForDelivery(orderId);
        return AjaxResult.success(order);
    }

    /**
     * 新增保存配送单
     */
    @RequiresPermissions("delivery:delivery:add")
    @Log(title = "配送单管理", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(@Validated Delivery delivery, @RequestParam(value = "deliveryDetailsJson", required = false) String deliveryDetailsJson)
    {
        // 解析配送明细JSON
        if (deliveryDetailsJson != null && !deliveryDetailsJson.isEmpty())
        {
            try
            {
                ObjectMapper objectMapper = new ObjectMapper();
                List<DeliveryDetail> deliveryDetails = objectMapper.readValue(deliveryDetailsJson, 
                    new TypeReference<List<DeliveryDetail>>() {});
                delivery.setDeliveryDetails(deliveryDetails);
            }
            catch (Exception e)
            {
                return error("解析配送明细数据失败：" + e.getMessage());
            }
        }
        delivery.setCreateBy(getLoginName());
        return toAjax(deliveryService.insertDelivery(delivery));
    }

    /**
     * 查看配送单详情
     */
    @RequiresPermissions("delivery:delivery:view")
    @GetMapping("/view/{deliveryId}")
    public String view(@PathVariable("deliveryId") Long deliveryId, ModelMap mmap)
    {
        Delivery delivery = deliveryService.selectDeliveryById(deliveryId);
        mmap.put("delivery", delivery);
        return prefix + "/view";
    }

    /**
     * 修改配送单
     */
    @RequiresPermissions("delivery:delivery:edit")
    @GetMapping("/edit/{deliveryId}")
    public String edit(@PathVariable("deliveryId") Long deliveryId, ModelMap mmap)
    {
        Delivery delivery = deliveryService.selectDeliveryById(deliveryId);
        mmap.put("delivery", delivery);
        // 查询所有医院列表
        Hospital hospital = new Hospital();
        hospital.setStatus("0"); // 只查询启用状态的医院
        List<Hospital> hospitalList = hospitalService.selectHospitalList(hospital);
        mmap.put("hospitalList", hospitalList);
        // 查询所有供应商列表
        Supplier supplier = new Supplier();
        supplier.setStatus("0"); // 只查询启用状态的供应商
        List<Supplier> supplierList = supplierService.selectSupplierList(supplier);
        mmap.put("supplierList", supplierList);
        return prefix + "/edit";
    }

    /**
     * 修改保存配送单
     */
    @RequiresPermissions("delivery:delivery:edit")
    @Log(title = "配送单管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(@Validated Delivery delivery, @RequestParam(value = "deliveryDetailsJson", required = false) String deliveryDetailsJson)
    {
        // 解析配送明细JSON
        if (deliveryDetailsJson != null && !deliveryDetailsJson.isEmpty())
        {
            try
            {
                ObjectMapper objectMapper = new ObjectMapper();
                List<DeliveryDetail> deliveryDetails = objectMapper.readValue(deliveryDetailsJson, 
                    new TypeReference<List<DeliveryDetail>>() {});
                delivery.setDeliveryDetails(deliveryDetails);
            }
            catch (Exception e)
            {
                return error("解析配送明细数据失败：" + e.getMessage());
            }
        }
        delivery.setUpdateBy(getLoginName());
        return toAjax(deliveryService.updateDelivery(delivery));
    }

    /**
     * 删除配送单
     */
    @RequiresPermissions("delivery:delivery:remove")
    @Log(title = "配送单管理", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        return toAjax(deliveryService.deleteDeliveryByIds(ids));
    }

    /**
     * 查看配送单详情
     */
    @RequiresPermissions("delivery:delivery:detail")
    @GetMapping("/detail/{deliveryId}")
    public String detail(@PathVariable("deliveryId") Long deliveryId, ModelMap mmap)
    {
        Delivery delivery = deliveryService.selectDeliveryById(deliveryId);
        mmap.put("delivery", delivery);
        return prefix + "/detail";
    }

    /**
     * 审核配送单
     */
    @RequiresPermissions("delivery:delivery:audit")
    @Log(title = "配送单审核", businessType = BusinessType.UPDATE)
    @PostMapping("/audit")
    @ResponseBody
    public AjaxResult audit(String ids)
    {
        String[] deliveryIds = ids.split(",");
        int successCount = 0;
        for (String deliveryId : deliveryIds)
        {
            if (deliveryService.auditDelivery(Long.parseLong(deliveryId)) > 0)
            {
                successCount++;
            }
        }
        return successCount > 0 ? success("成功审核 " + successCount + " 个配送单") : error("审核配送单失败");
    }

    /**
     * 打印配送单
     */
    @RequiresPermissions("delivery:delivery:print")
    @GetMapping("/print/{deliveryId}")
    public String print(@PathVariable("deliveryId") Long deliveryId, ModelMap mmap)
    {
        Delivery delivery = deliveryService.selectDeliveryById(deliveryId);
        // 配送明细已经通过selectDeliveryById方法加载到delivery对象中
        List<DeliveryDetail> details = delivery.getDeliveryDetails();
        mmap.put("delivery", delivery);
        mmap.put("deliveryDetails", details);
        
        // 计算合计数量
        int totalQuantity = 0;
        if (details != null && !details.isEmpty())
        {
            for (DeliveryDetail detail : details)
            {
                if (detail.getDeliveryQuantity() != null)
                {
                    totalQuantity += detail.getDeliveryQuantity().intValue();
                }
            }
        }
        mmap.put("totalQuantity", totalQuantity);
        
        return prefix + "/print";
    }

    /**
     * 查询配送明细列表
     */
    @RequiresPermissions("delivery:delivery:list")
    @PostMapping("/detailList")
    @ResponseBody
    public TableDataInfo detailList(Long deliveryId)
    {
        List<DeliveryDetail> list = deliveryService.selectDeliveryDetailListByDeliveryId(deliveryId);
        return getDataTable(list);
    }

    /**
     * 配送信息查询页面（包含明细表和汇总表）
     */
    @RequiresPermissions("delivery:delivery:view")
    @GetMapping("/query")
    public String query()
    {
        return prefix + "/query";
    }

    /**
     * 配送明细表查询页面（保留用于兼容）
     */
    @RequiresPermissions("delivery:delivery:view")
    @GetMapping("/query/detail")
    public String queryDetail()
    {
        return prefix + "/query/detail";
    }

    /**
     * 查询配送明细表数据
     */
    @RequiresPermissions("delivery:delivery:list")
    @PostMapping("/query/detail/list")
    @ResponseBody
    public TableDataInfo queryDetailList(DeliveryDetail deliveryDetail)
    {
        startPage();
        List<DeliveryDetail> list = deliveryService.selectDeliveryDetailList(deliveryDetail);
        return getDataTable(list);
    }

    /**
     * 导出配送明细表
     */
    @RequiresPermissions("delivery:delivery:export")
    @Log(title = "配送明细表", businessType = BusinessType.EXPORT)
    @PostMapping("/query/detail/export")
    @ResponseBody
    public AjaxResult exportDetail(DeliveryDetail deliveryDetail)
    {
        List<DeliveryDetail> list = deliveryService.selectDeliveryDetailList(deliveryDetail);
        ExcelUtil<DeliveryDetail> util = new ExcelUtil<DeliveryDetail>(DeliveryDetail.class);
        return util.exportExcel(list, "配送明细表");
    }

    /**
     * 配送汇总表查询页面
     */
    @RequiresPermissions("delivery:delivery:view")
    @GetMapping("/query/summary")
    public String querySummary()
    {
        return prefix + "/query/summary";
    }

    /**
     * 查询配送汇总表数据
     */
    @RequiresPermissions("delivery:delivery:list")
    @PostMapping("/query/summary/list")
    @ResponseBody
    public TableDataInfo querySummaryList(Delivery delivery)
    {
        startPage();
        List<Delivery> list = deliveryService.selectDeliveryList(delivery);
        return getDataTable(list);
    }

    /**
     * 导出配送汇总表
     */
    @RequiresPermissions("delivery:delivery:export")
    @Log(title = "配送汇总表", businessType = BusinessType.EXPORT)
    @PostMapping("/query/summary/export")
    @ResponseBody
    public AjaxResult exportSummary(Delivery delivery)
    {
        List<Delivery> list = deliveryService.selectDeliveryList(delivery);
        ExcelUtil<Delivery> util = new ExcelUtil<Delivery>(Delivery.class);
        return util.exportExcel(list, "配送汇总表");
    }
}

