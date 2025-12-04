package com.scm.web.controller.settlement;

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
import com.scm.system.domain.Settlement;
import com.scm.system.domain.SettlementDetail;
import com.scm.system.service.ISettlementService;

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

    @RequiresPermissions("settlement:settlement:view")
    @GetMapping()
    public String settlement()
    {
        return prefix + "/settlement";
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
    @ResponseBody
    public AjaxResult export(Settlement settlement)
    {
        List<Settlement> list = settlementService.selectSettlementList(settlement);
        ExcelUtil<Settlement> util = new ExcelUtil<Settlement>(Settlement.class);
        return util.exportExcel(list, "结算单数据");
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

