package com.scm.web.controller.order;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
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
import com.scm.system.domain.Delivery;
import com.scm.system.domain.Hospital;
import com.scm.system.domain.HospitalSupplier;
import com.scm.system.domain.Order;
import com.scm.system.domain.OrderDetail;
import com.scm.system.domain.Supplier;
import com.scm.system.service.IDeliveryService;
import com.scm.system.service.IHospitalSupplierService;
import com.scm.system.service.IHospitalService;
import com.scm.system.service.IOrderService;
import com.scm.system.service.IScmHospitalContextService;
import com.scm.system.service.IScmSupplierContextService;
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

    @Autowired
    private IDeliveryService deliveryService;
    @Autowired
    private IScmHospitalContextService scmHospitalContextService;
    @Autowired
    private IScmSupplierContextService scmSupplierContextService;
    @Autowired
    private IHospitalSupplierService hospitalSupplierService;

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
    public void export(Order order, HttpServletResponse response)
    {
        List<Order> list = orderService.selectOrderList(order);
        ExcelUtil<Order> util = new ExcelUtil<Order>(Order.class);
        util.exportExcel(response, list, "订单数据");
    }

    /**
     * 新增订单
     */
    @RequiresPermissions("order:order:add")
    @GetMapping("/add")
    public String add(ModelMap mmap)
    {
        fillScopedHospitalSupplier(mmap);
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
        fillScopedHospitalSupplier(mmap);
        return prefix + "/edit";
    }

    private void fillScopedHospitalSupplier(ModelMap mmap)
    {
        Long userId = getUserId();
        Long hospitalCtx = scmHospitalContextService.resolveHospitalIdForUser(userId);
        Long supplierCtx = scmSupplierContextService.resolveSupplierIdForUser(userId);

        Hospital hospitalQ = new Hospital();
        hospitalQ.setStatus("0");
        Supplier supplierQ = new Supplier();
        supplierQ.setStatus("0");

        if (hospitalCtx != null)
        {
            hospitalQ.setHospitalId(hospitalCtx);
            mmap.put("hospitalList", hospitalService.selectHospitalList(hospitalQ));

            HospitalSupplier relQ = new HospitalSupplier();
            relQ.setHospitalId(hospitalCtx);
            List<HospitalSupplier> rels = hospitalSupplierService.selectHospitalSupplierList(relQ);
            java.util.List<Supplier> suppliers = new java.util.ArrayList<>();
            java.util.Set<Long> seen = new java.util.HashSet<>();
            for (HospitalSupplier rel : rels)
            {
                if (rel.getSupplierId() != null && seen.add(rel.getSupplierId()))
                {
                    Supplier s = supplierService.selectSupplierById(rel.getSupplierId());
                    if (s != null && "0".equals(s.getStatus()))
                    {
                        suppliers.add(s);
                    }
                }
            }
            mmap.put("supplierList", suppliers);
            return;
        }

        if (supplierCtx != null)
        {
            supplierQ.setSupplierId(supplierCtx);
            mmap.put("supplierList", supplierService.selectSupplierList(supplierQ));

            HospitalSupplier relQ = new HospitalSupplier();
            relQ.setSupplierId(supplierCtx);
            List<HospitalSupplier> rels = hospitalSupplierService.selectHospitalSupplierList(relQ);
            java.util.List<Hospital> hospitals = new java.util.ArrayList<>();
            java.util.Set<Long> seen = new java.util.HashSet<>();
            for (HospitalSupplier rel : rels)
            {
                if (rel.getHospitalId() != null && seen.add(rel.getHospitalId()))
                {
                    Hospital h = hospitalService.selectHospitalById(rel.getHospitalId());
                    if (h != null && "0".equals(h.getStatus()))
                    {
                        hospitals.add(h);
                    }
                }
            }
            mmap.put("hospitalList", hospitals);
            return;
        }

        mmap.put("supplierList", supplierService.selectSupplierList(supplierQ));
        mmap.put("hospitalList", hospitalService.selectHospitalList(hospitalQ));
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
     * 作废订单（已作废后不可再引用生成配送单）
     */
    @RequiresPermissions("order:order:void")
    @Log(title = "订单作废", businessType = BusinessType.UPDATE)
    @PostMapping("/void")
    @ResponseBody
    public AjaxResult voidOrder(String ids)
    {
        String[] orderIds = ids.split(",");
        int successCount = 0;
        for (String orderId : orderIds)
        {
            if (orderService.voidOrder(Long.parseLong(orderId.trim()), getLoginName()) > 0)
            {
                successCount++;
            }
        }
        return successCount > 0 ? success("成功作废 " + successCount + " 个订单") : error("作废订单失败");
    }

    /**
     * 查询订单明细列表（与订单查看页一致）
     */
    @RequiresPermissions("order:order:view")
    @PostMapping("/detailList")
    @ResponseBody
    public TableDataInfo detailList(Long orderId)
    {
        List<OrderDetail> list = orderService.selectOrderDetailListByOrderId(orderId);
        return getDataTable(list);
    }

    /**
     * 本系统订单关联的配送单（用于订单查看页回查）
     */
    @RequiresPermissions("order:order:view")
    @PostMapping("/deliveriesByOrder/{orderId}")
    @ResponseBody
    public TableDataInfo deliveriesByOrder(@PathVariable("orderId") Long orderId)
    {
        List<Delivery> list = deliveryService.selectDeliveriesByOrderId(orderId);
        return getDataTable(list);
    }

    /**
     * 订单明细行关联的配送明细
     */
    @RequiresPermissions("order:order:view")
    @PostMapping("/detailDeliveryTraces")
    @ResponseBody
    public TableDataInfo detailDeliveryTraces(Long orderDetailId)
    {
        return getDataTable(deliveryService.selectTracesByScmOrderDetailId(orderDetailId));
    }
}

