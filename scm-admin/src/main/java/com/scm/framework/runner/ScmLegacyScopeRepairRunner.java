package com.scm.framework.runner;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson.JSONObject;
import com.scm.common.utils.StringUtils;
import com.scm.system.domain.SysConfig;
import com.scm.system.service.IScmScopeBootstrapService;
import com.scm.system.service.ISysConfigService;

/**
 * 升级后自动修复：为老医院/老供应商补齐管理员/职工/业务员角色及菜单权限（仅补缺，不删除既有授权）。
 * 默认仅在检测到应用版本 {@code scm.version} 升级时执行一次，通过 {@code sys_config} 记录防重复。
 */
@Component
public class ScmLegacyScopeRepairRunner implements ApplicationRunner
{
    private static final Logger log = LoggerFactory.getLogger(ScmLegacyScopeRepairRunner.class);

    /** 已成功执行补齐所对应的应用版本（与 scm.version 对齐） */
    private static final String CONFIG_KEY_APPLIED_VERSION = "scm.legacy.scope_repair.applied_version";
    /** 最近一次执行摘要（JSON，便于审计） */
    private static final String CONFIG_KEY_LAST_RUN = "scm.legacy.scope_repair.last_run";

    private final IScmScopeBootstrapService scmScopeBootstrapService;
    private final ISysConfigService configService;

    @Value("${scm.legacy.scope-repair.enabled:true}")
    private boolean enabled;

    @Value("${scm.legacy.scope-repair.upgrade-only:true}")
    private boolean upgradeOnly;

    @Value("${scm.version:}")
    private String appVersion;

    public ScmLegacyScopeRepairRunner(IScmScopeBootstrapService scmScopeBootstrapService, ISysConfigService configService)
    {
        this.scmScopeBootstrapService = scmScopeBootstrapService;
        this.configService = configService;
    }

    @Override
    public void run(ApplicationArguments args)
    {
        if (!enabled)
        {
            log.info("Skip legacy scope repair: scm.legacy.scope-repair.enabled=false");
            return;
        }
        String ver = StringUtils.trim(appVersion);
        if (StringUtils.isEmpty(ver))
        {
            log.warn("Skip legacy scope repair: scm.version is empty, cannot detect upgrade.");
            return;
        }
        if (upgradeOnly)
        {
            String applied = StringUtils.trim(configService.selectConfigByKey(CONFIG_KEY_APPLIED_VERSION));
            if (ver.equals(applied))
            {
                log.info("Skip legacy scope repair: already applied for scm.version={}", ver);
                return;
            }
            log.info("Legacy scope repair will run: scm.version={} (last applied={})", ver,
                StringUtils.isEmpty(applied) ? "(none)" : applied);
        }
        else
        {
            log.warn("Legacy scope repair runs on every startup (scm.legacy.scope-repair.upgrade-only=false). scm.version={}", ver);
        }

        try
        {
            Map<String, Integer> stat = scmScopeBootstrapService.repairLegacyAdminScopes("system_upgrade");
            log.info("Legacy admin scope repair finished: {}", stat);

            String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            JSONObject payload = new JSONObject();
            payload.put("version", ver);
            payload.put("time", now);
            payload.put("upgradeOnly", upgradeOnly);
            payload.put("stat", stat);

            upsertConfig(CONFIG_KEY_APPLIED_VERSION, "老数据权限补齐-已应用版本", ver);
            upsertConfig(CONFIG_KEY_LAST_RUN, "老数据权限补齐-最近执行", trimToConfigLimit(payload.toJSONString()));
        }
        catch (Exception e)
        {
            // 启动阶段仅记录，不阻断应用启动；不写入 applied_version，下次启动可重试
            log.error("Legacy admin scope repair failed.", e);
        }
    }

    private static String trimToConfigLimit(String s)
    {
        if (s == null)
        {
            return "";
        }
        if (s.length() <= 500)
        {
            return s;
        }
        return s.substring(0, 497) + "...";
    }

    private void upsertConfig(String key, String name, String value)
    {
        if (StringUtils.isEmpty(key) || value == null)
        {
            return;
        }
        SysConfig probe = new SysConfig();
        probe.setConfigKey(key);
        List<SysConfig> list = configService.selectConfigList(probe);
        if (list != null && !list.isEmpty())
        {
            SysConfig row = list.get(0);
            row.setConfigName(name);
            row.setConfigValue(value);
            row.setUpdateBy("system_upgrade");
            configService.updateConfig(row);
        }
        else
        {
            SysConfig row = new SysConfig();
            row.setConfigName(name);
            row.setConfigKey(key);
            row.setConfigValue(value);
            row.setConfigType("N");
            row.setCreateBy("system_upgrade");
            row.setRemark("系统自动写入：老医院/老供应商角色与菜单权限补齐");
            configService.insertConfig(row);
        }
    }
}
