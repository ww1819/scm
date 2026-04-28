package com.scm.web.controller.scm;

import java.util.Map;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.scm.common.annotation.Log;
import com.scm.common.annotation.RepeatSubmit;
import com.scm.common.core.controller.BaseController;
import com.scm.common.core.domain.AjaxResult;
import com.scm.common.enums.BusinessType;
import com.scm.system.service.IScmPinyinBackfillService;

/**
 * 医院、供应商、角色首拼简码批量生成（历史数据回填）。
 */
@Controller
@RequestMapping("/scm/maintenance/pinyin")
public class ScmPinyinBackfillController extends BaseController
{
    @Autowired
    private IScmPinyinBackfillService scmPinyinBackfillService;

    /**
     * 按医院名称、公司名称、角色名称批量写入 pinyin_code；默认仅补空，overwrite=true 时覆盖已有值。
     */
    @RequiresPermissions("system:config:edit")
    @Log(title = "首拼简码批量回填", businessType = BusinessType.UPDATE)
    @RepeatSubmit(interval = 5000, message = "操作过于频繁，请稍后再试")
    @PostMapping("/backfill")
    @ResponseBody
    public AjaxResult backfill(@RequestParam(value = "overwrite", defaultValue = "false") boolean overwrite)
    {
        Map<String, Integer> r = scmPinyinBackfillService.backfillHospitalAndSupplierPinyin(overwrite);
        return AjaxResult.success("处理完成：医院 " + r.get("hospitalUpdated") + " 条，供应商 " + r.get("supplierUpdated")
            + " 条，角色 " + r.get("roleUpdated") + " 条。", r);
    }
}
