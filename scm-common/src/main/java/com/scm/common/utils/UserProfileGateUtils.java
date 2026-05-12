package com.scm.common.utils;

import com.scm.common.constant.UserConstants;
import com.scm.common.core.domain.entity.SysUser;

/**
 * 登录后强制完善资料：姓名、合规登录账号等判定。
 */
public final class UserProfileGateUtils
{
    private UserProfileGateUtils()
    {
    }

    /** 姓名为空或仅空白，需维护 */
    public static boolean isRealNameMissing(SysUser user)
    {
        if (user == null)
        {
            return false;
        }
        return StringUtils.isEmpty(StringUtils.trimToNull(user.getRealName()));
    }

    /**
     * 登录名不符合 SCM 规则（含汉字、长度/字符集不合法等），需改为英文登录名并重置密码。
     */
    public static boolean isLoginNameInvalidForScm(SysUser user)
    {
        if (user == null)
        {
            return false;
        }
        String loginName = StringUtils.trimToNull(user.getLoginName());
        if (loginName == null)
        {
            return false;
        }
        if (LoginNameUtils.containsHan(loginName))
        {
            return true;
        }
        return LoginNameUtils.validateLoginName(loginName, UserConstants.USERNAME_MAX_LENGTH) != null;
    }

    /** 任一强制项未满足时需拦截业务请求 */
    public static boolean isProfileGateBlocking(SysUser user)
    {
        return isRealNameMissing(user) || isLoginNameInvalidForScm(user);
    }
}
