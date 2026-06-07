package com.scm.common.utils.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import com.scm.common.utils.StringUtils;

/**
 * 配置项对称加解密（用于 yml 中 ENC(...) 包裹的敏感配置）
 */
public final class ConfigCryptoUtils
{
    private static final String ENC_PREFIX = "ENC(";
    private static final String ENC_SUFFIX = ")";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final int IV_LENGTH = 16;

    private ConfigCryptoUtils()
    {
    }

    public static boolean isEncrypted(String value)
    {
        return StringUtils.isNotEmpty(value) && value.startsWith(ENC_PREFIX) && value.endsWith(ENC_SUFFIX);
    }

    /**
     * 若为 ENC(...) 则解密，否则原样返回（兼容明文配置）
     */
    public static String decryptIfNeeded(String value, String cipherKey)
    {
        if (StringUtils.isEmpty(value))
        {
            return value;
        }
        if (!isEncrypted(value))
        {
            return value;
        }
        String payload = value.substring(ENC_PREFIX.length(), value.length() - ENC_SUFFIX.length());
        return decrypt(payload, cipherKey);
    }

    public static String encrypt(String plainText, String cipherKey)
    {
        if (StringUtils.isEmpty(plainText))
        {
            return plainText;
        }
        requireCipherKey(cipherKey);
        try
        {
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, buildKey(cipherKey), new IvParameterSpec(iv));
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
            return ENC_PREFIX + Base64.getEncoder().encodeToString(combined) + ENC_SUFFIX;
        }
        catch (Exception e)
        {
            throw new IllegalStateException("配置加密失败", e);
        }
    }

    public static String decrypt(String base64Payload, String cipherKey)
    {
        requireCipherKey(cipherKey);
        try
        {
            byte[] combined = Base64.getDecoder().decode(base64Payload);
            if (combined.length <= IV_LENGTH)
            {
                throw new IllegalArgumentException("密文格式无效");
            }
            byte[] iv = new byte[IV_LENGTH];
            byte[] encrypted = new byte[combined.length - IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH);
            System.arraycopy(combined, IV_LENGTH, encrypted, 0, encrypted.length);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, buildKey(cipherKey), new IvParameterSpec(iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        }
        catch (Exception e)
        {
            throw new IllegalStateException("配置解密失败，请检查 scm.config-cipher-key 是否正确", e);
        }
    }

    private static SecretKeySpec buildKey(String cipherKey)
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = digest.digest(cipherKey.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(keyBytes, "AES");
        }
        catch (Exception e)
        {
            throw new IllegalStateException("构建 AES 密钥失败", e);
        }
    }

    private static void requireCipherKey(String cipherKey)
    {
        if (StringUtils.isEmpty(cipherKey))
        {
            throw new IllegalStateException("未配置 scm.config-cipher-key，无法加解密敏感配置");
        }
    }
}
