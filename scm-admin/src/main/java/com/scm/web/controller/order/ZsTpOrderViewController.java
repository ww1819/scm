package com.scm.web.controller.order;

import java.util.List;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.scm.common.core.controller.BaseController;
import com.scm.common.core.page.TableDataInfo;
import com.scm.system.domain.Delivery;
import com.scm.system.domain.ZsTpOrder;
import com.scm.system.domain.ZsTpOrderDetail;
import com.scm.system.service.IDeliveryService;

/**
 * 第三方订单查看（明细与配送回查）
 */
@Controller
@RequestMapping("/order/zstp")
public class ZsTpOrderViewController extends BaseController
{
    private String prefix = "order/zstp";

    @Autowired
    private IDeliveryService deliveryService;

    @RequiresPermissions(value = { "order:order:view", "order:tpOrder:detail" }, logical = Logical.OR)
    @GetMapping("/detail/{zsOrderId}")
    public String detail(@PathVariable("zsOrderId") String zsOrderId, ModelMap mmap)
    {
        ZsTpOrder head = deliveryService.selectZsTpOrderHeadForView(zsOrderId);
        mmap.put("head", head);
        mmap.put("zsOrderId", zsOrderId);
        return prefix + "/detail";
    }

    @RequiresPermissions(value = { "order:order:view", "order:tpOrder:detail" }, logical = Logical.OR)
    @PostMapping("/detailList")
    @ResponseBody
    public TableDataInfo detailList(String zsOrderId)
    {
        deliveryService.assertZsTpOrderViewScope(zsOrderId);
        startPage();
        List<ZsTpOrderDetail> list = deliveryService.selectZsTpOrderDetailListForView(zsOrderId);
        return getDataTable(list);
    }

    @RequiresPermissions(value = { "order:order:view", "order:tpOrder:detail" }, logical = Logical.OR)
    @PostMapping("/deliveriesByZs/{zsOrderId}")
    @ResponseBody
    public TableDataInfo deliveriesByZs(@PathVariable("zsOrderId") String zsOrderId)
    {
        deliveryService.assertZsTpOrderViewScope(zsOrderId);
        List<Delivery> list = deliveryService.selectDeliveriesByZsOrderId(zsOrderId);
        return getDataTable(list);
    }

    @RequiresPermissions(value = { "order:order:view", "order:tpOrder:detail" }, logical = Logical.OR)
    @PostMapping("/detailDeliveryTraces")
    @ResponseBody
    public TableDataInfo detailDeliveryTraces(String zsOrderDetailId, String zsOrderId)
    {
        deliveryService.assertZsTpOrderViewScope(zsOrderId);
        return getDataTable(deliveryService.selectTracesByZsOrderDetailId(zsOrderDetailId));
    }
}
