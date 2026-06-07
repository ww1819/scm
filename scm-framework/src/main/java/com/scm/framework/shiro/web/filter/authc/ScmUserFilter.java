package com.scm.framework.shiro.web.filter.authc;

import java.io.IOException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.web.filter.authc.UserFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scm.common.core.domain.AjaxResult;
import com.scm.common.utils.ServletUtils;

/**
 * 未登录时：页面请求跳转登录页，Ajax 返回 JSON（避免前端把登录页 HTML 当成 JSON 解析报「服务器错误」）。
 */
public class ScmUserFilter extends UserFilter
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    protected void redirectToLogin(ServletRequest request, ServletResponse response) throws IOException
    {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        if (ServletUtils.isAjaxRequest(req))
        {
            ServletUtils.renderString(res,
                    OBJECT_MAPPER.writeValueAsString(AjaxResult.sessionExpired("用户会话已断开，请重新登录系统")));
        }
        else
        {
            super.redirectToLogin(request, response);
        }
    }
}
