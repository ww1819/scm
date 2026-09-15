package com.scm.web.controller.wx;

import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.scm.common.config.ScmConfig;
import com.scm.common.constant.WeChatMpConstants;
import com.scm.common.core.controller.BaseController;
import com.scm.common.core.domain.AjaxResult;
import com.scm.common.core.domain.entity.SysUser;
import com.scm.common.exception.ServiceException;
import com.scm.common.exception.user.UserException;
import com.scm.common.utils.StringUtils;
import com.scm.framework.shiro.service.SysLoginService;
import com.scm.system.domain.WxBoundAccount;
import com.scm.system.service.ISysUserService;
import com.scm.system.service.WeChatMpOauthService;

/**
 * 微信服务号内绑定系统账号（网页授权 code → openid）。
 */
@Controller
@RequestMapping("/wx/bind")
public class WxBindController extends BaseController
{
    private static final String SESSION_TOAST = "WX_MP_BIND_TOAST";

    @Autowired
    private WeChatMpOauthService weChatMpOauthService;

    @Autowired
    private SysLoginService loginService;

    @Autowired
    private ISysUserService userService;

    @GetMapping
    public String bind(String code, HttpSession session, ModelMap mmap)
    {
        String openid = (String) session.getAttribute(WeChatMpConstants.SESSION_OPENID);
        if (StringUtils.isEmpty(openid) && StringUtils.isNotEmpty(code))
        {
            try
            {
                openid = weChatMpOauthService.exchangeCodeForOpenid(code);
                session.setAttribute(WeChatMpConstants.SESSION_OPENID, openid);
            }
            catch (ServiceException e)
            {
                return bindView(mmap, session, false, e.getMessage());
            }
        }
        if (StringUtils.isEmpty(openid))
        {
            return bindView(mmap, session, false, "请从微信服务号菜单进入");
        }
        return bindView(mmap, session, true, "");
    }

    @GetMapping("/success")
    public String bindSuccessRedirect()
    {
        return "redirect:/wx/bind";
    }

    private String bindView(ModelMap mmap, HttpSession session, boolean ready, String errorMsg)
    {
        mmap.put("ready", ready);
        mmap.put("errorMsg", errorMsg);
        List<WxBoundAccount> accounts = ready
            ? userService.selectWxBoundSupplierAccounts((String) session.getAttribute(WeChatMpConstants.SESSION_OPENID))
            : Collections.<WxBoundAccount>emptyList();
        mmap.put("accounts", accounts);
        mmap.put("hasAccounts", accounts != null && !accounts.isEmpty());
        String toast = (String) session.getAttribute(SESSION_TOAST);
        if (StringUtils.isNotEmpty(toast))
        {
            mmap.put("toastMsg", toast);
            session.removeAttribute(SESSION_TOAST);
        }
        putIcpModel(mmap);
        return "wx/bind";
    }

    private static void putIcpModel(ModelMap mmap)
    {
        String icpNo = ScmConfig.getIcpNo();
        if (StringUtils.isEmpty(icpNo))
        {
            icpNo = "冀ICP备2026009090号-1";
        }
        mmap.put("icpNo", icpNo);
        String icpLink = ScmConfig.getIcpLink();
        mmap.put("icpLink", StringUtils.isNotEmpty(icpLink) ? icpLink : "https://beian.miit.gov.cn/");
    }

    @PostMapping("/login")
    @ResponseBody
    public AjaxResult login(String username, String password, HttpSession session)
    {
        String openid = (String) session.getAttribute(WeChatMpConstants.SESSION_OPENID);
        if (StringUtils.isEmpty(openid))
        {
            return error("微信授权已失效，请从服务号菜单重新进入");
        }
        try
        {
            SysUser user = loginService.login(username, password);
            userService.bindWxOpenid(user.getUserId(), openid);
            session.setAttribute(SESSION_TOAST, "绑定成功");
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

    @PostMapping("/unbind")
    @ResponseBody
    public AjaxResult unbind(Long userId, HttpSession session)
    {
        String openid = (String) session.getAttribute(WeChatMpConstants.SESSION_OPENID);
        if (StringUtils.isEmpty(openid))
        {
            return error("微信授权已失效，请从服务号菜单重新进入");
        }
        try
        {
            userService.unbindWxOpenid(userId, openid);
            session.setAttribute(SESSION_TOAST, "已解除绑定");
            return success("已解除绑定");
        }
        catch (ServiceException e)
        {
            return error(e.getMessage());
        }
        catch (Exception e)
        {
            logger.error("微信解绑失败", e);
            return error("解绑失败，请稍后重试");
        }
    }
}
