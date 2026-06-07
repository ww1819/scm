package com.scm.web.controller.delivery;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scm.common.annotation.Log;
import com.scm.common.core.controller.BaseController;
import com.scm.common.exception.ServiceException;
import com.scm.common.utils.ShiroUtils;
import com.scm.common.utils.StringUtils;
import com.scm.common.core.domain.AjaxResult;
import com.scm.common.core.page.TableDataInfo;
import com.scm.common.enums.BusinessType;
import com.scm.common.utils.DateUtils;
import com.scm.common.utils.file.FileUtils;
import com.scm.common.utils.poi.ExcelUtil;
import com.scm.system.constants.ScmPrintPageType;
import com.scm.system.domain.Delivery;
import com.scm.system.domain.DeliveryDetail;
import com.scm.system.domain.DeliveryDownloadLog;
import com.scm.system.domain.Hospital;
import com.scm.system.domain.HospitalSupplier;
import com.scm.system.domain.Order;
import com.scm.system.domain.Supplier;
import com.scm.system.domain.ZsTpOrder;
import com.scm.system.domain.vo.DeliveryPrintSheetVo;
import com.scm.system.domain.vo.PrintStyleVO;
import com.scm.system.service.IDeliveryService;
import com.scm.system.service.IHospitalSupplierService;
import com.scm.system.service.IHospitalService;
import com.scm.system.service.IScmHospitalContextService;
import com.scm.system.service.IScmUserPrintSettingService;
import com.scm.system.service.IScmSupplierContextService;
import com.scm.system.service.ISupplierService;
import com.scm.system.util.DeliveryAcceptancePrintStyleExcelBuilder;
import com.scm.system.util.DeliveryPrintStyleExcelBuilder;

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
    private IScmUserPrintSettingService scmUserPrintSettingService;

    @Autowired
    private IHospitalService hospitalService;

    @Autowired
    private ISupplierService supplierService;
    @Autowired
    private IScmHospitalContextService scmHospitalContextService;
    @Autowired
    private IScmSupplierContextService scmSupplierContextService;
    @Autowired
    private IHospitalSupplierService hospitalSupplierService;

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
    public void export(Delivery delivery, HttpServletResponse response)
    {
        List<Delivery> list = deliveryService.selectDeliveryList(delivery);
        ExcelUtil<Delivery> util = new ExcelUtil<Delivery>(Delivery.class);
        util.exportExcel(response, list, "配送单数据");
    }

    /**
     * 新增配送单
     */
    @RequiresPermissions("delivery:delivery:add")
    @GetMapping("/add")
    public String add(ModelMap mmap)
    {
        fillScopedHospitalSupplier(mmap);
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
     * 选择第三方订单（弹窗页）
     */
    @RequiresPermissions("delivery:delivery:add")
    @GetMapping("/zsOrder/select")
    public String zsOrderSelect()
    {
        return prefix + "/zsOrderSelect";
    }

    /**
     * 第三方订单列表（zs_tp_order）
     */
    @RequiresPermissions("delivery:delivery:list")
    @PostMapping("/zsOrder/list")
    @ResponseBody
    public TableDataInfo zsOrderList(ZsTpOrder query)
    {
        startPage();
        List<ZsTpOrder> list = deliveryService.selectZsTpOrderList(query);
        return getDataTable(list);
    }

    /**
     * 按第三方订单主键加载，用于填充配送单
     */
    @RequiresPermissions("delivery:delivery:add")
    @GetMapping("/selectZsOrder/{zsOrderId}")
    @ResponseBody
    public AjaxResult selectZsOrderForDelivery(@PathVariable("zsOrderId") String zsOrderId)
    {
        return AjaxResult.success(deliveryService.selectZsTpOrderForDelivery(zsOrderId));
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
                delivery.setDeliveryDetails(parseDeliveryDetailsJson(deliveryDetailsJson));
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
        deliveryService.assertDeliveryEditable(deliveryId);
        Delivery delivery = deliveryService.selectDeliveryById(deliveryId);
        mmap.put("delivery", delivery);
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

        mmap.put("hospitalList", hospitalService.selectHospitalList(hospitalQ));
        mmap.put("supplierList", supplierService.selectSupplierList(supplierQ));
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
                delivery.setDeliveryDetails(parseDeliveryDetailsJson(deliveryDetailsJson));
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
     * 解析前端提交的配送明细 JSON（忽略 bootstrap-table 复选框等未知字段）
     */
    private List<DeliveryDetail> parseDeliveryDetailsJson(String deliveryDetailsJson) throws IOException
    {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        objectMapper.setDateFormat(dateFormat);
        return objectMapper.readValue(deliveryDetailsJson, new TypeReference<List<DeliveryDetail>>() {});
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
            if (deliveryService.auditDelivery(Long.parseLong(deliveryId), getLoginName()) > 0)
            {
                successCount++;
            }
        }
        return successCount > 0 ? success("成功审核 " + successCount + " 个配送单") : error("审核配送单失败");
    }

    /**
     * 反审核配送单（已有接口下载记录时不允许）
     */
    @RequiresPermissions("delivery:delivery:unaudit")
    @Log(title = "配送单反审核", businessType = BusinessType.UPDATE)
    @PostMapping("/unAudit")
    @ResponseBody
    public AjaxResult unAudit(String ids)
    {
        String[] deliveryIds = ids.split(",");
        int successCount = 0;
        for (String deliveryId : deliveryIds)
        {
            if (deliveryService.unAuditDelivery(Long.parseLong(deliveryId.trim()), getLoginName()) > 0)
            {
                successCount++;
            }
        }
        return successCount > 0 ? success("成功反审核 " + successCount + " 个配送单") : error("反审核配送单失败");
    }

    /**
     * 配送单接口下载记录
     */
    @RequiresPermissions("delivery:delivery:detail")
    @GetMapping("/downloadLogList")
    @ResponseBody
    public AjaxResult downloadLogList(@RequestParam("deliveryId") Long deliveryId)
    {
        List<DeliveryDownloadLog> list = deliveryService.selectDeliveryDownloadLogList(deliveryId);
        return AjaxResult.success(list);
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

        // 计算合计数量、明细金额汇总（打印页与表头金额一致）
        int totalQuantity = 0;
        BigDecimal printTotalAmount = BigDecimal.ZERO;
        if (details != null && !details.isEmpty())
        {
            for (DeliveryDetail detail : details)
            {
                if (detail.getDeliveryQuantity() != null)
                {
                    totalQuantity += detail.getDeliveryQuantity().intValue();
                }
                if (detail.getAmount() != null)
                {
                    printTotalAmount = printTotalAmount.add(detail.getAmount());
                }
            }
        }
        mmap.put("totalQuantity", totalQuantity);
        mmap.put("printTotalAmount", printTotalAmount);

        // 打印页「输入码」与一维码内容（与模板原逻辑一致：单号长度>4 取末四位，否则整段）
        String printInputCode = "";
        String deliveryNo = delivery.getDeliveryNo();
        if (deliveryNo != null && !deliveryNo.isEmpty())
        {
            printInputCode = deliveryNo.length() > 4 ? deliveryNo.substring(deliveryNo.length() - 4) : deliveryNo;
        }
        mmap.put("printInputCode", printInputCode);

        putPrintStyleModel(mmap, ScmPrintPageType.DELIVERY);
        applyDeliveryDetailPages(mmap, details);
        return prefix + "/print";
    }

    /**
     * 保存当前用户打印版式设置（按打印类型），保存后前端刷新预览页生效
     */
    @RequiresPermissions("delivery:delivery:print")
    @PostMapping("/printSetting/save")
    @ResponseBody
    public AjaxResult savePrintSetting(@RequestParam("printType") String printType,
        @RequestParam("orientation") String orientation,
        @RequestParam("paperWidthMm") int paperWidthMm,
        @RequestParam("paperHeightMm") int paperHeightMm,
        @RequestParam("titleFontPx") int titleFontPx,
        @RequestParam("headerFooterFontPx") int headerFooterFontPx,
        @RequestParam("contentFontPx") int contentFontPx,
        @RequestParam("offsetXMm") int offsetXMm,
        @RequestParam("offsetYMm") int offsetYMm,
        @RequestParam("rowsPerPage") int rowsPerPage)
    {
        if (!ScmPrintPageType.isValid(printType))
        {
            return error("无效的打印类型");
        }
        scmUserPrintSettingService.saveOrUpdate(ShiroUtils.getUserId(), getLoginName(), printType, orientation,
            paperWidthMm, paperHeightMm, titleFontPx, headerFooterFontPx, contentFontPx, offsetXMm, offsetYMm,
            rowsPerPage);
        return success("已保存");
    }

    private void putPrintStyleModel(ModelMap mmap, String printType)
    {
        Long uid = null;
        try
        {
            uid = ShiroUtils.getUserId();
        }
        catch (Exception e)
        {
            logger.debug("print style: no shiro user, use defaults only");
        }
        mmap.put("printStyle", scmUserPrintSettingService.resolvePrintStyle(uid, printType));
        mmap.put("printPageType", printType);
    }

    /**
     * 按打印设置中的每页行数拆分明细，供模板分页输出
     */
    private void applyDeliveryDetailPages(ModelMap mmap, List<DeliveryDetail> details)
    {
        PrintStyleVO ps = (PrintStyleVO) mmap.get("printStyle");
        int rpp = ps != null ? ps.getRowsPerPage() : 10;
        List<List<DeliveryDetail>> pages = partitionDeliveryDetails(details, rpp);
        mmap.put("deliveryDetailPages", pages);
        mmap.put("printPageCount", pages.size());
    }

    private static List<List<DeliveryDetail>> partitionDeliveryDetails(List<DeliveryDetail> details, int rowsPerPage)
    {
        int n = Math.max(1, rowsPerPage);
        List<DeliveryDetail> list = details == null ? Collections.emptyList() : details;
        if (list.isEmpty())
        {
            return Collections.singletonList(new ArrayList<>());
        }
        List<List<DeliveryDetail>> pages = new ArrayList<>();
        for (int i = 0; i < list.size(); i += n)
        {
            pages.add(new ArrayList<>(list.subList(i, Math.min(i + n, list.size()))));
        }
        return pages;
    }

    /**
     * 医用耗材质量验收单打印（A4）
     */
    @RequiresPermissions("delivery:delivery:print")
    @GetMapping("/printAcceptance/{deliveryId}")
    public String printAcceptance(@PathVariable("deliveryId") Long deliveryId, ModelMap mmap)
    {
        Delivery delivery = deliveryService.selectDeliveryById(deliveryId);
        List<DeliveryDetail> details = delivery.getDeliveryDetails();
        if (details == null)
        {
            details = java.util.Collections.emptyList();
        }
        mmap.put("delivery", delivery);
        mmap.put("deliveryDetails", details);

        BigDecimal printTotalAmount = BigDecimal.ZERO;
        if (details != null && !details.isEmpty())
        {
            for (DeliveryDetail detail : details)
            {
                if (detail.getAmount() != null)
                {
                    printTotalAmount = printTotalAmount.add(detail.getAmount());
                }
            }
        }
        mmap.put("printTotalAmount", printTotalAmount);

        putPrintStyleModel(mmap, ScmPrintPageType.ACCEPTANCE);
        applyDeliveryDetailPages(mmap, details);
        return prefix + "/printAcceptance";
    }

    /** 单次「打印样式」Excel 导出最多配送单数 */
    private static final int PRINT_EXPORT_MAX_SHEETS = 300;

    /**
     * 按打印版式导出 Excel（每单一个 Sheet）。exportMode=selected 时须传 ids；exportMode=all 时按查询条件导出全部（不分页）。
     */
    @RequiresPermissions("delivery:delivery:export")
    @Log(title = "配送单打印样式Excel导出", businessType = BusinessType.EXPORT)
    @PostMapping("/printExportExcel")
    public void printExportExcel(@RequestParam("exportMode") String exportMode,
        @RequestParam(value = "ids", required = false) String ids,
        Delivery delivery,
        HttpServletResponse response) throws IOException
    {
        List<DeliveryPrintSheetVo> sheets = resolveDeliveryPrintSheets(exportMode, ids, delivery);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        try
        {
            FileUtils.setAttachmentResponseHeader(response, "配送单打印样式_" + DateUtils.dateTimeNow() + ".xlsx");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new ServiceException("导出失败：文件名编码异常");
        }
        DeliveryPrintStyleExcelBuilder.write(sheets, response.getOutputStream());
    }

    /**
     * 按「医用耗材质量验收单」打印版式导出 Excel（每单一个 Sheet）。参数与 {@link #printExportExcel} 相同。
     */
    @RequiresPermissions("delivery:delivery:export")
    @Log(title = "配送单验收单样式Excel导出", businessType = BusinessType.EXPORT)
    @PostMapping("/printAcceptanceExportExcel")
    public void printAcceptanceExportExcel(@RequestParam("exportMode") String exportMode,
        @RequestParam(value = "ids", required = false) String ids,
        Delivery delivery,
        HttpServletResponse response) throws IOException
    {
        List<DeliveryPrintSheetVo> sheets = resolveDeliveryPrintSheets(exportMode, ids, delivery);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        try
        {
            FileUtils.setAttachmentResponseHeader(response, "配送单验收单样式_" + DateUtils.dateTimeNow() + ".xlsx");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new ServiceException("导出失败：文件名编码异常");
        }
        DeliveryAcceptancePrintStyleExcelBuilder.write(sheets, response.getOutputStream());
    }

    private List<DeliveryPrintSheetVo> resolveDeliveryPrintSheets(String exportMode, String ids, Delivery delivery)
    {
        List<DeliveryPrintSheetVo> sheets = new ArrayList<>();
        if ("selected".equalsIgnoreCase(StringUtils.trim(exportMode)))
        {
            if (StringUtils.isEmpty(ids))
            {
                throw new ServiceException("请先勾选要导出的配送单");
            }
            LinkedHashSet<Long> idSet = new LinkedHashSet<>();
            for (String p : ids.split(","))
            {
                String t = StringUtils.trim(p);
                if (StringUtils.isEmpty(t))
                {
                    continue;
                }
                try
                {
                    idSet.add(Long.parseLong(t));
                }
                catch (NumberFormatException ex)
                {
                    throw new ServiceException("配送单ID格式不正确：" + t);
                }
            }
            if (idSet.isEmpty())
            {
                throw new ServiceException("请先勾选要导出的配送单");
            }
            for (Long deliveryId : idSet)
            {
                DeliveryPrintSheetVo sheet = buildDeliveryPrintSheet(deliveryId);
                if (sheet != null)
                {
                    sheets.add(sheet);
                }
            }
            if (sheets.isEmpty())
            {
                throw new ServiceException("未找到可导出的配送单，请确认已勾选且有权访问");
            }
        }
        else if ("all".equalsIgnoreCase(StringUtils.trim(exportMode)))
        {
            clearPage();
            List<Delivery> rows = deliveryService.selectDeliveryList(delivery);
            LinkedHashSet<Long> seen = new LinkedHashSet<>();
            if (rows != null)
            {
                for (Delivery row : rows)
                {
                    if (row == null || row.getDeliveryId() == null)
                    {
                        continue;
                    }
                    if (!seen.add(row.getDeliveryId()))
                    {
                        continue;
                    }
                    DeliveryPrintSheetVo sheet = buildDeliveryPrintSheet(row.getDeliveryId());
                    if (sheet != null)
                    {
                        sheets.add(sheet);
                    }
                }
            }
            if (sheets.isEmpty())
            {
                throw new ServiceException("当前搜索条件下没有可导出的配送单");
            }
        }
        else
        {
            throw new ServiceException("无效的导出方式");
        }
        if (sheets.size() > PRINT_EXPORT_MAX_SHEETS)
        {
            throw new ServiceException("单次最多导出 " + PRINT_EXPORT_MAX_SHEETS + " 张配送单，请缩小筛选范围或分批导出");
        }
        return sheets;
    }

    private DeliveryPrintSheetVo buildDeliveryPrintSheet(Long deliveryId)
    {
        try
        {
            Delivery delivery = deliveryService.selectDeliveryById(deliveryId);
            if (delivery == null)
            {
                return null;
            }
            List<DeliveryDetail> details = delivery.getDeliveryDetails();
            DeliveryPrintSheetVo vo = new DeliveryPrintSheetVo();
            vo.setDelivery(delivery);
            vo.setDeliveryDetails(details);

            int totalQuantity = 0;
            BigDecimal printTotalAmount = BigDecimal.ZERO;
            if (details != null && !details.isEmpty())
            {
                for (DeliveryDetail detail : details)
                {
                    if (detail.getDeliveryQuantity() != null)
                    {
                        totalQuantity += detail.getDeliveryQuantity().intValue();
                    }
                    if (detail.getAmount() != null)
                    {
                        printTotalAmount = printTotalAmount.add(detail.getAmount());
                    }
                }
            }
            vo.setTotalQuantity(totalQuantity);
            vo.setPrintTotalAmount(printTotalAmount);

            String printInputCode = "";
            String deliveryNo = delivery.getDeliveryNo();
            if (deliveryNo != null && !deliveryNo.isEmpty())
            {
                printInputCode = deliveryNo.length() > 4 ? deliveryNo.substring(deliveryNo.length() - 4) : deliveryNo;
            }
            vo.setPrintInputCode(printInputCode);
            return vo;
        }
        catch (ServiceException ex)
        {
            return null;
        }
    }

    /**
     * 查询配送明细列表（与查看配送单详情页权限一致，避免仅有 detail 无 list 时表格一直加载）
     */
    @RequiresPermissions("delivery:delivery:detail")
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
    public void exportDetail(DeliveryDetail deliveryDetail, HttpServletResponse response)
    {
        List<DeliveryDetail> list = deliveryService.selectDeliveryDetailList(deliveryDetail);
        ExcelUtil<DeliveryDetail> util = new ExcelUtil<DeliveryDetail>(DeliveryDetail.class);
        util.exportExcel(response, list, "配送明细表");
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
    public void exportSummary(Delivery delivery, HttpServletResponse response)
    {
        List<Delivery> list = deliveryService.selectDeliveryList(delivery);
        ExcelUtil<Delivery> util = new ExcelUtil<Delivery>(Delivery.class);
        util.exportExcel(response, list, "配送汇总表");
    }
}

