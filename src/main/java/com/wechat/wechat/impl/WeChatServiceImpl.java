package com.wechat.wechat.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.wechat.wechat.mapper.WeChatMapper;
import com.wechat.wechat.module.AccessToken;
import com.wechat.wechat.module.WeChatEntity;
import com.wechat.wechat.module.menu.Button;
import com.wechat.wechat.module.menu.ClickButton;
import com.wechat.wechat.module.menu.Menu;
import com.wechat.wechat.module.menu.ViewButton;
import com.wechat.wechat.service.WeChatService;
import com.wechat.wechat.util.Constants;
import com.wechat.wechat.util.JsUtil;
import com.wechat.wechat.util.MessageUtil;
import com.wechat.wechat.util.WeiXinUtil;
import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.javase.QRCode;
import net.sf.json.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

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
        //  关闭
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
        //  提取公共数据
        //  发送方帐号（open_id）
        String fromUserName = requestMap.get("FromUserName");
        //  公众帐号
        String toUserName = requestMap.get("ToUserName");
        //  消息类型
        String msgType = requestMap.get("MsgType");
        //  事件类型
        String eventType = requestMap.get("Event");
        //  消息创建时间
        String createTime = requestMap.get("CreateTime");

        switch (eventType) {
            case MessageUtil.EVENT_TYPE_SUBSCRIBE:
                System.out.println("关注");
                responseMsg = MessageUtil.initText(fromUserName, toUserName, "感谢您的关注!");
                break;
            case MessageUtil.EVENT_TYPE_UNSUBSCRIBE:
                System.out.println("用户(" + fromUserName + ")取消关注");
                break;
            case MessageUtil.EVENT_TYPE_SCAN:
                System.out.println("扫码");
                String content = "二维码参数: " + requestMap.get("EventKey");
                responseMsg = MessageUtil.initText(fromUserName, toUserName, content);
                break;
            case MessageUtil.EVENT_TYPE_LOCATION:
                System.out.println("地理位置");
                try {
                    responseMsg = addressInfo2(requestMap.get("longtitue"), requestMap.get("latitude"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case MessageUtil.EVENT_TYPE_CLICK:
                System.out.println("点击事件");
                break;
            case MessageUtil.EVENT_TYPE_VIEW:
                System.out.println("链接事件");
                break;
            default:
                System.out.println("默认");
                break;
        }
        return responseMsg;
    }

    /**
     * text消息类型
     *
     * @param requestMap
     * @param responseMsg
     * @return
     */
    private String textMsg(Map<String, String> requestMap, String responseMsg) throws IOException {
        String content;
        if ("创建菜单".equals(requestMap.get("content"))) {
            //  根据返回status判定请求成功/失败
            int status = createMenu(requestMap.get("token"));
            if (status == HttpStatus.SC_OK) {
                content = "菜单创建成功!";
                System.out.println(content);
            } else {
                content = "创建菜单失败!错误码: " + status;
                System.out.println(content);
            }
            responseMsg = MessageUtil.initText(requestMap.get("FromUserName"), requestMap.get("ToUserName"), content);
        } else if ("查询菜单".equals(requestMap.get("content"))) {
            //  转换数据为json格式并输出
            JSONObject jsonObject = getMenuInfo(requestMap.get("token"));
            content = jsonObject.toString();
            responseMsg = MessageUtil.initText(requestMap.get("FromUserName"), requestMap.get("ToUserName"), content);
        } else if ("删除菜单".equals(requestMap.get("content"))) {
            int status = deleteMenu(requestMap.get("token"));
            if (status == HttpStatus.SC_OK) {
                content = "菜单删除成功!";
                System.out.println(content);
            } else {
                content = "删除菜单失败!错误码: " + status;
                System.out.println(content);
            }
            responseMsg = MessageUtil.initText(requestMap.get("FromUserName"), requestMap.get("ToUserName"), content);
        } else {
            content = "测试文字消息反馈";
            //  处理文字消息返回内容
            responseMsg = MessageUtil.initText(requestMap.get("FromUserName"), requestMap.get("ToUserName"), content);
        }

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
        responseMsg = MessageUtil.initText(requestMap.get("FromUserName"), requestMap.get("ToUserName"), label);
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
            //  创建http请求连接
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            //  读取输入流信息
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

    /**
     * 创建自定义菜单
     *
     * @param token
     * @return
     * @throws IOException
     */
    private int createMenu(String token) throws IOException {
        String url = Constants.CREATE_MENU_URL.replace("ACCESS_TOKEN", token);
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(url);
        StringEntity entity = new StringEntity(initMenu().toString(), "UTF-8");
        httpPost.setEntity(entity);
        HttpResponse response = httpClient.execute(httpPost);
        int status = response.getStatusLine().getStatusCode();
        return status;
    }

    /**
     * 初始化菜单数据
     *
     * @return
     */
    private JSONObject initMenu() {
        //  创建菜单主体对象
        Menu menu = new Menu();

        //  点击类型子菜单
        ClickButton clickButton11 = new ClickButton();
        clickButton11.setType("click");
        clickButton11.setName("click11");
        clickButton11.setKey("key11");

        ClickButton clickButton12 = new ClickButton();
        clickButton12.setType("click");
        clickButton12.setName("click12");
        clickButton12.setKey("key12");

        //  链接类型子菜单
        ViewButton viewButton21 = new ViewButton();
        viewButton21.setType("view");
        viewButton21.setName("view21");
        viewButton21.setUrl("https://www.baidu.com");

        ViewButton viewButton22 = new ViewButton();
        viewButton22.setType("view");
        viewButton22.setName("view22");
        viewButton22.setUrl("https://cn.bing.com");

        //  封装一级菜单数据
        Button button1 = new Button();
        button1.setName("btn1");
        button1.setSub_button(new Button[]{clickButton11, clickButton12});

        Button button2 = new Button();
        button2.setName("btn2");
        button2.setSub_button(new Button[]{viewButton21, viewButton22});

        //  封装菜单主体对象数据
        menu.setButton(new Button[]{button1, button2});

        //  转为json格式返回
        JSONObject jsonObject = JSONObject.fromObject(menu);
        return jsonObject;
    }

    /**
     * 查询自定义菜单
     *
     * @param token
     * @return
     * @throws IOException
     */
    private JSONObject getMenuInfo(String token) throws IOException {
        String url = Constants.GET_MENU_URL.replace("ACCESS_TOKEN", token);
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(url);
        HttpResponse response = httpClient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        JSONObject jsonObject = JSONObject.fromObject(entity);
        return jsonObject;
    }

    /**
     * 删除自定义菜单
     *
     * @param token
     * @return
     * @throws IOException
     */
    private int deleteMenu(String token) throws IOException {
        String url = Constants.DELETE_MENU_URL.replace("ACCESS_TOKEN", token);
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(url);
        HttpResponse response = httpClient.execute(httpGet);
        int status = response.getStatusLine().getStatusCode();
        return status;
    }

    /**
     * 测试自动生成循环变量
     *
     * @param count
     */
    private void loopVariables(int count) {
        Menu menu = new Menu();

        Map<String, ClickButton> cb = new HashMap<>();
        ClickButton clickButton;
        for (int i = 0; i < count; i++) {
            clickButton = new ClickButton();
            clickButton.setType("click");
            clickButton.setName("click1" + i);
            clickButton.setKey("key1" + i);
            cb.put("click1" + i, clickButton);
        }

        Map<String, ViewButton> vb = new HashMap<>();
        ViewButton viewButton;
        for (int i = 0; i < count; i++) {
            viewButton = new ViewButton();
            viewButton.setType("view");
            viewButton.setName("view2" + i);
            viewButton.setUrl("https://www.baidu.com");
            vb.put("view2" + i, viewButton);
        }

        Map<String, Button> b = new HashMap<>();
        Button button;
        for (int i = 0; i < count; i++) {
            button = new Button();
            Button[] subButton;
            for (int j = 0; j < count; j++) {
                subButton = new Button[]{};
                if (i == 0) {
                    button.setName("btn" + i);
                    subButton[j] = cb.get("click" + i + j);
                }
                if (i == 1) {
                    button.setName("btn" + i);
                    button.setSub_button(new Button[]{vb.get("click" + i + j)});
                }
                button.setSub_button(subButton);
            }
            b.put("button" + i, button);
        }

        for (int i = 0; i < count; i++) {
            menu.setButton(new Button[]{b.get("button" + i)});
        }
    }

    /**
     * 获取用户基本信息
     *
     * @param requestMap
     * @return
     * @throws IOException
     */
    private WeChatEntity userInfo(Map<String, String> requestMap) throws IOException {
        WeChatEntity weChatEntity = new WeChatEntity();
        //  获取token,openId
        String token = requestMap.get("token");
        String openId = requestMap.get("FromUserName");
        //  调用接口获取用户基本信息
        String url = Constants.GET_USERINFO_URL.replace("ACCESSTOKEN", token).replace("OPENID", openId);
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(url);
        HttpResponse response = httpClient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        String jsonStr = EntityUtils.toString(entity, "UTF-8");
        //  转为json格式
        JsonParser jsonParser = new JsonParser();
        JsonObject json = jsonParser.parse(jsonStr).getAsJsonObject();
        //  封装WeChatEntity对象
        if (Objects.nonNull(json)) {
            weChatEntity.setId(UUID.randomUUID().toString());
            weChatEntity.setSubscribe(json.get("subscribe").getAsInt());
            weChatEntity.setOpenid(json.get("openid").getAsString());
            weChatEntity.setNickname(json.get("nickname").getAsString());
            weChatEntity.setSex(json.get("sex").getAsInt());
            weChatEntity.setLanguage(json.get("language").getAsString());
            weChatEntity.setCity(json.get("city").getAsString());
            weChatEntity.setProvince(json.get("province").getAsString());
            weChatEntity.setCountry(json.get("country").getAsString());
            weChatEntity.setHeadimgurl(json.get("headimgurl").getAsString());
            weChatEntity.setSubscribe_time(new Timestamp(json.get("subscribe_time").getAsLong()));
            weChatEntity.setRemark(json.get("remark").getAsString());
            weChatEntity.setGroupid(json.get("groupid").getAsInt());
            weChatEntity.setTagid_list((List<Integer>) json.get("tagid_list"));
        } else {
            System.out.println("获取用户信息失败!");
        }
        return weChatEntity;
    }

    /**
     * 生成场景值二维码
     *
     * @param codeInfo
     * @param response
     * @throws IOException
     */
    @Override
    public void createQrCode(String codeInfo, HttpServletResponse response) throws IOException {
        //  调用临时二维码生成接口
        String token = getToken();
        String codeUrl = Constants.GET_QRCODE_URL.replace("TOKEN", token);
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(codeUrl);
        //  处理post请求参数
        String codeParams = Constants.CODE_PARAMS_STR.replace("EXPIRE_SECONDS", "604800").replace("SCENE_STR", codeInfo);
        StringEntity params = new StringEntity(new String(codeParams), "UTF-8");
        httpPost.setEntity(params);
        HttpResponse res = httpClient.execute(httpPost);
        HttpEntity entity = res.getEntity();
        //  解析返回的json数据
        JsonParser jsonParser = new JsonParser();
        String jsonStr = EntityUtils.toString(entity, "UTF-8");
        JsonObject ticketJson = jsonParser.parse(jsonStr).getAsJsonObject();
        String ticket;
        String url = null;
        //  通过ticket换取二维码(https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=TICKET)
        if (Objects.nonNull(ticketJson)) {
            ticket = ticketJson.get("ticket").getAsString();
            url = ticketJson.get("url").getAsString();
        }
        //  利用qrcode库生成二维码(使用url)
        //  设置字节数组输出流的格式,大小等
        ByteArrayOutputStream out = QRCode.from(url).to(ImageType.PNG).withSize(350, 350).stream();
        response.setContentType("image/png");
        response.setContentLength(out.size());
        OutputStream outStream = response.getOutputStream();
        outStream.write(out.toByteArray());
        outStream.flush();
        outStream.close();
    }
}
