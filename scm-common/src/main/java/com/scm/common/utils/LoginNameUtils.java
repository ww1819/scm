package com.scm.common.utils;

import java.util.regex.Pattern;
import com.scm.common.constant.UserConstants;

/**
 * 登录账号格式校验（与注册页、密码摘要规则配套）
 */
public final class LoginNameUtils
{
    private static final Pattern ALLOWED = Pattern.compile("^[a-zA-Z0-9_-]+$");
    /** CJK 统一表意文字（汉字） */
    private static final Pattern HAS_CJK = Pattern.compile("\\p{IsHan}");

    private LoginNameUtils()
    {
    }

    public static boolean containsHan(String s)
    {
        return s != null && HAS_CJK.matcher(s).find();
    }

    public static boolean isAllowedCharset(String s)
    {
        return s != null && ALLOWED.matcher(s).matches();
    }

    /**
     * @param maxLength 最大长度（含），系统用户名为 {@link UserConstants#USERNAME_MAX_LENGTH}，供应商注册页为 30
     * @return 错误文案；null 表示合法
     */
    public static String validateLoginName(String loginName, int maxLength)
    {
        if (loginName == null || loginName.trim().isEmpty())
        {
            return "登录账号不能为空";
        }
        String t = loginName.trim();
        if (t.length() < UserConstants.USERNAME_MIN_LENGTH || t.length() > maxLength)
        {
            return "登录账号长度须为 " + UserConstants.USERNAME_MIN_LENGTH + "～" + maxLength + " 个字符";
        }
        if (containsHan(t))
        {
            return "登录账号不能包含汉字";
        }
        if (!isAllowedCharset(t))
        {
            return "登录账号仅允许英文字母、数字、下划线（_）与连字符（-）";
        }
        return null;
    }
}
