package com.scm.common.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.scm.common.utils.security.ConfigCryptoUtils;

/**
 * 敏感配置解密（COS 等）
 */
@Component
public class ScmConfigCryptoSupport
{
    @Autowired
    private ScmConfig scmConfig;

    public String decryptIfNeeded(String value)
    {
        return ConfigCryptoUtils.decryptIfNeeded(value, scmConfig.getConfigCipherKey());
    }
}
