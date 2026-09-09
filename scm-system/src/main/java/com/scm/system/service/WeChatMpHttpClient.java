package com.scm.system.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.scm.common.exception.ServiceException;

/**
 * 调用微信开放接口的 GET/POST。
 */
@Component
public class WeChatMpHttpClient
{
    private static final Logger log = LoggerFactory.getLogger(WeChatMpHttpClient.class);

    private static final int TIMEOUT_MS = 8000;

    public String get(String url)
    {
        return request("GET", url, null);
    }

    public String postJson(String url, String jsonBody)
    {
        return request("POST", url, jsonBody);
    }

    private String request(String method, String url, String jsonBody)
    {
        HttpURLConnection conn = null;
        BufferedReader reader = null;
        try
        {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod(method);
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.setRequestProperty("Accept", "application/json");
            if (jsonBody != null)
            {
                byte[] bytes = jsonBody.getBytes(StandardCharsets.UTF_8);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Content-Length", String.valueOf(bytes.length));
                OutputStream out = conn.getOutputStream();
                out.write(bytes);
                out.flush();
                out.close();
            }
            int status = conn.getResponseCode();
            InputStream in = status >= 400 ? conn.getErrorStream() : conn.getInputStream();
            if (in == null)
            {
                throw new ServiceException("微信接口无响应");
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
            log.error("wechat http {} error, url={}", method, url, e);
            throw new ServiceException("调用微信接口失败，请稍后重试");
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
