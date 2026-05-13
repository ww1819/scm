package com.scm.web.controller.order;

import java.util.ArrayList;
import java.util.List;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
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
import com.scm.common.utils.ShiroUtils;
import com.scm.common.utils.StringUtils;
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

    @RequiresPermissions("order:tpOrder:confirm")
    @Log(title = "第三方订单批量确认", businessType = BusinessType.UPDATE)
    @PostMapping("/confirmBatch")
    @ResponseBody
    public AjaxResult confirmBatch(@RequestParam("ids") String ids)
    {
        return runTpOrderBatch(ids, true);
    }

    @RequiresPermissions("order:tpOrder:void")
    @Log(title = "第三方订单批量作废", businessType = BusinessType.UPDATE)
    @PostMapping("/voidBatch")
    @ResponseBody
    public AjaxResult voidBatch(@RequestParam("ids") String ids)
    {
        return runTpOrderBatch(ids, false);
    }

    private AjaxResult runTpOrderBatch(String ids, boolean confirm)
    {
        if (StringUtils.isEmpty(ids))
        {
            return AjaxResult.error("请至少选择一条订单");
        }
        String[] parts = ids.split(",");
        int ok = 0;
        List<String> fails = new ArrayList<>();
        for (String raw : parts)
        {
            String id = StringUtils.trim(raw);
            if (StringUtils.isEmpty(id))
            {
                continue;
            }
            try
            {
                if (confirm)
                {
                    deliveryService.confirmZsTpOrder(id);
                }
                else
                {
                    deliveryService.voidZsTpOrder(id);
                }
                ok++;
            }
            catch (ServiceException ex)
            {
                fails.add(id + "：" + ex.getMessage());
            }
            catch (Exception ex)
            {
                fails.add(id + "：处理失败");
            }
        }
        if (ok == 0 && fails.isEmpty())
        {
            return AjaxResult.error("未解析到有效订单主键");
        }
        String act = confirm ? "确认" : "作废";
        if (fails.isEmpty())
        {
            return AjaxResult.success("批量" + act + "成功，共 " + ok + " 条");
        }
        if (ok == 0)
        {
            return AjaxResult.error("批量" + act + "失败：" + summarizeFails(fails));
        }
        return AjaxResult.warn("成功" + act + " " + ok + " 条；失败 " + fails.size() + " 条：" + summarizeFails(fails));
    }

    private static String summarizeFails(List<String> fails)
    {
        int max = Math.min(5, fails.size());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < max; i++)
        {
            if (i > 0)
            {
                sb.append("；");
            }
            sb.append(fails.get(i));
        }
        if (fails.size() > max)
        {
            sb.append("…（共 ").append(fails.size()).append(" 条失败）");
        }
        return sb.toString();
    }
}
