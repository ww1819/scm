package com.scm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

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
        SpringApplication.run(ScmApplication.class, args);
        System.out.println("耗材供应链管理平台启动成功 ");
    }
}