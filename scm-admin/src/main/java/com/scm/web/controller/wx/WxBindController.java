package com.scm.web.controller.wx;

import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.scm.common.core.controller.BaseController;
import com.scm.common.core.domain.AjaxResult;
import com.scm.common.core.domain.entity.SysUser;
import com.scm.common.exception.ServiceException;
import com.scm.common.exception.user.UserException;
import com.scm.common.utils.StringUtils;
import com.scm.framework.shiro.service.SysLoginService;
import com.scm.system.service.ISysUserService;
import com.scm.system.service.WeChatMpOauthService;

/**
 * 微信服务号内绑定系统账号（网页授权 code → openid）。
 */
@Controller
@RequestMapping("/wx/bind")
public class WxBindController extends BaseController
{
    private static final String SESSION_OPENID = "WX_MP_OPENID";
    private static final String SESSION_BIND_NAME = "WX_MP_BIND_LOGIN_NAME";

    @Autowired
    private WeChatMpOauthService weChatMpOauthService;

    @Autowired
    private SysLoginService loginService;

    @Autowired
    private ISysUserService userService;

    @GetMapping
    public String bind(String code, HttpSession session, ModelMap mmap)
    {
        String openid = (String) session.getAttribute(SESSION_OPENID);
        if (StringUtils.isEmpty(openid) && StringUtils.isNotEmpty(code))
        {
            try
            {
                openid = weChatMpOauthService.exchangeCodeForOpenid(code);
                session.setAttribute(SESSION_OPENID, openid);
            }
            catch (ServiceException e)
            {
                mmap.put("ready", false);
                mmap.put("errorMsg", e.getMessage());
                return "wx/bind";
            }
        }
        if (StringUtils.isEmpty(openid))
        {
            mmap.put("ready", false);
            mmap.put("errorMsg", "请从微信服务号菜单进入");
            return "wx/bind";
        }
        mmap.put("ready", true);
        mmap.put("errorMsg", "");
        return "wx/bind";
    }

    @GetMapping("/success")
    public String success(HttpSession session, ModelMap mmap)
    {
        String loginName = (String) session.getAttribute(SESSION_BIND_NAME);
        if (StringUtils.isEmpty(loginName))
        {
            return "redirect:/wx/bind";
        }
        mmap.put("loginName", loginName);
        return "wx/bindSuccess";
    }

    @PostMapping("/login")
    @ResponseBody
    public AjaxResult login(String username, String password, HttpSession session)
    {
        String openid = (String) session.getAttribute(SESSION_OPENID);
        if (StringUtils.isEmpty(openid))
        {
            return error("微信授权已失效，请从服务号菜单重新进入");
        }
        try
        {
            SysUser user = loginService.login(username, password);
            userService.bindWxOpenid(user.getUserId(), openid);
            session.setAttribute(SESSION_BIND_NAME, user.getLoginName());
            return success("绑定成功");
        }
        catch (UserException e)
        {
            String msg = e.getMessage();
            return error(StringUtils.isNotEmpty(msg) ? msg : "登录失败");
        }
        catch (ServiceException e)
        {
            return error(e.getMessage());
        }
        catch (Exception e)
        {
            logger.error("微信绑定登录失败", e);
            return error("登录失败，请稍后重试");
        }
    }
}
