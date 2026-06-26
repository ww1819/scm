package com.scm.framework.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 启动时 SQL 脚本执行配置（仿照 SPD 后端数据完整性脚本）
 *
 * @author scm
 */
@ConfigurationProperties(prefix = "scm.sql.init")
public class SqlInitProperties
{
    /** 是否启用启动时执行 SQL 脚本 */
    private boolean enabled = true;

    /** 脚本根路径，如 classpath:sql/mysql/ 或 file:./sql/mysql/ */
    private String location = "classpath:sql/mysql/";

    /** 某脚本执行失败是否中断应用启动 */
    private boolean failOnError = false;

    /**
     * 是否仅在应用版本 {@code scm.version} 升级后执行增量脚本（推荐生产环境）。
     * 为 true 时：版本未变则跳过；版本变更则执行 {@link #upgradeScripts}。
     */
    private boolean upgradeOnly = true;

    /**
     * 版本升级时执行的增量脚本（须含 procedure.sql，再执行 column.sql）。
     * 逗号分隔，相对 sql/mysql/scm/ 目录。
     */
    private String upgradeScripts = "procedure.sql,column.sql";

    /**
     * 首次安装（库中无 SCM 业务表）时执行的全量脚本顺序。
     * upgrade-only=false 时每次启动也会按此全量顺序执行。
     */
    private String fullScripts = "table.sql,procedure.sql,column.sql,view.sql,trigger.sql,function.sql,menu.sql,data_integrity.sql";

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public String getLocation()
    {
        return location;
    }

    public void setLocation(String location)
    {
        this.location = location;
    }

    public boolean isFailOnError()
    {
        return failOnError;
    }

    public void setFailOnError(boolean failOnError)
    {
        this.failOnError = failOnError;
    }

    public boolean isUpgradeOnly()
    {
        return upgradeOnly;
    }

    public void setUpgradeOnly(boolean upgradeOnly)
    {
        this.upgradeOnly = upgradeOnly;
    }

    public String getUpgradeScripts()
    {
        return upgradeScripts;
    }

    public void setUpgradeScripts(String upgradeScripts)
    {
        this.upgradeScripts = upgradeScripts;
    }

    public String getFullScripts()
    {
        return fullScripts;
    }

    public void setFullScripts(String fullScripts)
    {
        this.fullScripts = fullScripts;
    }

    public String[] parseUpgradeScriptNames()
    {
        return splitScriptList(upgradeScripts);
    }

    public String[] parseFullScriptNames()
    {
        return splitScriptList(fullScripts);
    }

    private static String[] splitScriptList(String csv)
    {
        if (csv == null || csv.trim().isEmpty())
        {
            return new String[0];
        }
        String[] parts = csv.split(",");
        java.util.List<String> out = new java.util.ArrayList<>();
        for (String part : parts)
        {
            String name = part != null ? part.trim() : "";
            if (!name.isEmpty())
            {
                out.add(name);
            }
        }
        return out.toArray(new String[0]);
    }
}
