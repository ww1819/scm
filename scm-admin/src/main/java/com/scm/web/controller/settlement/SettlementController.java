package com.scm.web.controller.settlement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.scm.common.annotation.Log;
import com.scm.common.core.controller.BaseController;
import com.scm.common.core.domain.AjaxResult;
import com.scm.common.core.page.TableDataInfo;
import com.scm.common.enums.BusinessType;
import com.scm.common.utils.poi.ExcelUtil;
import com.scm.system.domain.Hospital;
import com.scm.system.domain.HospitalSupplier;
import com.scm.system.domain.ReconciliationYearMonthRow;
import com.scm.system.domain.Settlement;
import com.scm.system.domain.SettlementDetail;
import com.scm.system.service.IHospitalService;
import com.scm.system.service.IHospitalSupplierService;
import com.scm.system.service.ISettlementService;
import com.scm.system.service.IScmHospitalContextService;
import com.scm.system.service.IScmReconciliationService;
import com.scm.system.service.IScmSupplierContextService;

/**
 * 结算单信息
 * 
 * @author scm
 */
@Controller
@RequestMapping("/settlement/settlement")
public class SettlementController extends BaseController
{
    private String prefix = "settlement";

    @Autowired
    private ISettlementService settlementService;

    @Autowired
    private IHospitalService hospitalService;

    @Autowired
    private IScmReconciliationService scmReconciliationService;

    @Autowired
    private IScmSupplierContextService scmSupplierContextService;

    @Autowired
    private IScmHospitalContextService scmHospitalContextService;

    @Autowired
    private IHospitalSupplierService hospitalSupplierService;

    @RequiresPermissions("settlement:settlement:view")
    @GetMapping()
    public String settlement()
    {
        return prefix + "/settlement";
    }

    /**
     * 结算查询页（布局与配送信息查询一致，独立路由供菜单「结算查询」使用）
     */
    @RequiresPermissions("settlement:settlement:view")
    @GetMapping("/query")
    public String query()
    {
        return prefix + "/query";
    }

    /**
     * 对账表（年度按月汇总）
     */
    @RequiresPermissions("settlement:settlement:view")
    @GetMapping("/reconciliation")
    public String reconciliation(ModelMap mmap)
    {
        Long userId = getUserId();
        Long hospitalCtx = scmHospitalContextService.resolveHospitalIdForUser(userId);
        Long bindSupplierId = scmSupplierContextService.resolveSupplierIdForUser(userId);
        List<Hospital> hospitals = listHospitalsForReconciliation(hospitalCtx, bindSupplierId);
        mmap.put("hospitals", hospitals);
        mmap.put("bindSupplierId", bindSupplierId);
        mmap.put("bindHospitalId", hospitalCtx);

        Long preloadHospitalId = hospitalCtx;
        if (preloadHospitalId == null && hospitals != null && hospitals.size() == 1 && hospitals.get(0) != null)
        {
            preloadHospitalId = hospitals.get(0).getHospitalId();
        }
        List<Map<String, Object>> initialSuppliers = new ArrayList<>();
        if (preloadHospitalId != null)
        {
            initialSuppliers = scmReconciliationService.selectSupplierOptions(preloadHospitalId, bindSupplierId);
            if (hospitalCtx == null)
            {
                mmap.put("bindHospitalId", preloadHospitalId);
            }
        }
        mmap.put("initialSuppliers", initialSuppliers != null ? initialSuppliers : new ArrayList<>());
        return prefix + "/reconciliation";
    }

    /**
     * 对账表：供应商下拉（可按医院过滤；供应商账号仅返回绑定供应商）
     */
    @RequiresPermissions("settlement:settlement:view")
    @RequestMapping(value = "/reconciliation/suppliers", method = { RequestMethod.GET, RequestMethod.POST })
    @ResponseBody
    public AjaxResult reconciliationSuppliers(Long hospitalId)
    {
        hospitalId = resolveReconciliationHospitalId(hospitalId);
        Long bindSupplierId = scmSupplierContextService.resolveSupplierIdForUser(getUserId());
        List<Map<String, Object>> list = scmReconciliationService.selectSupplierOptions(hospitalId, bindSupplierId);
        return success(list);
    }

    /**
     * 对账表数据：按医院、年份、可选供应商返回 12 个月配送/结算汇总
     */
    @RequiresPermissions("settlement:settlement:view")
    @PostMapping("/reconciliation/yearSummary")
    @ResponseBody
    public AjaxResult reconciliationYearSummary(Long hospitalId, Integer year, Long supplierId)
    {
        hospitalId = resolveReconciliationHospitalId(hospitalId);
        if (hospitalId == null)
        {
            return error("请选择医院");
        }
        if (year == null || year < 2000 || year > 2100)
        {
            return error("请选择有效年份");
        }
        Long bindSupplierId = scmSupplierContextService.resolveSupplierIdForUser(getUserId());
        if (bindSupplierId != null)
        {
            supplierId = bindSupplierId;
        }
        List<ReconciliationYearMonthRow> list = scmReconciliationService.selectYearSummary(hospitalId, year, supplierId);
        return success(list);
    }

    /**
     * 医院账号仅本院；供应商账号仅关联医院；平台账号看全部正常医院。
     */
    private List<Hospital> listHospitalsForReconciliation(Long hospitalCtx, Long bindSupplierId)
    {
        Hospital query = new Hospital();
        query.setStatus("0");
        if (hospitalCtx != null)
        {
            query.setHospitalId(hospitalCtx);
            return hospitalService.selectHospitalList(query);
        }
        if (bindSupplierId != null)
        {
            HospitalSupplier relQ = new HospitalSupplier();
            relQ.setSupplierId(bindSupplierId);
            List<HospitalSupplier> rels = hospitalSupplierService.selectHospitalSupplierList(relQ);
            List<Hospital> hospitals = new ArrayList<>();
            Set<Long> seen = new HashSet<>();
            if (rels != null)
            {
                for (HospitalSupplier rel : rels)
                {
                    if (rel == null || rel.getHospitalId() == null || !seen.add(rel.getHospitalId()))
                    {
                        continue;
                    }
                    Hospital h = hospitalService.selectHospitalById(rel.getHospitalId());
                    if (h != null && "0".equals(h.getStatus()))
                    {
                        hospitals.add(h);
                    }
                }
            }
            return hospitals;
        }
        return hospitalService.selectHospitalList(query);
    }

    /**
     * 院端强制本院；商端校验所选医院是否在关联范围内。
     */
    private Long resolveReconciliationHospitalId(Long hospitalId)
    {
        Long hospitalCtx = scmHospitalContextService.resolveHospitalIdForUser(getUserId());
        if (hospitalCtx != null)
        {
            return hospitalCtx;
        }
        Long bindSupplierId = scmSupplierContextService.resolveSupplierIdForUser(getUserId());
        if (bindSupplierId != null)
        {
            if (hospitalId == null)
            {
                return null;
            }
            HospitalSupplier relQ = new HospitalSupplier();
            relQ.setSupplierId(bindSupplierId);
            relQ.setHospitalId(hospitalId);
            List<HospitalSupplier> rels = hospitalSupplierService.selectHospitalSupplierList(relQ);
            if (rels == null || rels.isEmpty())
            {
                return null;
            }
            return hospitalId;
        }
        return hospitalId;
    }

    /**
     * 结算查询：结算明细分页数据
     */
    @RequiresPermissions("settlement:settlement:list")
    @PostMapping("/query/detail/list")
    @ResponseBody
    public TableDataInfo queryDetailList(SettlementDetail settlementDetail)
    {
        startPage();
        List<SettlementDetail> list = settlementService.selectSettlementDetailQueryList(settlementDetail);
        return getDataTable(list);
    }

    /**
     * 结算查询：导出结算明细
     */
    @RequiresPermissions("settlement:settlement:export")
    @Log(title = "结算明细表", businessType = BusinessType.EXPORT)
    @PostMapping("/query/detail/export")
    public void exportQueryDetail(SettlementDetail settlementDetail, HttpServletResponse response)
    {
        List<SettlementDetail> list = settlementService.selectSettlementDetailQueryList(settlementDetail);
        ExcelUtil<SettlementDetail> util = new ExcelUtil<SettlementDetail>(SettlementDetail.class);
        util.exportExcel(response, list, "结算明细表");
    }

    /**
     * 结算查询：结算汇总（主表）分页数据
     */
    @RequiresPermissions("settlement:settlement:list")
    @PostMapping("/query/summary/list")
    @ResponseBody
    public TableDataInfo querySummaryList(Settlement settlement)
    {
        startPage();
        List<Settlement> list = settlementService.selectSettlementList(settlement);
        return getDataTable(list);
    }

    /**
     * 结算查询：导出结算汇总
     */
    @RequiresPermissions("settlement:settlement:export")
    @Log(title = "结算汇总表", businessType = BusinessType.EXPORT)
    @PostMapping("/query/summary/export")
    public void exportQuerySummary(Settlement settlement, HttpServletResponse response)
    {
        List<Settlement> list = settlementService.selectSettlementList(settlement);
        ExcelUtil<Settlement> util = new ExcelUtil<Settlement>(Settlement.class);
        util.exportExcel(response, list, "结算汇总表");
    }

    /**
     * 查询结算单列表
     */
    @RequiresPermissions("settlement:settlement:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(Settlement settlement)
    {
        startPage();
        List<Settlement> list = settlementService.selectSettlementList(settlement);
        return getDataTable(list);
    }

    /**
     * 导出结算单列表
     */
    @RequiresPermissions("settlement:settlement:export")
    @Log(title = "结算单管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(Settlement settlement, HttpServletResponse response)
    {
        List<Settlement> list = settlementService.selectSettlementList(settlement);
        ExcelUtil<Settlement> util = new ExcelUtil<Settlement>(Settlement.class);
        util.exportExcel(response, list, "结算单数据");
    }

    /**
     * 新增结算单
     */
    @RequiresPermissions("settlement:settlement:add")
    @GetMapping("/add")
    public String add()
    {
        return prefix + "/add";
    }

    /**
     * 新增保存结算单
     */
    @RequiresPermissions("settlement:settlement:add")
    @Log(title = "结算单管理", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(@Validated Settlement settlement)
    {
        settlement.setCreateBy(getLoginName());
        return toAjax(settlementService.insertSettlement(settlement));
    }

    /**
     * 查看结算单详情
     */
    @RequiresPermissions("settlement:settlement:view")
    @GetMapping("/view/{settlementId}")
    public String view(@PathVariable("settlementId") Long settlementId, ModelMap mmap)
    {
        Settlement settlement = settlementService.selectSettlementById(settlementId);
        mmap.put("settlement", settlement);
        return prefix + "/view";
    }

    /**
     * 修改结算单
     */
    @RequiresPermissions("settlement:settlement:edit")
    @GetMapping("/edit/{settlementId}")
    public String edit(@PathVariable("settlementId") Long settlementId, ModelMap mmap)
    {
        Settlement settlement = settlementService.selectSettlementById(settlementId);
        mmap.put("settlement", settlement);
        return prefix + "/edit";
    }

    /**
     * 修改保存结算单
     */
    @RequiresPermissions("settlement:settlement:edit")
    @Log(title = "结算单管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(@Validated Settlement settlement)
    {
        settlement.setUpdateBy(getLoginName());
        return toAjax(settlementService.updateSettlement(settlement));
    }

    /**
     * 删除结算单
     */
    @RequiresPermissions("settlement:settlement:remove")
    @Log(title = "结算单管理", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        return toAjax(settlementService.deleteSettlementByIds(ids));
    }

    /**
     * 查看结算单详情
     */
    @RequiresPermissions("settlement:settlement:detail")
    @GetMapping("/detail/{settlementId}")
    public String detail(@PathVariable("settlementId") Long settlementId, ModelMap mmap)
    {
        Settlement settlement = settlementService.selectSettlementById(settlementId);
        mmap.put("settlement", settlement);
        return prefix + "/detail";
    }

    /**
     * 审核结算单
     */
    @RequiresPermissions("settlement:settlement:audit")
    @Log(title = "结算单审核", businessType = BusinessType.UPDATE)
    @PostMapping("/audit")
    @ResponseBody
    public AjaxResult audit(String ids)
    {
        String[] settlementIds = ids.split(",");
        int successCount = 0;
        for (String settlementId : settlementIds)
        {
            if (settlementService.auditSettlement(Long.parseLong(settlementId)) > 0)
            {
                successCount++;
            }
        }
        return successCount > 0 ? success("成功审核 " + successCount + " 个结算单") : error("审核结算单失败");
    }

    /**
     * 查询结算明细列表
     */
    @RequiresPermissions("settlement:settlement:list")
    @PostMapping("/detailList")
    @ResponseBody
    public TableDataInfo detailList(Long settlementId)
    {
        List<SettlementDetail> list = settlementService.selectSettlementDetailListBySettlementId(settlementId);
        return getDataTable(list);
    }
}

