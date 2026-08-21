package com.scm.system.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
 * 微信服务号网页授权：用一次性 code 换取 openid。
 */
@Service
public class WeChatMpOauthService
{
    private static final Logger log = LoggerFactory.getLogger(WeChatMpOauthService.class);

    private static final String OAUTH_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token";
    private static final int TIMEOUT_MS = 8000;

    @Autowired
    private WeChatMpProperties weChatMpProperties;

    public String exchangeCodeForOpenid(String code)
    {
        if (!weChatMpProperties.isConfigured())
        {
            throw new ServiceException("未配置微信服务号参数");
        }
        if (StringUtils.isEmpty(code))
        {
            throw new ServiceException("请从微信服务号菜单进入");
        }
        String body = httpGet(buildOauthUrl(code));
        JSONObject json = JSON.parseObject(body);
        if (json == null)
        {
            throw new ServiceException("微信授权失败，请从服务号菜单重新进入");
        }
        Integer errcode = json.getInteger("errcode");
        if (errcode != null && errcode != 0)
        {
            log.warn("wechat oauth failed, errcode={}", errcode);
            if (Integer.valueOf(40029).equals(errcode) || Integer.valueOf(40163).equals(errcode))
            {
                throw new ServiceException("微信授权已失效，请从服务号菜单重新进入");
            }
            throw new ServiceException("微信授权失败，请从服务号菜单重新进入");
        }
        String openid = json.getString("openid");
        if (StringUtils.isEmpty(openid))
        {
            throw new ServiceException("微信授权失败，请从服务号菜单重新进入");
        }
        return openid;
    }

    private String buildOauthUrl(String code)
    {
        try
        {
            return OAUTH_TOKEN_URL
                + "?appid=" + URLEncoder.encode(weChatMpProperties.getAppId(), StandardCharsets.UTF_8.name())
                + "&secret=" + URLEncoder.encode(weChatMpProperties.getAppSecret(), StandardCharsets.UTF_8.name())
                + "&code=" + URLEncoder.encode(code, StandardCharsets.UTF_8.name())
                + "&grant_type=authorization_code";
        }
        catch (Exception e)
        {
            throw new ServiceException("微信授权失败，请稍后重试");
        }
    }

    private String httpGet(String url)
    {
        HttpURLConnection conn = null;
        BufferedReader reader = null;
        try
        {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.setRequestProperty("Accept", "application/json");
            int status = conn.getResponseCode();
            InputStream in = status >= 400 ? conn.getErrorStream() : conn.getInputStream();
            if (in == null)
            {
                throw new ServiceException("微信授权失败，请稍后重试");
            }
            reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
            {
                result.append(line);
            }
            return result.toString();
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            log.error("wechat oauth http error", e);
            throw new ServiceException("微信授权失败，请稍后重试");
        }
        finally
        {
            if (reader != null)
            {
                try
                {
                    reader.close();
                }
                catch (Exception ignored)
                {
                }
            }
            if (conn != null)
            {
                conn.disconnect();
            }
        }
    }
}
