package com.wechat.wechat.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.wechat.wechat.module.AccessToken;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

/**
 * @title: wechat-service
 * @author: Young
 * @desc: 微信 - 微信基础数据工具类
 * @date: Created at 7/4 0004 16:19
 */
@Component
public class WeiXinUtil {

    private static String APPID;

    private static String SECRET;

    @Value("${custom.appId}")
    private void setAppId(String appId) {
        APPID = appId;
    }

    @Value("${custom.secret}")
    private void setSecret(String secret) {
        SECRET = secret;
    }

    /**
     * 从微信服务器获取新token
     *
     * @return
     */
    public static AccessToken getAccessToken() throws IOException {
        AccessToken accessToken = null;
        String url = Constants.GET_ACCESSTOKEN_URL.replace("APPID", APPID)
                .replace("APPSECRET", SECRET);
        //  HttpClient模拟访问获取token数据
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(url);
        HttpResponse response = httpClient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        String resMsg = EntityUtils.toString(entity, "UTF-8");
        //  将请求结果转为json对象
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = jsonParser.parse(resMsg).getAsJsonObject();
        if (Objects.nonNull(jsonObject)){
            accessToken = new AccessToken();
            accessToken.setId(UUID.randomUUID().toString().replaceAll("-", ""));
            accessToken.setAccessToken(jsonObject.get("access_token").getAsString());
            accessToken.setExpiresIn(jsonObject.get("expires_in").getAsInt());
            accessToken.setCreateTime(new Date());
        }
        return accessToken;
    }
}
