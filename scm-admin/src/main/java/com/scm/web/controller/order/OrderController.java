package com.scm.web.controller.order;

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
import com.scm.system.domain.Hospital;
import com.scm.system.domain.Order;
import com.scm.system.domain.OrderDetail;
import com.scm.system.domain.Supplier;
import com.scm.system.service.IHospitalService;
import com.scm.system.service.IOrderService;
import com.scm.system.service.ISupplierService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 订单信息
 * 
 * @author scm
 */
@Controller
@RequestMapping("/order/order")
public class OrderController extends BaseController
{
    private String prefix = "order";

    @Autowired
    private IOrderService orderService;
    
    @Autowired
    private ISupplierService supplierService;
    
    @Autowired
    private IHospitalService hospitalService;

    @RequiresPermissions("order:order:view")
    @GetMapping()
    public String order()
    {
        return prefix + "/order";
    }

    /**
     * 订单选择页面
     */
    @GetMapping("/select")
    public String select()
    {
        return prefix + "/select";
    }

    /**
     * 查询订单列表
     */
    @RequiresPermissions("order:order:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(Order order)
    {
        startPage();
        List<Order> list = orderService.selectOrderList(order);
        return getDataTable(list);
    }

    /**
     * 导出订单列表
     */
    @RequiresPermissions("order:order:export")
    @Log(title = "订单管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(Order order)
    {
        List<Order> list = orderService.selectOrderList(order);
        ExcelUtil<Order> util = new ExcelUtil<Order>(Order.class);
        return util.exportExcel(list, "订单数据");
    }

    /**
     * 新增订单
     */
    @RequiresPermissions("order:order:add")
    @GetMapping("/add")
    public String add(ModelMap mmap)
    {
        // 查询所有供应商列表
        Supplier supplier = new Supplier();
        supplier.setStatus("0"); // 只查询启用状态的供应商
        List<Supplier> supplierList = supplierService.selectSupplierList(supplier);
        mmap.put("supplierList", supplierList);
        
        // 查询所有医院列表
        Hospital hospital = new Hospital();
        hospital.setStatus("0"); // 只查询启用状态的医院
        List<Hospital> hospitalList = hospitalService.selectHospitalList(hospital);
        mmap.put("hospitalList", hospitalList);
        
        return prefix + "/add";
    }

    /**
     * 新增保存订单
     */
    @RequiresPermissions("order:order:add")
    @Log(title = "订单管理", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(@Validated Order order, @RequestParam(value = "orderDetailsJson", required = false) String orderDetailsJson)
    {
        // 解析订单明细JSON
        if (orderDetailsJson != null && !orderDetailsJson.isEmpty())
        {
            try
            {
                ObjectMapper objectMapper = new ObjectMapper();
                List<OrderDetail> orderDetails = objectMapper.readValue(orderDetailsJson, 
                    new TypeReference<List<OrderDetail>>() {});
                order.setOrderDetails(orderDetails);
            }
            catch (Exception e)
            {
                return error("解析订单明细数据失败：" + e.getMessage());
            }
        }
        order.setCreateBy(getLoginName());
        return toAjax(orderService.insertOrder(order));
    }

    /**
     * 获取订单信息（用于AJAX调用）
     */
    @GetMapping("/{orderId}")
    @ResponseBody
    public AjaxResult getOrder(@PathVariable("orderId") Long orderId)
    {
        Order order = orderService.selectOrderById(orderId);
        return AjaxResult.success(order);
    }

    /**
     * 查看订单详情
     */
    @RequiresPermissions("order:order:view")
    @GetMapping("/view/{orderId}")
    public String view(@PathVariable("orderId") Long orderId, ModelMap mmap)
    {
        Order order = orderService.selectOrderById(orderId);
        mmap.put("order", order);
        return prefix + "/view";
    }

    /**
     * 查看订单明细
     */
    @RequiresPermissions("order:order:view")
    @GetMapping("/detail/{orderId}")
    public String detail(@PathVariable("orderId") Long orderId, ModelMap mmap)
    {
        Order order = orderService.selectOrderById(orderId);
        mmap.put("order", order);
        return prefix + "/detail";
    }

    /**
     * 修改订单
     */
    @RequiresPermissions("order:order:edit")
    @GetMapping("/edit/{orderId}")
    public String edit(@PathVariable("orderId") Long orderId, ModelMap mmap)
    {
        Order order = orderService.selectOrderById(orderId);
        mmap.put("order", order);
        // 查询所有供应商列表
        Supplier supplier = new Supplier();
        supplier.setStatus("0"); // 只查询启用状态的供应商
        List<Supplier> supplierList = supplierService.selectSupplierList(supplier);
        mmap.put("supplierList", supplierList);
        // 查询所有医院列表
        Hospital hospital = new Hospital();
        hospital.setStatus("0"); // 只查询启用状态的医院
        List<Hospital> hospitalList = hospitalService.selectHospitalList(hospital);
        mmap.put("hospitalList", hospitalList);
        return prefix + "/edit";
    }

    /**
     * 修改保存订单
     */
    @RequiresPermissions("order:order:edit")
    @Log(title = "订单管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(@Validated Order order, @RequestParam(value = "orderDetailsJson", required = false) String orderDetailsJson)
    {
        // 解析订单明细JSON
        if (orderDetailsJson != null && !orderDetailsJson.isEmpty())
        {
            try
            {
                ObjectMapper objectMapper = new ObjectMapper();
                List<OrderDetail> orderDetails = objectMapper.readValue(orderDetailsJson, 
                    new TypeReference<List<OrderDetail>>() {});
                order.setOrderDetails(orderDetails);
            }
            catch (Exception e)
            {
                return error("解析订单明细数据失败：" + e.getMessage());
            }
        }
        order.setUpdateBy(getLoginName());
        return toAjax(orderService.updateOrder(order));
    }

    /**
     * 删除订单
     */
    @RequiresPermissions("order:order:remove")
    @Log(title = "订单管理", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        return toAjax(orderService.deleteOrderByIds(ids));
    }


    /**
     * 接收订单
     */
    @RequiresPermissions("order:order:receive")
    @Log(title = "订单接收", businessType = BusinessType.UPDATE)
    @PostMapping("/receive")
    @ResponseBody
    public AjaxResult receive(String ids)
    {
        String[] orderIds = ids.split(",");
        int successCount = 0;
        for (String orderId : orderIds)
        {
            if (orderService.receiveOrder(Long.parseLong(orderId)) > 0)
            {
                successCount++;
            }
        }
        return successCount > 0 ? success("成功接收 " + successCount + " 个订单") : error("接收订单失败");
    }

    /**
     * 查询订单明细列表
     */
    @RequiresPermissions("order:order:list")
    @PostMapping("/detailList")
    @ResponseBody
    public TableDataInfo detailList(Long orderId)
    {
        List<OrderDetail> list = orderService.selectOrderDetailListByOrderId(orderId);
        return getDataTable(list);
    }
}

