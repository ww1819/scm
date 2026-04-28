package com.scm.web.controller.scm;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.scm.common.annotation.Log;
import com.scm.common.annotation.RepeatSubmit;
import com.scm.common.core.controller.BaseController;
import com.scm.common.core.domain.AjaxResult;
import com.scm.common.enums.BusinessType;
import com.scm.common.utils.StringUtils;
import com.scm.system.domain.SysConfig;
import com.scm.system.service.IScmScopeBootstrapService;
import com.scm.system.service.ISysConfigService;

/**
 * 老医院/老供应商：管理员/职工/业务员角色与白名单、管理员角色菜单（sys_role_menu）补缺维护。
 */
@Controller
@RequestMapping("/scm/maintenance/legacyScope")
public class ScmLegacyScopeRepairController extends BaseController
{
    private static final String CONFIG_LAST_MANUAL_MS = "scm.legacy.scope_repair.last_manual_run_ms";

    @Autowired
    private IScmScopeBootstrapService scmScopeBootstrapService;
    @Autowired
    private ISysConfigService configService;

    @Value("${scm.legacy.scope-repair.manual-cooldown-ms:120000}")
    private long manualCooldownMs;

    /**
     * 手工执行补齐（仅补缺，不删除已有授权）。<br/>
     * 会补齐：① 缺失的医院管理员/医院职工/供应商管理员/供应商业务员角色；② 医院/供应商菜单白名单缺失项；③ 管理员角色在 sys_role_menu 上缺失的菜单行（与当前菜单种子一致）。<br/>
     * 防重复：默认 2 分钟内不可重复点击（可传 force=true 跳过冷却）；另受全局防重复提交拦截。
     */
    @RequiresPermissions("system:config:edit")
    @Log(title = "老数据权限补齐（手工）", businessType = BusinessType.UPDATE)
    @RepeatSubmit(interval = 10000, message = "操作过于频繁，请稍后再试")
    @PostMapping("/run")
    @ResponseBody
    public AjaxResult run(@RequestParam(value = "force", defaultValue = "false") boolean force)
    {
        if (!force && manualCooldownMs > 0)
        {
            String lastStr = StringUtils.trim(configService.selectConfigByKey(CONFIG_LAST_MANUAL_MS));
            if (StringUtils.isNotEmpty(lastStr))
            {
                try
                {
                    long last = Long.parseLong(lastStr);
                    long elapsed = System.currentTimeMillis() - last;
                    if (elapsed >= 0 && elapsed < manualCooldownMs)
                    {
                        long left = (manualCooldownMs - elapsed + 999) / 1000;
                        return AjaxResult.warn("请勿重复执行：距上次手工补齐不足冷却时间（约 " + left + " 秒后可再试）。如需立即再跑请勾选「强制」或传 force=true。");
                    }
                }
                catch (NumberFormatException ignored)
                {
                    // 忽略异常时间戳，继续执行
                }
            }
        }

        Map<String, Integer> stat = scmScopeBootstrapService.repairLegacyAdminScopes(getLoginName());
        upsertLastManualRunMs(String.valueOf(System.currentTimeMillis()));

        int sum = 0;
        for (Integer v : stat.values())
        {
            if (v != null)
            {
                sum += v;
            }
        }
        if (sum == 0)
        {
            return AjaxResult.success("未发现待补齐项（角色、白名单、管理员角色菜单均已满足当前规则）。", stat);
        }
        return AjaxResult.success("补齐完成：已处理缺失的医院管理员/医院职工/供应商管理员/供应商业务员角色、菜单白名单及管理员角色的 sys_role_menu 权限行。", stat);
    }

    private void upsertLastManualRunMs(String ms)
    {
        String key = CONFIG_LAST_MANUAL_MS;
        String name = "老数据权限补齐-上次手工执行时间戳(ms)";
        SysConfig probe = new SysConfig();
        probe.setConfigKey(key);
        java.util.List<SysConfig> list = configService.selectConfigList(probe);
        if (list != null && !list.isEmpty())
        {
            SysConfig row = list.get(0);
            row.setConfigName(name);
            row.setConfigValue(ms);
            row.setUpdateBy(getLoginName());
            configService.updateConfig(row);
        }
        else
        {
            SysConfig row = new SysConfig();
            row.setConfigName(name);
            row.setConfigKey(key);
            row.setConfigValue(ms);
            row.setConfigType("N");
            row.setCreateBy(getLoginName());
            row.setRemark("系统自动写入：手工补齐冷却时间戳");
            configService.insertConfig(row);
        }
    }
}
