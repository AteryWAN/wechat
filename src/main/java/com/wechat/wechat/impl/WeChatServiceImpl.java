package com.wechat.wechat.impl;

import com.wechat.wechat.mapper.WeChatMapper;
import com.wechat.wechat.module.AccessToken;
import com.wechat.wechat.service.WeChatService;
import com.wechat.wechat.util.Constants;
import com.wechat.wechat.util.JsUtil;
import com.wechat.wechat.util.MessageUtil;
import com.wechat.wechat.util.WeiXinUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * @title: wechat-service
 * @author: Young
 * @desc: 微信
 * @date: Created at 7/3 0003 14:53
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class WeChatServiceImpl implements WeChatService {

    private static final Logger logger = LoggerFactory.getLogger(WeChatServiceImpl.class);

    @Value("{custom.tokenTime}")
    private String tokenTime;

    @Autowired
    private WeChatMapper weChatMapper;

    /**
     * 微信通讯入口
     *
     * @param request
     * @param response
     */
    @Override
    public void weChatEntry(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //  区分接口配置/接口调用
        if (Objects.isNull(request.getParameter("openid"))) {
            //  openid为空,接口配置请求
            MessageUtil messageUtil = MessageUtil.getInstance();
            PrintWriter printWriter;
            try {
                //  将微信请求数据并转换为map集合
                Map map = messageUtil.parseXml(request);
                //  微信加密签名
                String signature = request.getParameter("signature");
                //  时间戳
                String timestamp = request.getParameter("timestamp");
                //  随机数
                String nonce = request.getParameter("nonce");
                //  随机字符串
                String echostr = request.getParameter("echostr");
                //  通过检验signature对请求进行校验，若校验成功则原样返回echostr，表示接入成功，否则接入失败
                if (JsUtil.checkSignature(timestamp, nonce, signature)) {
                    //  验证一致,打印echostr
                    printWriter = response.getWriter();
                    printWriter.write(echostr);
                    printWriter.flush();
                    printWriter.close();
                    System.out.println("校验成功,echostr: " + echostr);
                } else {
                    System.out.println("校验不成功!");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            //  用户访问请求
            operate(request, response);
        }
    }

    /**
     * 用户请求操作
     *
     * @param request
     * @param response
     */
    public void operate(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        //  响应消息数据
        String responseMsg = null;
        PrintWriter printWriter = response.getWriter();
        //  获取请求数据
        MessageUtil messageUtil = MessageUtil.getInstance();
        Map<String, String> requestMap = messageUtil.parseXml(request);
        //  获取token
        String token = getToken();
        requestMap.put("token", token);
        //  分别获取基本信息
        // 发送方帐号（open_id）
        String fromUserName = requestMap.get("FromUserName");
        // 公众帐号
        String toUserName = requestMap.get("ToUserName");
        // 消息类型
        String msgType = requestMap.get("MsgType");
        // 事件类型
        String eventType = requestMap.get("Event");
        // 消息创建时间
        String createTime = requestMap.get("CreateTime");

        //  根据不同msgType进行不同响应
        //  有msgid的消息推荐使用msgid排重。事件类型消息推荐使用FromUserName + CreateTime 排重。
        if (MessageUtil.MESSAGE_TYPE_EVENT.equals(msgType)) {
            //  推送event消息类型
            eventMsg(requestMap, responseMsg);
        } else if (MessageUtil.MESSAGE_TYPE_TEXT.equals(msgType)) {
            //  文字消息类型
            textMsg(requestMap, responseMsg);
        } else if (MessageUtil.MESSAGE_TYPE_IMAGE.equals(msgType)) {
            //  图片消息类型
            imageMsg(requestMap, responseMsg);
        } else if (MessageUtil.MESSAGE_TYPE_VIDEO.equals(msgType)) {
            //  视频消息类型
            videoMsg(requestMap, responseMsg);
        } else if (MessageUtil.MESSAGE_TYPE_LOCATION.equals(msgType)) {
            //  上报地理位置
            locationMsg(requestMap, responseMsg);
        }

        //  输出响应消息内容反馈
        if (Strings.isNotEmpty(responseMsg)) {
            printWriter.write(responseMsg);
        }
        printWriter.flush();
        printWriter.close();

    }


    /**
     * event消息类型
     *
     * @param requestMap
     * @param responseMsg
     * @return
     */
    private String eventMsg(Map<String, String> requestMap, String responseMsg) {

        return null;
    }

    /**
     * text消息类型
     *
     * @param requestMap
     * @param responseMsg
     * @return
     */
    private String textMsg(Map<String, String> requestMap, String responseMsg) {
        String content = "测试文字消息反馈";
        //  处理文字消息返回内容
        responseMsg = MessageUtil.initText(requestMap.get("fromUserName"), requestMap.get("toUserName"), content);
        return responseMsg;
    }

    /**
     * image消息类型
     *
     * @param requestMap
     * @param responseMsg
     * @return
     */
    private String imageMsg(Map<String, String> requestMap, String responseMsg) {

        return null;
    }

    /**
     * video消息类型
     *
     * @param requestMap
     * @param responseMsg
     * @return
     */
    private String videoMsg(Map<String, String> requestMap, String responseMsg) {

        return null;
    }

    /**
     * location消息类型(用户向公众号发送的地理位置)
     * 区别于用户进入公众号时自动进行的地理位置上报操作(此操作属于event类型)
     *
     * @param requestMap
     * @param responseMsg
     * @return
     */
    private String locationMsg(Map<String, String> requestMap, String responseMsg) {
        //  地理位置维度
        String x = requestMap.get("Location_X");
        //  地理位置经度
        String y = requestMap.get("Location_Y");
        //  地图缩放大小
        String scale = requestMap.get("Scale");
        //  地理位置信息
        String label = requestMap.get("Label");
        responseMsg = MessageUtil.initText(requestMap.get("fromUserName"), requestMap.get("toUserName"), label);
        return responseMsg;
    }

    /**
     * 获取token数据(有效期7200,推荐3600以保证故障率)
     *
     * @return
     */
    private String getToken() throws IOException {
        AccessToken accessToken;
        //  获取当前数据库最新token对象,并进行有效期比对
        accessToken = weChatMapper.getToken();
        if (Objects.nonNull(accessToken)) {
            Date nowDate = new Date();
            Date tokenDate = accessToken.getCreateTime();
            if ((nowDate.getTime() - tokenDate.getTime()) > Integer.valueOf(tokenTime)) {
                //  当前时间和token存储时间差值大于设定的标准值,则重新获取
                accessToken = WeiXinUtil.getAccessToken();
                weChatMapper.newToken(accessToken);
            }
        } else {
            accessToken = WeiXinUtil.getAccessToken();
            weChatMapper.newToken(accessToken);
        }
        return accessToken.getAccessToken();
    }


    /**
     * 根据经纬度获取地理位置
     * HttpURLConnection
     *
     * @param X
     * @param Y
     * @return
     */
    private String addressInfo(String X, String Y) {
        // latitude 纬度, longitude 经度, type 001 (100代表道路，010代表POI，001代表门址，111可以同时显示前三项)--001报错
        String urlString = Constants.ADDRESS_URL.replace("LATITUDE", X).
                replace("LONGITUDE", Y).replace("TYPE", "010");
        String res = "";
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;
            while ((line = in.readLine()) != null) {
                res += line + "\n";
            }
            in.close();
        } catch (Exception e) {
            System.out.println("error is " + e.getMessage());
        }
        return res;
    }

    /**
     * 根据经纬度获取地理位置
     * httpClient
     *
     * @param longitude
     * @param latitude
     * @return
     */
    private String addressInfo2(String longitude, String latitude) throws IOException {
        // latitude 纬度, longitude 经度, type 001 (100代表道路，010代表POI，001代表门址，111可以同时显示前三项)--001报错
        String url = Constants.ADDRESS_URL.replace("LATITUDE", latitude).
                replace("LONGITUDE", longitude).replace("TYPE", "010");
        StringBuffer res = new StringBuffer();
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(url);
        HttpResponse response = httpClient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        //  读取数据
        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()));
        while ((line = br.readLine()) != null) {
            res.append(line + "\n");
        }
        return res.toString();
    }


}
