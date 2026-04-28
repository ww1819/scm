package com.scm;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

/**
 * 启动程序
 * 
 * @author scm
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class ScmApplication
{
    public static void main(String[] args)
    {
        // System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication app = new SpringApplication(ScmApplication.class);
        app.setBannerMode(Banner.Mode.OFF);
        ConfigurableApplicationContext context = app.run(args);
        printStartupAddresses(context.getEnvironment());
    }

    private static void printStartupAddresses(Environment env)
    {
        String appName = env.getProperty("spring.application.name", "scm");
        String protocol = "true".equals(env.getProperty("server.ssl.enabled")) ? "https" : "http";
        String port = env.getProperty("server.port", "80");
        String contextPath = env.getProperty("server.servlet.context-path", "/");
        if (contextPath == null || contextPath.trim().isEmpty())
        {
            contextPath = "/";
        }
        if (!contextPath.startsWith("/"))
        {
            contextPath = "/" + contextPath;
        }
        String localUrl = protocol + "://localhost:" + port + normalizePath(contextPath);
        String hostAddress = "localhost";
        try
        {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        }
        catch (UnknownHostException ignored)
        {
        }
        String externalUrl = protocol + "://" + hostAddress + ":" + port + normalizePath(contextPath);
        System.out.println("\n----------------------------------------------------------\n\t" +
            appName + " 启动成功，访问地址：\n\t" +
            "本机: \t" + localUrl + "\n\t" +
            "外网: \t" + externalUrl + "\n----------------------------------------------------------");
    }

    private static String normalizePath(String contextPath)
    {
        return "/".equals(contextPath) ? "/" : contextPath + "/";
    }
}