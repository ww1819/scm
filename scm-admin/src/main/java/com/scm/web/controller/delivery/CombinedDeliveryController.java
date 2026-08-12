package com.scm.web.controller.delivery;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import org.apache.shiro.authz.annotation.Logical;
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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scm.common.annotation.Log;
import com.scm.common.core.controller.BaseController;
import com.scm.common.core.domain.AjaxResult;
import com.scm.common.core.page.TableDataInfo;
import com.scm.common.enums.BusinessType;
import com.scm.common.exception.ServiceException;
import com.scm.common.utils.StringUtils;
import com.scm.system.domain.CombinedDelivery;
import com.scm.system.domain.CombinedDeliveryDetail;
import com.scm.system.domain.DeliveryLineOpLog;
import com.scm.system.domain.Hospital;
import com.scm.system.domain.HospitalSupplier;
import com.scm.system.domain.Supplier;
import com.scm.system.domain.vo.CombinedOrderProgressVo;
import com.scm.system.service.ICombinedDeliveryService;
import com.scm.system.service.IDeliveryLineOpLogService;
import com.scm.system.service.IHospitalService;
import com.scm.system.service.IHospitalSupplierService;
import com.scm.system.service.IScmHospitalContextService;
import com.scm.system.service.IScmSupplierContextService;
import com.scm.system.service.ISupplierService;

/**
 * 合单配送
 */
@Controller
@RequestMapping("/delivery/combined")
public class CombinedDeliveryController extends BaseController
{
    private String prefix = "delivery";

    @Autowired
    private ICombinedDeliveryService combinedDeliveryService;
    @Autowired
    private IDeliveryLineOpLogService deliveryLineOpLogService;
    @Autowired
    private IHospitalService hospitalService;
    @Autowired
    private ISupplierService supplierService;
    @Autowired
    private IHospitalSupplierService hospitalSupplierService;
    @Autowired
    private IScmHospitalContextService scmHospitalContextService;
    @Autowired
    private IScmSupplierContextService scmSupplierContextService;

    @RequiresPermissions("delivery:combined:view")
    @GetMapping()
    public String combined()
    {
        return prefix + "/combined";
    }

    @RequiresPermissions("delivery:combined:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(CombinedDelivery query)
    {
        startPage();
        List<CombinedDelivery> list = combinedDeliveryService.selectList(query);
        return getDataTable(list);
    }

    @RequiresPermissions("delivery:combined:list")
    @PostMapping("/orderProgress")
    @ResponseBody
    public TableDataInfo orderProgress(CombinedDelivery query)
    {
        List<CombinedOrderProgressVo> list = combinedDeliveryService.selectOrderProgress(query);
        return getDataTable(list);
    }

    @RequiresPermissions(value = {"delivery:combined:add", "delivery:combined:edit"}, logical = Logical.OR)
    @PostMapping("/candidateLines")
    @ResponseBody
    public TableDataInfo candidateLines(CombinedDelivery query)
    {
        List<CombinedDeliveryDetail> list = combinedDeliveryService.selectCandidateLines(query);
        return getDataTable(list);
    }

    @RequiresPermissions("delivery:combined:add")
    @GetMapping("/add")
    public String add(ModelMap mmap)
    {
        mmap.put("combined", new CombinedDelivery());
        fillScopedHospitalSupplier(mmap);
        return prefix + "/combinedEdit";
    }

    @RequiresPermissions("delivery:combined:add")
    @Log(title = "合单配送", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(CombinedDelivery row,
            @RequestParam(value = "detailsJson", required = false) String detailsJson)
    {
        try
        {
            row.setDetails(parseDetailsJson(detailsJson));
        }
        catch (Exception e)
        {
            return error("解析合单明细失败：" + e.getMessage());
        }
        return toAjax(combinedDeliveryService.insert(row));
    }

    @RequiresPermissions("delivery:combined:edit")
    @GetMapping("/edit/{combinedId}")
    public String edit(@PathVariable("combinedId") String combinedId, ModelMap mmap)
    {
        CombinedDelivery row = combinedDeliveryService.selectById(combinedId);
        if (row == null)
        {
            throw new ServiceException("合单不存在");
        }
        if ("1".equals(row.getAuditStatus()))
        {
            throw new ServiceException("已审核合单不可修改");
        }
        mmap.put("combined", row);
        fillScopedHospitalSupplier(mmap);
        return prefix + "/combinedEdit";
    }

    @RequiresPermissions("delivery:combined:edit")
    @Log(title = "合单配送", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(CombinedDelivery row,
            @RequestParam(value = "detailsJson", required = false) String detailsJson)
    {
        try
        {
            row.setDetails(parseDetailsJson(detailsJson));
        }
        catch (Exception e)
        {
            return error("解析合单明细失败：" + e.getMessage());
        }
        return toAjax(combinedDeliveryService.update(row));
    }

    @RequiresPermissions("delivery:combined:view")
    @GetMapping("/view/{combinedId}")
    public String view(@PathVariable("combinedId") String combinedId, ModelMap mmap)
    {
        CombinedDelivery row = combinedDeliveryService.selectById(combinedId);
        mmap.put("combined", row);
        return prefix + "/combinedView";
    }

    @RequiresPermissions("delivery:combined:remove")
    @Log(title = "合单配送", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        return toAjax(combinedDeliveryService.deleteByIds(ids));
    }

    @RequiresPermissions("delivery:combined:audit")
    @Log(title = "合单审核", businessType = BusinessType.UPDATE)
    @PostMapping("/audit")
    @ResponseBody
    public AjaxResult audit(String ids)
    {
        String[] arr = ids.split(",");
        int n = 0;
        for (String id : arr)
        {
            if (StringUtils.isEmpty(id))
            {
                continue;
            }
            n += combinedDeliveryService.audit(id.trim(), getLoginName());
        }
        return toAjax(n);
    }

    @RequiresPermissions("delivery:combined:view")
    @GetMapping("/lineOpLog/{combinedId}")
    public String lineOpLog(@PathVariable("combinedId") String combinedId, ModelMap mmap)
    {
        mmap.put("billKind", DeliveryLineOpLog.KIND_COMBINED);
        mmap.put("billId", combinedId);
        mmap.put("listUrl", "/delivery/combined/lineOpLog/list");
        return prefix + "/lineOpLog";
    }

    @RequiresPermissions("delivery:combined:view")
    @PostMapping("/lineOpLog/list")
    @ResponseBody
    public TableDataInfo lineOpLogList(@RequestParam("billId") String billId)
    {
        List<DeliveryLineOpLog> list = deliveryLineOpLogService.selectByBill(DeliveryLineOpLog.KIND_COMBINED, billId);
        return getDataTable(list);
    }

    private List<CombinedDeliveryDetail> parseDetailsJson(String detailsJson) throws IOException
    {
        if (StringUtils.isEmpty(detailsJson))
        {
            return new ArrayList<>();
        }
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        objectMapper.setDateFormat(dateFormat);
        return objectMapper.readValue(detailsJson, new TypeReference<List<CombinedDeliveryDetail>>() {});
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
            List<Supplier> suppliers = new ArrayList<>();
            Set<Long> seen = new HashSet<>();
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
            List<Hospital> hospitals = new ArrayList<>();
            Set<Long> seen = new HashSet<>();
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
        mmap.put("hospitalList", hospitalService.selectHospitalList(hospitalQ));
        mmap.put("supplierList", supplierService.selectSupplierList(supplierQ));
    }
}
