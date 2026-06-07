package com.scm.common.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import com.scm.common.utils.StringUtils;

/**
 * 腾讯云 COS 配置
 */
@Component
@ConfigurationProperties(prefix = "scm.cos")
public class CosProperties
{
    /** 是否启用 COS 上传 */
    private boolean enabled = false;

    /** SecretId（支持 ENC(...) 密文） */
    private String secretId;

    /** SecretKey（支持 ENC(...) 密文） */
    private String secretKey;

    /** 地域，如 ap-guangzhou */
    private String region = "ap-guangzhou";

    /** 存储桶名称 */
    private String bucketName;

    /** 对象键前缀 */
    private String basePath = "scm-test/";

    /** 自定义访问域名（可选） */
    private String domain;

    @Autowired
    private ScmConfigCryptoSupport configCryptoSupport;

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public String getSecretId()
    {
        return decrypt(secretId);
    }

    public void setSecretId(String secretId)
    {
        this.secretId = secretId;
    }

    public String getSecretKey()
    {
        return decrypt(secretKey);
    }

    public void setSecretKey(String secretKey)
    {
        this.secretKey = secretKey;
    }

    public String getRegion()
    {
        return region;
    }

    public void setRegion(String region)
    {
        this.region = region;
    }

    public String getBucketName()
    {
        return bucketName;
    }

    public void setBucketName(String bucketName)
    {
        this.bucketName = bucketName;
    }

    public String getBasePath()
    {
        return basePath;
    }

    public void setBasePath(String basePath)
    {
        this.basePath = basePath;
    }

    public String getDomain()
    {
        return domain;
    }

    public void setDomain(String domain)
    {
        this.domain = domain;
    }

    public boolean isConfigured()
    {
        return StringUtils.isNotEmpty(getSecretId())
                && StringUtils.isNotEmpty(getSecretKey())
                && StringUtils.isNotEmpty(bucketName)
                && StringUtils.isNotEmpty(region);
    }

    private String decrypt(String value)
    {
        if (configCryptoSupport == null)
        {
            return value;
        }
        return configCryptoSupport.decryptIfNeeded(value);
    }
}
