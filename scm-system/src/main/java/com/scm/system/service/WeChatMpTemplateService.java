package com.scm.system.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.scm.common.config.WeChatMpProperties;
import com.scm.common.exception.ServiceException;
import com.scm.common.utils.StringUtils;

/**
 * 微信服务号模板消息：access_token 缓存与发送。
 */
@Service
public class WeChatMpTemplateService
{
    private static final Logger log = LoggerFactory.getLogger(WeChatMpTemplateService.class);

    private static final String TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";
    private static final String SEND_URL = "https://api.weixin.qq.com/cgi-bin/message/template/send";
    /** 提前 5 分钟刷新，避免临界过期 */
    private static final long EXPIRE_SKEW_MS = 5 * 60 * 1000L;

    private final AtomicReference<CachedToken> cachedToken = new AtomicReference<CachedToken>();

    @Autowired
    private WeChatMpProperties weChatMpProperties;

    @Autowired
    private WeChatMpHttpClient weChatMpHttpClient;

    /**
     * @return null 表示成功，否则为失败原因
     */
    public String sendTemplate(String openid, String url, Map<String, String> data)
    {
        if (!weChatMpProperties.isTemplateConfigured())
        {
            return "未配置微信模板消息参数";
        }
        if (StringUtils.isEmpty(openid))
        {
            return "openid 为空";
        }
        try
        {
            JSONObject payload = new JSONObject();
            payload.put("touser", openid);
            payload.put("template_id", weChatMpProperties.getTemplateId());
            if (StringUtils.isNotEmpty(url))
            {
                payload.put("url", url);
            }
            JSONObject dataNode = new JSONObject();
            if (data != null)
            {
                for (Map.Entry<String, String> e : data.entrySet())
                {
                    JSONObject item = new JSONObject();
                    item.put("value", e.getValue() == null ? "" : e.getValue());
                    dataNode.put(e.getKey(), item);
                }
            }
            payload.put("data", dataNode);

            JSONObject json = postSend(payload.toJSONString(), false);
            Integer errcode = json.getInteger("errcode");
            if (errcode != null && (errcode == 40001 || errcode == 42001))
            {
                cachedToken.set(null);
                json = postSend(payload.toJSONString(), true);
                errcode = json.getInteger("errcode");
            }
            if (errcode != null && errcode != 0)
            {
                String errmsg = json.getString("errmsg");
                log.warn("wechat template send failed openidSuffix={} templateId={} errcode={} errmsg={}",
                    tail(openid), weChatMpProperties.getTemplateId(), errcode, errmsg);
                if (Integer.valueOf(40037).equals(errcode))
                {
                    return "微信模板ID无效，请核对 scm.wechat.mp.template-id";
                }
                return StringUtils.isNotEmpty(errmsg) ? errmsg : ("errcode=" + errcode);
            }
            return null;
        }
        catch (ServiceException e)
        {
            return e.getMessage();
        }
        catch (Exception e)
        {
            log.error("wechat template send error", e);
            return "发送失败";
        }
    }

    private JSONObject postSend(String body, boolean forceRefresh)
    {
        String token = getAccessToken(forceRefresh);
        String sendUrl = SEND_URL + "?access_token=" + encode(token);
        String resp = weChatMpHttpClient.postJson(sendUrl, body);
        JSONObject json = JSON.parseObject(resp);
        if (json == null)
        {
            throw new ServiceException("微信模板接口无响应");
        }
        return json;
    }

    private String getAccessToken(boolean forceRefresh)
    {
        if (!forceRefresh)
        {
            CachedToken current = cachedToken.get();
            if (current != null && current.expireAtMs > System.currentTimeMillis())
            {
                return current.token;
            }
        }
        String req = TOKEN_URL
            + "?grant_type=client_credential"
            + "&appid=" + encode(weChatMpProperties.getAppId())
            + "&secret=" + encode(weChatMpProperties.getAppSecret());
        String body = weChatMpHttpClient.get(req);
        JSONObject json = JSON.parseObject(body);
        if (json == null)
        {
            throw new ServiceException("获取微信 access_token 失败");
        }
        Integer errcode = json.getInteger("errcode");
        if (errcode != null && errcode != 0)
        {
            log.warn("wechat token failed, errcode={} errmsg={}", errcode, json.getString("errmsg"));
            throw new ServiceException("获取微信 access_token 失败");
        }
        String token = json.getString("access_token");
        Integer expiresIn = json.getInteger("expires_in");
        if (StringUtils.isEmpty(token))
        {
            throw new ServiceException("获取微信 access_token 失败");
        }
        long ttlMs = (expiresIn != null && expiresIn > 0 ? expiresIn * 1000L : 7200_000L) - EXPIRE_SKEW_MS;
        if (ttlMs < 60_000L)
        {
            ttlMs = 60_000L;
        }
        cachedToken.set(new CachedToken(token, System.currentTimeMillis() + ttlMs));
        return token;
    }

    private static String encode(String value)
    {
        try
        {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        }
        catch (Exception e)
        {
            return value;
        }
    }

    private static String tail(String openid)
    {
        if (openid == null || openid.length() < 6)
        {
            return "***";
        }
        return openid.substring(openid.length() - 6);
    }

    private static final class CachedToken
    {
        private final String token;
        private final long expireAtMs;

        private CachedToken(String token, long expireAtMs)
        {
            this.token = token;
            this.expireAtMs = expireAtMs;
        }
    }
}
