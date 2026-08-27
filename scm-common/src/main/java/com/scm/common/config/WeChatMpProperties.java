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

    /** 订单提交成功通知模板 ID */
    private String templateId;

    /**
     * 对外 HTTPS 根地址（无尾斜杠），须与服务号「网页授权域名」一致。
     * 用于模板消息跳转与 oauth redirect_uri，例如 https://scm.example.com
     */
    private String oauthBaseUrl;

    /** scminterface 回调内部通知接口的密钥（支持 ENC(...)） */
    private String internalApiKey;

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

    public String getTemplateId()
    {
        return templateId;
    }

    public void setTemplateId(String templateId)
    {
        this.templateId = templateId;
    }

    public String getOauthBaseUrl()
    {
        if (StringUtils.isEmpty(oauthBaseUrl))
        {
            return oauthBaseUrl;
        }
        String trimmed = oauthBaseUrl.trim();
        while (trimmed.endsWith("/"))
        {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    public void setOauthBaseUrl(String oauthBaseUrl)
    {
        this.oauthBaseUrl = oauthBaseUrl;
    }

    public String getInternalApiKey()
    {
        return decrypt(internalApiKey);
    }

    public void setInternalApiKey(String internalApiKey)
    {
        this.internalApiKey = internalApiKey;
    }

    public boolean isConfigured()
    {
        return enabled && StringUtils.isNotEmpty(getAppId()) && StringUtils.isNotEmpty(getAppSecret());
    }

    public boolean isTemplateConfigured()
    {
        return isConfigured() && StringUtils.isNotEmpty(getTemplateId());
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
