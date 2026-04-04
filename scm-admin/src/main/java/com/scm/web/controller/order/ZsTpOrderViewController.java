package com.scm.web.controller.order;

import java.util.List;
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
 * 中设订单查看（明细与配送回查）
 */
@Controller
@RequestMapping("/order/zstp")
public class ZsTpOrderViewController extends BaseController
{
    private String prefix = "order/zstp";

    @Autowired
    private IDeliveryService deliveryService;

    @RequiresPermissions("order:order:view")
    @GetMapping("/detail/{zsOrderId}")
    public String detail(@PathVariable("zsOrderId") String zsOrderId, ModelMap mmap)
    {
        ZsTpOrder head = deliveryService.selectZsTpOrderById(zsOrderId);
        mmap.put("head", head);
        mmap.put("zsOrderId", zsOrderId);
        return prefix + "/detail";
    }

    @RequiresPermissions("order:order:list")
    @PostMapping("/detailList")
    @ResponseBody
    public TableDataInfo detailList(String zsOrderId)
    {
        List<ZsTpOrderDetail> list = deliveryService.selectZsTpOrderDetailListForView(zsOrderId);
        return getDataTable(list);
    }

    @RequiresPermissions("order:order:list")
    @PostMapping("/deliveriesByZs/{zsOrderId}")
    @ResponseBody
    public TableDataInfo deliveriesByZs(@PathVariable("zsOrderId") String zsOrderId)
    {
        List<Delivery> list = deliveryService.selectDeliveriesByZsOrderId(zsOrderId);
        return getDataTable(list);
    }

    @RequiresPermissions("order:order:list")
    @PostMapping("/detailDeliveryTraces")
    @ResponseBody
    public TableDataInfo detailDeliveryTraces(String zsOrderDetailId)
    {
        return getDataTable(deliveryService.selectTracesByZsOrderDetailId(zsOrderDetailId));
    }
}
