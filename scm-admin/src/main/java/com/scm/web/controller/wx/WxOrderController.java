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
 * 微信内手机订单列表/详情（模板消息跳转、绑定账号进入）。
 */
@Controller
@RequestMapping("/wx/order")
public class WxOrderController extends BaseController
{
    private static final int HISTORY_LIMIT = 100;

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

    @GetMapping("/list")
    public String list(Long userId, String code, HttpServletRequest request, HttpSession session, ModelMap mmap)
    {
        putIcpModel(mmap);
        mmap.put("listUserId", userId);
        mmap.put("bindUrl", request.getContextPath() + "/wx/bind");
        if (userId == null)
        {
            return listError(mmap, "请选择要查看的账号");
        }
        if (!weChatMpProperties.isConfigured())
        {
            return listError(mmap, "未配置微信服务号参数");
        }
        String openid = resolveOpenidOrRedirect(code, request, session, mmap,
            orderWxNotifyService.buildOrderListJumpUrl(userId), "/wx/order/list?userId=" + userId);
        if (openid == null)
        {
            return mmap.get("viewName") != null ? mmap.get("viewName").toString() : "wx/orderList";
        }

        if (!userService.isWxBoundSupplierUser(openid, userId))
        {
            mmap.put("needBind", Boolean.TRUE);
            return listError(mmap, "当前微信未绑定该供应商账号");
        }
        List<Long> supplierIds = userService.selectActiveSupplierIdsByUserId(userId);
        List<Order> orders = orderService.selectOrderListBySupplierIdsForWx(supplierIds, HISTORY_LIMIT);
        mmap.put("ready", Boolean.TRUE);
        mmap.put("orders", orders);
        mmap.put("accountName", displayName(userId));
        return "wx/orderList";
    }

    @GetMapping("/{orderId}")
    public String view(@PathVariable("orderId") Long orderId, Long userId, String code, HttpServletRequest request,
        HttpSession session, ModelMap mmap)
    {
        putIcpModel(mmap);
        mmap.put("orderId", orderId);
        mmap.put("listUserId", userId);
        mmap.put("bindUrl", request.getContextPath() + "/wx/bind");
        if (orderId == null)
        {
            return errorView(mmap, "订单不存在");
        }
        if (!weChatMpProperties.isConfigured())
        {
            return errorView(mmap, "未配置微信服务号参数");
        }

        String redirectAfterCode = "/wx/order/" + orderId + (userId != null ? "?userId=" + userId : "");
        String oauthUri = orderWxNotifyService.buildOauthRedirectUri(orderId);
        String openid = resolveOpenidOrRedirect(code, request, session, mmap, oauthUri, redirectAfterCode);
        if (openid == null)
        {
            return mmap.get("viewName") != null ? mmap.get("viewName").toString() : "wx/order";
        }

        if (!orderWxNotifyService.hasSupplierBinding(openid))
        {
            mmap.put("needBind", Boolean.TRUE);
            return errorView(mmap, "当前微信尚未绑定供应商账号，请先完成绑定后再查看订单");
        }

        Order order = orderService.selectOrderByIdForSystem(orderId);
        if (order == null)
        {
            return errorView(mmap, "订单不存在");
        }
        if (!orderWxNotifyService.canOpenidViewOrder(openid, order))
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

    /**
     * 解析 openid。需要跳转时返回 null，并在 mmap.viewName 中放入已选视图。
     */
    private String resolveOpenidOrRedirect(String code, HttpServletRequest request, HttpSession session,
        ModelMap mmap, String oauthRedirectUri, String localRedirect)
    {
        String openid = (String) session.getAttribute(WeChatMpConstants.SESSION_OPENID);
        if (StringUtils.isEmpty(openid) && StringUtils.isNotEmpty(code))
        {
            try
            {
                openid = weChatMpOauthService.exchangeCodeForOpenid(code);
                session.setAttribute(WeChatMpConstants.SESSION_OPENID, openid);
                mmap.put("viewName", "redirect:" + localRedirect);
                return null;
            }
            catch (ServiceException e)
            {
                mmap.put("viewName", listOrDetailError(mmap, localRedirect, e.getMessage()));
                return null;
            }
        }
        if (StringUtils.isEmpty(openid))
        {
            if (!isMicroMessenger(request))
            {
                mmap.put("viewName", listOrDetailError(mmap, localRedirect, "请在微信中打开本页查看订单"));
                return null;
            }
            if (StringUtils.isEmpty(oauthRedirectUri))
            {
                mmap.put("viewName", listOrDetailError(mmap, localRedirect, "未配置网页授权域名（scm.wechat.mp.oauth-base-url）"));
                return null;
            }
            mmap.put("viewName", "redirect:" + weChatMpOauthService.buildSnsapiBaseAuthorizeUrl(oauthRedirectUri));
            return null;
        }
        return openid;
    }

    private String listOrDetailError(ModelMap mmap, String localRedirect, String message)
    {
        if (localRedirect != null && localRedirect.contains("/list"))
        {
            return listError(mmap, message);
        }
        return errorView(mmap, message);
    }

    private String displayName(Long userId)
    {
        SysUser user = userService.selectUserById(userId);
        if (user == null)
        {
            return "";
        }
        if (StringUtils.isNotEmpty(user.getUserName()))
        {
            return user.getUserName();
        }
        return user.getLoginName();
    }

    private String listError(ModelMap mmap, String message)
    {
        mmap.put("ready", Boolean.FALSE);
        mmap.put("errorMsg", message);
        if (mmap.get("needBind") == null)
        {
            mmap.put("needBind", Boolean.FALSE);
        }
        return "wx/orderList";
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

    static String statusText(String status)
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
