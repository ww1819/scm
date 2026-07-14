package com.scm.framework.runner;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.fastjson.JSONObject;
import com.scm.common.utils.StringUtils;
import com.scm.framework.config.properties.SqlInitProperties;
import com.scm.system.domain.SysConfig;
import com.scm.system.service.ISysConfigService;

/**
 * 启动时按配置执行 SCM SQL 脚本。
 * <p>
 * 默认 {@code upgrade-only=true}：仅在 {@code scm.version} 相对库中记录发生变更时，
 * 自动执行 {@code procedure.sql} + {@code column.sql}（增量结构变更，可重复执行）。
 * 首次安装（无 SCM 业务表）时执行全量脚本链。
 *
 * @author scm
 */
@Component
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix = "scm.sql.init", name = "enabled", havingValue = "true")
public class SqlInitRunner implements ApplicationRunner
{
    private static final Logger log = LoggerFactory.getLogger(SqlInitRunner.class);

    private static final String CONFIG_KEY_APPLIED_VERSION = "scm.sql.init.applied_version";
    private static final String CONFIG_KEY_LAST_RUN = "scm.sql.init.last_run";

    private static final String MODULE = "scm";

    private final DataSource masterDataSource;
    private final SqlInitProperties properties;
    private final ResourceLoader resourceLoader;
    private final ISysConfigService configService;

    @Value("${scm.version:}")
    private String appVersion;

    public SqlInitRunner(
            @Qualifier("masterDataSource") DataSource masterDataSource,
            SqlInitProperties properties,
            ResourceLoader resourceLoader,
            ISysConfigService configService)
    {
        this.masterDataSource = masterDataSource;
        this.properties = properties;
        this.resourceLoader = resourceLoader;
        this.configService = configService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception
    {
        if (!properties.isEnabled())
        {
            return;
        }

        String ver = StringUtils.trim(appVersion);
        if (properties.isUpgradeOnly())
        {
            if (StringUtils.isEmpty(ver))
            {
                log.warn("Skip SCM SQL init: scm.version is empty, cannot detect upgrade.");
                return;
            }
            String applied = StringUtils.trim(configService.selectConfigByKey(CONFIG_KEY_APPLIED_VERSION));
            if (ver.equals(applied))
            {
                log.info("Skip SCM SQL init: column/procedure already applied for scm.version={}", ver);
                return;
            }
            log.info("SCM SQL init will run: scm.version={} (last applied={})", ver,
                StringUtils.isEmpty(applied) ? "(none)" : applied);
        }
        else
        {
            log.warn("SCM SQL init runs on every startup (scm.sql.init.upgrade-only=false). scm.version={}", ver);
        }

        String[] scripts = resolveScriptsToRun();
        if (scripts.length == 0)
        {
            log.warn("SCM SQL init: no scripts configured, skip.");
            return;
        }

        String location = normalizeLocation(properties.getLocation());
        boolean failOnError = properties.isFailOnError();
        Exception firstError = null;

        for (String scriptName : scripts)
        {
            String path = location + MODULE + "/" + scriptName;
            Resource resource = resourceLoader.getResource(path);
            if (!resource.exists())
            {
                log.debug("SCM SQL 脚本不存在，跳过: {}", path);
                continue;
            }

            try
            {
                String content = readResource(resource);
                List<String> statements = parseStatements(content);
                Exception err = executeStatements(statements, scriptName, failOnError);
                if (err != null && firstError == null)
                {
                    firstError = err;
                }
                log.info("SCM SQL 脚本执行完成: {}", scriptName);
            }
            catch (Exception e)
            {
                log.error("SCM SQL 脚本执行失败: {}", scriptName, e);
                if (firstError == null)
                {
                    firstError = e;
                }
                if (failOnError)
                {
                    throw e;
                }
            }
        }

        if (firstError != null && failOnError)
        {
            throw firstError;
        }
        if (firstError != null)
        {
            log.warn("SCM SQL init finished with errors; applied_version will NOT be updated.");
            return;
        }

        if (properties.isUpgradeOnly() && StringUtils.isNotEmpty(ver))
        {
            recordAppliedVersion(ver, scripts);
        }
    }

    private String[] resolveScriptsToRun()
    {
        if (!properties.isUpgradeOnly())
        {
            return properties.parseFullScriptNames();
        }
        Connection conn = null;
        try
        {
            conn = getRawConnection();
            if (!scmCoreTablesExist(conn))
            {
                log.info("SCM SQL init: fresh database detected, running full script chain.");
                return properties.parseFullScriptNames();
            }
            log.info("SCM SQL init: existing database, running upgrade scripts only.");
            return properties.parseUpgradeScriptNames();
        }
        catch (Exception e)
        {
            log.warn("SCM SQL init: cannot detect database state, fallback to upgrade scripts.", e);
            return properties.parseUpgradeScriptNames();
        }
        finally
        {
            if (conn != null)
            {
                try
                {
                    conn.close();
                }
                catch (Exception ignored)
                {
                }
            }
        }
    }

    private static boolean scmCoreTablesExist(Connection conn)
    {
        String sql = "SELECT COUNT(1) FROM information_schema.tables "
            + "WHERE table_schema = DATABASE() AND table_name = 'scm_hospital'";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql))
        {
            if (rs.next())
            {
                return rs.getInt(1) > 0;
            }
        }
        catch (Exception e)
        {
            return false;
        }
        return false;
    }

    private void recordAppliedVersion(String ver, String[] scripts)
    {
        try
        {
            String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            JSONObject payload = new JSONObject();
            payload.put("version", ver);
            payload.put("time", now);
            payload.put("scripts", String.join(",", scripts));
            upsertConfig(CONFIG_KEY_APPLIED_VERSION, "SQL增量脚本-已应用版本", ver);
            upsertConfig(CONFIG_KEY_LAST_RUN, "SQL增量脚本-最近执行", trimToConfigLimit(payload.toJSONString()));
            log.info("SCM SQL init recorded applied version: {}", ver);
        }
        catch (Exception e)
        {
            log.warn("SCM SQL init: failed to record applied version.", e);
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
            row.setRemark("系统自动写入：启动时 SQL 增量脚本（procedure/column）");
            configService.insertConfig(row);
        }
    }

    private static String normalizeLocation(String location)
    {
        if (location == null || location.isEmpty())
        {
            return "classpath:sql/mysql/";
        }
        return location.endsWith("/") ? location : location + "/";
    }

    private static String readResource(Resource resource) throws IOException
    {
        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))
        {
            StringBuilder sb = new StringBuilder();
            char[] buf = new char[4096];
            int n;
            while ((n = reader.read(buf)) >= 0)
            {
                sb.append(buf, 0, n);
            }
            return sb.toString();
        }
    }

    /**
     * 按单独一行的「/」分隔符解析 SQL，得到多条语句。
     */
    private List<String> parseStatements(String content)
    {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String line : content.split("\\r?\\n", -1))
        {
            if (line.trim().equals("/"))
            {
                String stmt = current.toString().trim();
                if (!stmt.isEmpty())
                {
                    statements.add(stmt);
                }
                current.setLength(0);
                continue;
            }
            current.append(line).append("\n");
        }
        String stmt = current.toString().trim();
        if (!stmt.isEmpty())
        {
            statements.add(stmt);
        }
        return statements;
    }

    /** 仅注释或空白的片段不发送给 MySQL */
    private static boolean isCommentOrBlankOnly(String sql)
    {
        String s = sql.trim();
        if (s.isEmpty())
        {
            return true;
        }
        boolean inBlock = false;
        StringBuilder sb = new StringBuilder();
        for (String line : s.split("\\r?\\n"))
        {
            String t = line.trim();
            if (inBlock)
            {
                if (t.endsWith("*/"))
                {
                    inBlock = false;
                }
                continue;
            }
            if (t.startsWith("/*"))
            {
                inBlock = !t.contains("*/");
                continue;
            }
            if (t.isEmpty() || t.startsWith("--"))
            {
                continue;
            }
            sb.append(t).append(' ');
        }
        return sb.toString().trim().isEmpty();
    }

    private Exception executeStatements(List<String> statements, String scriptName, boolean failOnError) throws Exception
    {
        Connection conn = getRawConnection();
        Exception firstError = null;
        try
        {
            for (String sql : statements)
            {
                sql = sql.trim();
                if (sql.isEmpty() || isCommentOrBlankOnly(sql))
                {
                    continue;
                }
                Exception err = executeOne(conn, sql, scriptName);
                if (err != null && isConnectionClosedOrTimeout(err))
                {
                    try
                    {
                        conn.close();
                    }
                    catch (Exception ignored)
                    {
                    }
                    conn = getRawConnection();
                    err = executeOne(conn, sql, scriptName);
                }
                if (err != null && firstError == null)
                {
                    firstError = err;
                }
                if (err != null && failOnError)
                {
                    throw err;
                }
            }
            return firstError;
        }
        finally
        {
            if (conn != null)
            {
                try
                {
                    conn.close();
                }
                catch (Exception ignored)
                {
                }
            }
        }
    }

    private Exception executeOne(Connection conn, String sql, String scriptName)
    {
        try (Statement st = conn.createStatement())
        {
            st.execute(sql);
            consumeAllResults(st);
            return null;
        }
        catch (Exception e)
        {
            log.warn("执行单条 SQL 失败 [{}]: {}", scriptName, sql.substring(0, Math.min(80, sql.length())) + "...", e);
            return e;
        }
    }

    private static boolean isConnectionClosedOrTimeout(Exception e)
    {
        if (e == null)
        {
            return false;
        }
        String msg = e.getMessage();
        if (msg != null && (msg.contains("connection closed") || msg.contains("Connection closed")
                || msg.contains("No operations allowed after connection closed")
                || msg.contains("Communications link failure") || msg.contains("Read timed out")))
        {
            return true;
        }
        Throwable cause = e.getCause();
        return cause != null && cause != e
            && isConnectionClosedOrTimeout(cause instanceof Exception ? (Exception) cause : new Exception(cause));
    }

    private static void consumeAllResults(Statement st) throws java.sql.SQLException
    {
        while (true)
        {
            if (st.getMoreResults(Statement.CLOSE_CURRENT_RESULT))
            {
                continue;
            }
            if (st.getUpdateCount() == -1)
            {
                break;
            }
        }
    }

    private static final int SOCKET_TIMEOUT_MS = 300_000;

    private Connection getRawConnection() throws Exception
    {
        if (masterDataSource instanceof DruidDataSource)
        {
            DruidDataSource druid = (DruidDataSource) masterDataSource;
            String url = druid.getUrl();
            url = appendSocketTimeout(url, SOCKET_TIMEOUT_MS);
            return DriverManager.getConnection(url, druid.getUsername(), druid.getPassword());
        }
        return masterDataSource.getConnection();
    }

    private static String appendSocketTimeout(String url, int timeoutMs)
    {
        if (url == null)
        {
            return url;
        }
        String param = "socketTimeout=" + timeoutMs;
        if (url.contains("socketTimeout="))
        {
            return url;
        }
        return url + (url.contains("?") ? "&" : "?") + param;
    }
}
