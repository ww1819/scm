package com.scm.web.controller.internal;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.scm.common.config.WeChatMpProperties;
import com.scm.common.constant.WeChatMpConstants;
import com.scm.common.core.controller.BaseController;
import com.scm.common.core.domain.AjaxResult;
import com.scm.common.utils.StringUtils;
import com.scm.system.domain.OrderWxNotifyResult;
import com.scm.system.service.OrderWxNotifyService;

/**
 * scminterface 在首次写入 scm_order 后回调，发送微信模板消息。
 */
@RestController
@RequestMapping("/api/internal/order")
public class OrderWxNotifyInternalController extends BaseController
{
    private static final Logger log = LoggerFactory.getLogger(OrderWxNotifyInternalController.class);

    @Autowired
    private WeChatMpProperties weChatMpProperties;

    @Autowired
    private OrderWxNotifyService orderWxNotifyService;

    @PostMapping("/wxNotify")
    public AjaxResult wxNotify(
        @RequestHeader(value = WeChatMpConstants.INTERNAL_API_KEY_HEADER, required = false) String apiKey,
        @RequestBody(required = false) Map<String, Object> body)
    {
        if (!apiKeyMatches(apiKey))
        {
            return AjaxResult.error("未授权");
        }
        Long orderId = parseOrderId(body);
        if (orderId == null)
        {
            return AjaxResult.success("缺少 orderId，已跳过微信通知");
        }
        try
        {
            OrderWxNotifyResult result = orderWxNotifyService.notifyOrderSubmitted(orderId);
            return AjaxResult.success(result.getMessage());
        }
        catch (Exception e)
        {
            log.error("内部订单微信通知失败 orderId={}", orderId, e);
            return AjaxResult.success("微信通知失败已忽略");
        }
    }

    private boolean apiKeyMatches(String given)
    {
        String expected = weChatMpProperties.getInternalApiKey();
        if (StringUtils.isEmpty(expected) || StringUtils.isEmpty(given))
        {
            return false;
        }
        byte[] a = expected.getBytes(StandardCharsets.UTF_8);
        byte[] b = given.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(a, b);
    }

    private static Long parseOrderId(Map<String, Object> body)
    {
        if (body == null)
        {
            return null;
        }
        Object raw = body.get("orderId");
        if (raw instanceof Number)
        {
            return ((Number) raw).longValue();
        }
        if (raw != null)
        {
            try
            {
                return Long.parseLong(String.valueOf(raw).trim());
            }
            catch (NumberFormatException ignored)
            {
                return null;
            }
        }
        return null;
    }
}
