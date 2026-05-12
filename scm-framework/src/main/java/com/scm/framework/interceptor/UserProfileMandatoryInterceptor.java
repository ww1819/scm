package com.scm.framework.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import com.scm.common.core.domain.AjaxResult;
import com.scm.common.core.domain.entity.SysUser;
import com.scm.common.json.JSON;
import com.scm.common.utils.ServletUtils;
import com.scm.common.utils.ShiroUtils;
import com.scm.common.utils.StringUtils;
import com.scm.common.utils.UserProfileGateUtils;
import com.scm.system.service.ISysUserService;

/**
 * 登录后若未维护姓名或登录名不合规，仅允许访问个人资料相关接口，其余请求拦截。
 */
@Component
public class UserProfileMandatoryInterceptor implements HandlerInterceptor
{
    @Autowired
    private ISysUserService userService;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
        @NonNull Object handler) throws Exception
    {
        if (!(handler instanceof HandlerMethod))
        {
            return true;
        }
        Subject subject = ShiroUtils.getSubject();
        if (subject == null || (!subject.isAuthenticated() && !subject.isRemembered()))
        {
            return true;
        }
        SysUser sessionUser = ShiroUtils.getSysUser();
        if (sessionUser == null || sessionUser.getUserId() == null)
        {
            return true;
        }
        SysUser fresh = userService.selectUserById(sessionUser.getUserId());
        if (fresh == null || !UserProfileGateUtils.isProfileGateBlocking(fresh))
        {
            return true;
        }
        String reqPath = normalizePath(request);
        if (isAllowedPath(reqPath))
        {
            return true;
        }
        String msg;
        if (UserProfileGateUtils.isLoginNameInvalidForScm(fresh))
        {
            msg = "当前登录名含中文或不符合规则，请先修改登录名并重设密码后再操作。";
        }
        else
        {
            msg = "请先维护姓名后再操作。";
        }
        if (ServletUtils.isAjaxRequest(request))
        {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json;charset=UTF-8");
            ServletUtils.renderString(response, JSON.marshal(AjaxResult.profileGate(msg)));
        }
        else
        {
            response.sendRedirect(request.getContextPath() + "/index");
        }
        return false;
    }

    private static String normalizePath(HttpServletRequest request)
    {
        String ctx = request.getContextPath() == null ? "" : request.getContextPath();
        String uri = request.getRequestURI();
        if (StringUtils.isNotEmpty(ctx) && uri.startsWith(ctx))
        {
            uri = uri.substring(ctx.length());
        }
        if (StringUtils.isEmpty(uri))
        {
            return "/";
        }
        return uri;
    }

    private static boolean isAllowedPath(String path)
    {
        if ("/".equals(path) || "/index".equals(path))
        {
            return true;
        }
        if (path.startsWith("/system/user/profile"))
        {
            return true;
        }
        if (path.startsWith("/logout"))
        {
            return true;
        }
        if (path.startsWith("/captcha/"))
        {
            return true;
        }
        if ("/lockscreen".equals(path) || path.startsWith("/unlockscreen"))
        {
            return true;
        }
        if (path.startsWith("/error"))
        {
            return true;
        }
        return false;
    }
}
