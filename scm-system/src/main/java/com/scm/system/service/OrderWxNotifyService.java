package com.scm.system.service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.scm.common.config.ServerConfig;
import com.scm.common.config.WeChatMpProperties;
import com.scm.common.core.domain.entity.SysUser;
import com.scm.common.utils.StringUtils;
import com.scm.system.domain.Order;
import com.scm.system.domain.OrderWxNotifyResult;
import com.scm.system.domain.SupplierUser;
import com.scm.system.mapper.SupplierUserMapper;
import com.scm.system.mapper.SysUserMapper;

/**
 * 订单微信模板通知：SPD 新订单首次入库后，发给该供应商下所有已绑微信用户。
 */
@Service
public class OrderWxNotifyService
{
    private static final Logger log = LoggerFactory.getLogger(OrderWxNotifyService.class);

    /** thing 类型约 20 字 */
    private static final int THING_MAX = 20;
    /** character_string 类型约 32 字 */
    private static final int CHAR_STRING_MAX = 32;

    @Autowired
    private IOrderService orderService;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SupplierUserMapper supplierUserMapper;

    @Autowired
    private WeChatMpTemplateService weChatMpTemplateService;

    @Autowired
    private WeChatMpProperties weChatMpProperties;

    /**
     * 向订单供应商下所有已绑微信、账号正常的用户发送模板消息。无人可发时只记日志，不抛错。
     */
    public OrderWxNotifyResult notifyOrderSubmitted(Long orderId)
    {
        OrderWxNotifyResult result = new OrderWxNotifyResult();
        if (orderId == null)
        {
            return fail(result, "订单ID不能为空");
        }
        if (!weChatMpProperties.isTemplateConfigured())
        {
            return fail(result, "未配置微信模板消息，请检查 scm.wechat.mp");
        }
        Order order = orderService.selectOrderById(orderId);
        if (order == null)
        {
            return fail(result, "订单不存在");
        }
        if (order.getSupplierId() == null)
        {
            return fail(result, "订单未关联平台供应商，无法确定接收人");
        }
        List<SysUser> users = sysUserMapper.selectWxBoundUsersBySupplierId(order.getSupplierId());
        if (users == null || users.isEmpty())
        {
            return fail(result, "该供应商没有已绑定微信的用户");
        }
        String jumpUrl = buildOrderJumpUrl(orderId);
        Map<String, String> data = buildTemplateData(order);
        int success = 0;
        int fail = 0;
        for (SysUser user : users)
        {
            String err = weChatMpTemplateService.sendTemplate(user.getWxOpenid(), jumpUrl, data);
            if (err == null)
            {
                success++;
            }
            else
            {
                fail++;
                log.warn("订单模板消息失败 orderId={} loginName={} reason={}", orderId, user.getLoginName(), err);
            }
        }
        result.setRecipientCount(users.size());
        result.setSuccessCount(success);
        result.setFailCount(fail);
        if (success == 0)
        {
            result.setMessage("已尝试向 " + users.size() + " 人发送，全部失败");
            log.warn("订单微信通知: {}", result.getMessage());
            return result;
        }
        result.setMessage("已向 " + users.size() + " 人发送，成功 " + success + "，失败 " + fail);
        log.info("订单微信通知完成 orderId={} supplierId={} {}", orderId, order.getSupplierId(), result.getMessage());
        return result;
    }

    public boolean canSupplierUserViewOrder(Long userId, Order order)
    {
        if (userId == null || order == null)
        {
            return false;
        }
        if (SysUser.isAdmin(userId))
        {
            return true;
        }
        if (order.getSupplierId() == null)
        {
            return false;
        }
        SupplierUser rel = supplierUserMapper.selectSupplierUserByUserIdAndSupplierId(userId, order.getSupplierId());
        if (rel == null)
        {
            return false;
        }
        return rel.getStatus() == null || "0".equals(rel.getStatus());
    }

    public String buildOrderJumpUrl(Long orderId)
    {
        String base = weChatMpProperties.getOauthBaseUrl();
        if (StringUtils.isEmpty(base))
        {
            base = currentRequestDomain();
        }
        if (StringUtils.isEmpty(base) || orderId == null)
        {
            log.warn("未配置 scm.wechat.mp.oauth-base-url，模板消息将不带跳转链接");
            return null;
        }
        while (base.endsWith("/"))
        {
            base = base.substring(0, base.length() - 1);
        }
        return base + "/wx/order/" + orderId;
    }

    public String buildOauthRedirectUri(Long orderId)
    {
        return buildOrderJumpUrl(orderId);
    }

    private Map<String, String> buildTemplateData(Order order)
    {
        Map<String, String> data = new LinkedHashMap<String, String>();
        data.put("character_string14", clip(order.getOrderNo(), CHAR_STRING_MAX));
        data.put("thing3", clip(firstNonBlank(order.getSupplierName(), order.getOrderSupplierName()), THING_MAX));
        data.put("thing8", clip(order.getHospitalName(), THING_MAX));
        data.put("time5", formatTime(order.getOrderDate()));
        data.put("amount2", formatAmount(order.getOrderAmount()));
        return data;
    }

    private OrderWxNotifyResult fail(OrderWxNotifyResult result, String message)
    {
        result.setMessage(message);
        log.warn("订单微信通知跳过: {}", message);
        return result;
    }

    private static String formatTime(Date date)
    {
        if (date == null)
        {
            return clip("", THING_MAX);
        }
        return new SimpleDateFormat("yyyy年MM月dd日 HH:mm").format(date);
    }

    private static String formatAmount(BigDecimal amount)
    {
        if (amount == null)
        {
            return "0.00元";
        }
        return amount.stripTrailingZeros().toPlainString() + "元";
    }

    private static String firstNonBlank(String a, String b)
    {
        if (StringUtils.isNotEmpty(a))
        {
            return a;
        }
        return b;
    }

    private static String clip(String value, int max)
    {
        if (value == null)
        {
            return "";
        }
        String t = value.trim();
        if (t.length() <= max)
        {
            return t;
        }
        return t.substring(0, max);
    }

    private static String currentRequestDomain()
    {
        try
        {
            RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
            if (attrs instanceof ServletRequestAttributes)
            {
                javax.servlet.http.HttpServletRequest req = ((ServletRequestAttributes) attrs).getRequest();
                if (req != null)
                {
                    return ServerConfig.getDomain(req);
                }
            }
        }
        catch (Exception ignored)
        {
        }
        return null;
    }
}
