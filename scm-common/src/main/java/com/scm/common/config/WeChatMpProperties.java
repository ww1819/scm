package com.scm.common.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import com.scm.common.utils.StringUtils;

/**
 * 微信服务号配置
 */
@Component
@ConfigurationProperties(prefix = "scm.wechat.mp")
public class WeChatMpProperties
{
    /** 是否启用网页授权绑定 */
    private boolean enabled = false;

    /** 服务号 AppID（支持 ENC(...) 密文） */
    private String appId;

    /** 服务号 AppSecret（支持 ENC(...) 密文） */
    private String appSecret;

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

    public String getAppId()
    {
        return decrypt(appId);
    }

    public void setAppId(String appId)
    {
        this.appId = appId;
    }

    public String getAppSecret()
    {
        return decrypt(appSecret);
    }

    public void setAppSecret(String appSecret)
    {
        this.appSecret = appSecret;
    }

    public boolean isConfigured()
    {
        return enabled && StringUtils.isNotEmpty(getAppId()) && StringUtils.isNotEmpty(getAppSecret());
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
