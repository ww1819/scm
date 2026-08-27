package com.scm.common.constant;

/**
 * 微信服务号会话键、内部回调头。
 */
public final class WeChatMpConstants
{
    private WeChatMpConstants()
    {
    }

    /** 网页授权换到的 openid，存在 HttpSession */
    public static final String SESSION_OPENID = "WX_MP_OPENID";

    /** scminterface 回调 SCM 内部通知接口的密钥头 */
    public static final String INTERNAL_API_KEY_HEADER = "X-Scm-Internal-Key";
}
