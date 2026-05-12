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
import com.scm.system.domain.ZsTpOrder;
import com.scm.system.service.IDeliveryService;

/**
 * 第三方订单查询（只读：列表 + 详情）
 */
@Controller
@RequestMapping("/order/tpQuery")
public class TpOrderQueryController extends BaseController
{
    private static final String PREFIX = "order/zstp";

    @Autowired
    private IDeliveryService deliveryService;

    @RequiresPermissions("order:tpOrder:view")
    @GetMapping()
    public String index()
    {
        return "order/tpQuery";
    }

    @RequiresPermissions("order:tpOrder:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ZsTpOrder query)
    {
        startPage();
        List<ZsTpOrder> list = deliveryService.selectZsTpOrderQueryList(query);
        return getDataTable(list);
    }

    @RequiresPermissions("order:tpOrder:detail")
    @GetMapping("/detail/{zsOrderId}")
    public String detail(@PathVariable("zsOrderId") String zsOrderId, ModelMap mmap)
    {
        ZsTpOrder head = deliveryService.selectZsTpOrderHeadForView(zsOrderId);
        mmap.put("head", head);
        mmap.put("zsOrderId", zsOrderId);
        return PREFIX + "/detail";
    }
}
