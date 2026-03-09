package com.scm.framework.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import com.scm.framework.config.properties.SqlInitProperties;

/**
 * SQL 启动脚本配置（仿照 SPD 后端）
 *
 * @author scm
 */
@Configuration
@ConditionalOnProperty(prefix = "scm.sql.init", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(SqlInitProperties.class)
public class SqlInitConfig
{
}
