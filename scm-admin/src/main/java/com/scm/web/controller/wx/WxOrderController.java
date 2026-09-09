package com.scm.web.controller.wx;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import com.scm.common.config.ScmConfig;
import com.scm.common.config.WeChatMpProperties;
import com.scm.common.constant.WeChatMpConstants;
import com.scm.common.core.controller.BaseController;
import com.scm.common.core.domain.entity.SysUser;
import com.scm.common.exception.ServiceException;
import com.scm.common.utils.StringUtils;
import com.scm.system.domain.Order;
import com.scm.system.domain.OrderDetail;
import com.scm.system.service.IOrderService;
import com.scm.system.service.ISysUserService;
import com.scm.system.service.OrderWxNotifyService;
import com.scm.system.service.WeChatMpOauthService;

/**
 * 微信内手机订单详情（模板消息跳转）。
 */
@Controller
@RequestMapping("/wx/order")
public class WxOrderController extends BaseController
{
    @Autowired
    private WeChatMpOauthService weChatMpOauthService;

    @Autowired
    private WeChatMpProperties weChatMpProperties;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private IOrderService orderService;

    @Autowired
    private OrderWxNotifyService orderWxNotifyService;

    @GetMapping("/{orderId}")
    public String view(@PathVariable("orderId") Long orderId, String code, HttpServletRequest request,
        HttpSession session, ModelMap mmap)
    {
        putIcpModel(mmap);
        mmap.put("orderId", orderId);
        if (orderId == null)
        {
            return errorView(mmap, "订单不存在");
        }
        if (!weChatMpProperties.isConfigured())
        {
            return errorView(mmap, "未配置微信服务号参数");
        }

        String openid = (String) session.getAttribute(WeChatMpConstants.SESSION_OPENID);
        if (StringUtils.isEmpty(openid) && StringUtils.isNotEmpty(code))
        {
            try
            {
                openid = weChatMpOauthService.exchangeCodeForOpenid(code);
                session.setAttribute(WeChatMpConstants.SESSION_OPENID, openid);
                return "redirect:/wx/order/" + orderId;
            }
            catch (ServiceException e)
            {
                return errorView(mmap, e.getMessage());
            }
        }
        if (StringUtils.isEmpty(openid))
        {
            if (!isMicroMessenger(request))
            {
                return errorView(mmap, "请在微信中打开本页查看订单");
            }
            String redirectUri = orderWxNotifyService.buildOauthRedirectUri(orderId);
            if (StringUtils.isEmpty(redirectUri))
            {
                return errorView(mmap, "未配置网页授权域名（scm.wechat.mp.oauth-base-url）");
            }
            return "redirect:" + weChatMpOauthService.buildSnsapiBaseAuthorizeUrl(redirectUri);
        }

        SysUser user = userService.selectUserByWxOpenid(openid);
        if (user == null)
        {
            mmap.put("needBind", Boolean.TRUE);
            mmap.put("bindUrl", request.getContextPath() + "/wx/bind");
            return errorView(mmap, "当前微信尚未绑定系统账号，请先完成绑定后再查看订单");
        }
        if (!"0".equals(user.getStatus()))
        {
            return errorView(mmap, "账号已停用，无法查看订单");
        }

        Order order = orderService.selectOrderById(orderId);
        if (order == null)
        {
            return errorView(mmap, "订单不存在");
        }
        if (!orderWxNotifyService.canSupplierUserViewOrder(user.getUserId(), order))
        {
            return errorView(mmap, "无权查看该订单");
        }
        List<OrderDetail> details = orderService.selectOrderDetailListByOrderId(orderId);
        mmap.put("ready", Boolean.TRUE);
        mmap.put("order", order);
        mmap.put("details", details);
        mmap.put("statusText", statusText(order.getOrderStatus()));
        return "wx/order";
    }

    private String errorView(ModelMap mmap, String message)
    {
        mmap.put("ready", Boolean.FALSE);
        mmap.put("errorMsg", message);
        if (mmap.get("needBind") == null)
        {
            mmap.put("needBind", Boolean.FALSE);
        }
        return "wx/order";
    }

    private static boolean isMicroMessenger(HttpServletRequest request)
    {
        String ua = request.getHeader("User-Agent");
        return ua != null && ua.toLowerCase().contains("micromessenger");
    }

    private static String statusText(String status)
    {
        if ("0".equals(status))
        {
            return "待接收";
        }
        if ("1".equals(status))
        {
            return "已接收";
        }
        if ("2".equals(status))
        {
            return "配送中";
        }
        if ("3".equals(status))
        {
            return "已完成";
        }
        if ("4".equals(status))
        {
            return "已取消";
        }
        return "-";
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
}
