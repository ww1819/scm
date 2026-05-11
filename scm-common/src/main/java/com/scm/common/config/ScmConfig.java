package com.scm.common.config;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 全局配置类
 * 
 * @author scm
 */
@Component
@ConfigurationProperties(prefix = "scm")
public class ScmConfig
{
    /** 项目名称 */
    private static String name;

    /** 版本 */
    private static String version;

    /** 版权年份 */
    private static String copyrightYear;

    /** 实例演示开关 */
    private static boolean demoEnabled;

    /** 上传路径 */
    private static String profile;

    /** 获取地址开关 */
    private static boolean addressEnabled;

    public static String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        ScmConfig.name = name;
    }

    public static String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        ScmConfig.version = version;
    }

    public static String getCopyrightYear()
    {
        return copyrightYear;
    }

    public void setCopyrightYear(String copyrightYear)
    {
        ScmConfig.copyrightYear = copyrightYear;
    }

    public static boolean isDemoEnabled()
    {
        return demoEnabled;
    }

    public void setDemoEnabled(boolean demoEnabled)
    {
        ScmConfig.demoEnabled = demoEnabled;
    }

    public static String getProfile()
    {
        return profile;
    }

    public void setProfile(String profile)
    {
        ScmConfig.profile = profile;
    }

    public static boolean isAddressEnabled()
    {
        return addressEnabled;
    }

    public void setAddressEnabled(boolean addressEnabled)
    {
        ScmConfig.addressEnabled = addressEnabled;
    }

    /**
     * 获取导入上传路径
     */
    public static String getImportPath()
    {
        return getProfile() + "/import";
    }

    /**
     * 获取头像上传路径
     */
    public static String getAvatarPath()
    {
        return getProfile() + "/avatar";
    }

    /**
     * 获取下载路径
     */
    public static String getDownloadPath()
    {
        return getProfile() + "/download/";
    }

    /**
     * Excel 等导出文件的落盘目录（按优先级依次尝试）。
     * 首选为 {@link #getDownloadPath()}；若配置路径所在卷不可用（例如 Windows「设备未就绪」），
     * 可回退到系统临时目录下的 {@code scm-download}。
     */
    public static List<String> getExportDownloadDirectoryCandidates()
    {
        LinkedHashSet<String> ordered = new LinkedHashSet<>();
        String primary = getDownloadPath();
        if (primary != null && !primary.isEmpty())
        {
            ordered.add(primary);
        }
        String tmp = System.getProperty("java.io.tmpdir");
        if (tmp != null && !tmp.isEmpty())
        {
            ordered.add(new File(tmp, "scm-download").getAbsolutePath() + File.separator);
        }
        return new ArrayList<>(ordered);
    }

    /**
     * 获取上传路径
     */
    public static String getUploadPath()
    {
        return getProfile() + "/upload";
    }
}
