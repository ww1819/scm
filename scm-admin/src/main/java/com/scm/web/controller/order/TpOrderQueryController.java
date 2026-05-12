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
import com.scm.common.core.domain.AjaxResult;
import com.scm.common.core.page.TableDataInfo;
import com.scm.common.utils.ShiroUtils;
import com.scm.system.domain.ZsTpOrder;
import com.scm.system.service.IDeliveryService;
import com.scm.system.service.IScmHospitalContextService;
import com.scm.system.service.IScmSupplierContextService;

/**
 * 第三方订单查询（列表、详情、确认、作废）
 */
@Controller
@RequestMapping("/order/tpQuery")
public class TpOrderQueryController extends BaseController
{
    private static final String PREFIX = "order/zstp";

    @Autowired
    private IDeliveryService deliveryService;

    @Autowired
    private IScmHospitalContextService scmHospitalContextService;

    @Autowired
    private IScmSupplierContextService scmSupplierContextService;

    @RequiresPermissions("order:tpOrder:view")
    @GetMapping()
    public String index(ModelMap mmap)
    {
        Long uid = ShiroUtils.getUserId();
        mmap.put("tpQuerySupplierCtx", scmSupplierContextService.resolveSupplierIdForUser(uid) != null);
        mmap.put("tpQueryHospitalCtx", scmHospitalContextService.resolveHospitalIdForUser(uid) != null);
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
        Long uid = ShiroUtils.getUserId();
        mmap.put("tpQuerySupplierCtx", scmSupplierContextService.resolveSupplierIdForUser(uid) != null);
        mmap.put("tpQueryHospitalCtx", scmHospitalContextService.resolveHospitalIdForUser(uid) != null);
        return PREFIX + "/detail";
    }

    @RequiresPermissions("order:tpOrder:confirm")
    @PostMapping("/confirm/{zsOrderId}")
    @ResponseBody
    public AjaxResult confirm(@PathVariable("zsOrderId") String zsOrderId)
    {
        deliveryService.confirmZsTpOrder(zsOrderId);
        return AjaxResult.success();
    }

    @RequiresPermissions("order:tpOrder:void")
    @PostMapping("/void/{zsOrderId}")
    @ResponseBody
    public AjaxResult voidOrder(@PathVariable("zsOrderId") String zsOrderId)
    {
        deliveryService.voidZsTpOrder(zsOrderId);
        return AjaxResult.success();
    }
}
