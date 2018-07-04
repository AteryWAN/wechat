package com.wechat.wechat.util;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.util.QuickWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.XppDriver;
import com.wechat.wechat.module.message.ImgVoiceMessage;
import com.wechat.wechat.module.message.MusicMessage;
import com.wechat.wechat.module.message.NewsMessage;
import com.wechat.wechat.module.message.SubNews;
import com.wechat.wechat.module.message.TextMessage;
import com.wechat.wechat.module.message.VideoMessage;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @title: wechat-service
 * @author: Young
 * @desc: 微信 - 消息转换类工具
 * @date: Created at 7/3 0003 15:11
 */
public class MessageUtil {

    /**
     * 消息类型(MsgType):
     event
     text
     image
     voice
     video
     shortvideo
     location
     link
     music
     news
     */

    /**
     * 消息类型：推送
     */
    public static final String MESSAGE_TYPE_EVENT = "event";

    /**
     * 消息类型：文本
     */
    public static final String MESSAGE_TYPE_TEXT = "text";

    /**
     * 消息类型：图片
     */
    public static final String MESSAGE_TYPE_IMAGE = "image";

    /**
     * 消息类型：音频
     */
    public static final String MESSAGE_TYPE_VOICE = "voice";

    /**
     * 消息类型：视频
     */
    public static final String MESSAGE_TYPE_VIDEO = "video";

    /**
     * 消息类型：小视频
     */
    public static final String MESSAGE_TYPE_SHORTVIDEO = "shortvideo";

    /**
     * 消息类型：地理位置
     */
    public static final String MESSAGE_TYPE_LOCATION = "location";

    /**
     * 消息类型：链接
     */
    public static final String MESSAGE_TYPE_LINK = "link";

    /**
     * 消息类型：音乐
     */
    public static final String MESSAGE_TYPE_MUSIC = "music";

    /**
     * 消息类型：图文
     */
    public static final String MESSAGE_TYPE_NEWS = "news";



    /**  而消息类型(Event)下的事件类型：
     subscribe(订阅)、
     unsubscribe(取消订阅)
     SCAN
     LOCATION
     CLICK
     VIEW
     */

    /**
     * 事件类型：subscribe(订阅)
     */
    public static final String EVENT_TYPE_SUBSCRIBE = "subscribe";

    /**
     * 事件类型：unsubscribe(取消订阅)
     */
    public static final String EVENT_TYPE_UNSUBSCRIBE = "unsubscribe";

    /**
     * 事件类型：SCAN(扫描带参数二维码事件)
     */
    public static final String EVENT_TYPE_SCAN = "SCAN";

    /**
     * 事件类型：LOCATION(上报地理位置事件)
     */
    public static final String EVENT_TYPE_LOCATION = "LOCATION";

    /**
     * 事件类型：CLICK(点击菜单拉取消息时的事件推送)
     */
    public static final String EVENT_TYPE_CLICK = "CLICK";

    /**
     * 事件类型：VIEW(点击菜单跳转链接时的事件推送)
     */
    public static final String EVENT_TYPE_VIEW = "VIEW";






    /**
     * 定义一个私有的静态全局变量来保存该类的唯一实例
     */
    private static MessageUtil messageUtil;

    /**
     * 构造函数必须是私有的
     * 这样在外部便无法使用 new 来创建该类的实例
     */
    private MessageUtil() {

    }

    /**
     * 定义一个全局访问点
     * 设置为静态方法
     * 则在类的外部便无需实例化就可以调用该方法
     *
     * @return
     */
    public static MessageUtil getInstance() {
        //这里可以保证只实例化一次
        //即在第一次调用时实例化
        //以后调用便不会再实例化
        if (messageUtil == null) {
            messageUtil = new MessageUtil();
        }
        return messageUtil;

    }

    /**
     * 解析微信发来的请求（XML）
     * XML转成Map集合
     *
     * @param request
     * @return
     * @throws Exception
     */
    public Map<String, String> parseXml(HttpServletRequest request) throws Exception {
        //  将微信数据存入Map集合
        Map<String, String> map = new HashMap<>();
        //  读取输入流
        InputStream is = request.getInputStream();
        //  读取文件信息
        SAXReader reader = new SAXReader();
        Document document = reader.read(is);
        //  获取xml格式文件信息
        String requestXML = document.asXML();
        //  将xml信息按">"隔开
        String subXML = requestXML.split(">")[0] + ">";
        requestXML = requestXML.substring(subXML.length());
        //  得到根元素
        Element elementRoot = document.getRootElement();
        //  得到根元素的全部子节点
        List<Element> elements = elementRoot.elements();
        //  遍历子节点信息放入map集合
        for (Element element : elements) {
            map.put(element.getName(), element.getText());
        }
        map.put("requestXML", requestXML);
        //  释放资源
        is.close();
        return map;
    }

    /**
     * 将请求的数据转化为xml
     *
     * @param request
     * @return
     */
    public String parseMsgToXml(HttpServletRequest request) {
        String responseMsg = null;
        try {
            //  读取输入流
            InputStream is = request.getInputStream();
            //  获取输入流的有效字节
            int size = is.available();
            //  读入数据
            byte[] bytes = new byte[size];
            is.read(bytes);
            responseMsg = new String(bytes, "UTF-8");
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseMsg;
    }

    /**

     * 推送对象转换成xml

     *

     * @param scanSendMessage 推送消息对象

     * @return xml

     */

//    public String scanSendMessageToXml(ScanSendMessage scanSendMessage) {
//
//        xstream.alias("xml", scanSendMessage.getClass());
//
//        return xstream.toXML(scanSendMessage);
//
//    }


    /**
     * 文本消息对象转换成xml
     *
     * @param textMessage 文本消息对象
     * @return xml
     */
    public String textMessageToXml(TextMessage textMessage) {
        xstream.alias("xml", textMessage.getClass());
        return xstream.toXML(textMessage);
    }

    /**
     * 处理文本消息返回数据
     *
     * @param fromUserName
     * @param toUserName
     * @param content
     * @return
     */
    public static String initText(String fromUserName, String toUserName, String content) {
        MessageUtil messageUtil = MessageUtil.getInstance();
        TextMessage textMessage = new TextMessage();
        textMessage.setToUserName(fromUserName);
        textMessage.setFromUserName(toUserName);
        textMessage.setCreateTime(new Date());
        textMessage.setMsgType(MESSAGE_TYPE_TEXT);
        textMessage.setContent(content);
        return messageUtil.textMessageToXml(textMessage);
    }


    /**
     * 音乐消息对象转换成xml
     *
     * @param musicMessage 音乐消息对象
     * @return xml
     */
    public String musicMessageToXml(MusicMessage musicMessage) {
        xstream.alias("xml", musicMessage.getClass());
        return xstream.toXML(musicMessage);

    }

    /**
     * 图文消息对象转换成xml
     *
     * @param newsMessage 图文消息对象
     * @return xml
     */

    public String newsMessageToXml(NewsMessage newsMessage) {
        xstream.alias("xml", newsMessage.getClass());
        xstream.alias("item", new SubNews().getClass());
        return xstream.toXML(newsMessage);

    }

    /**
     * 处理图文消息返回数据
     *
     * @param fromUserName
     * @param toUserName
     * @param picUrl
     * @param requestUrl
     * @return
     */
    public static String initNews(String fromUserName, String toUserName, String picUrl, String requestUrl) {
        MessageUtil messageUtil = MessageUtil.getInstance();

        List<SubNews> newsList = new ArrayList<SubNews>();
        NewsMessage newsMessage = new NewsMessage();
        SubNews news = new SubNews();
        news.setTitle("手机绑定");
        news.setDescription("请点击进行手机号码绑定");
        news.setPicUrl(picUrl);
        news.setUrl(requestUrl);
        newsList.add(news);

        newsMessage.setToUserName(fromUserName);
        newsMessage.setFromUserName(toUserName);
        newsMessage.setCreateTime(new Date());
        newsMessage.setMsgType(MESSAGE_TYPE_NEWS);
        newsMessage.setArticles(newsList);
        newsMessage.setArticleCount(newsList.size());

        return messageUtil.newsMessageToXml(newsMessage);
    }

    /**
     * 图片/语音消息转换成XML
     *
     * @param imgVoiceMessage
     * @return
     */
    public String imgVoiceToXML(ImgVoiceMessage imgVoiceMessage) {
        xstream.alias("xml", imgVoiceMessage.getClass());
        return xstream.toXML(imgVoiceMessage);
    }


    /**
     * 视频消息转换成XML
     *
     * @param videoMessage
     * @return
     */
    public String videoToXML(VideoMessage videoMessage) {
        xstream.alias("xml", videoMessage.getClass());
        return xstream.toXML(videoMessage);
    }


    /**
     * 扩展xstream，使其支持CDATA块
     */
    private XStream xstream = new XStream(new XppDriver() {
        @Override
        public HierarchicalStreamWriter createWriter(Writer out) {
            return new PrettyPrintWriter(out) {
                // 对全部xml节点的转换都添加CDATA标记
                boolean cdata = true;

                @Override
                public void startNode(String name, Class clazz) {
                    super.startNode(name, clazz);
                    //  创建时间不放入cdata块
                    if ("CreateTime".equals(name)) {
                        cdata = false;
                    }
                }

                @Override
                protected void writeText(QuickWriter writer, String text) {
                    if (cdata) {
                        writer.write("<![CDATA[");
                        writer.write(text);
                        writer.write("]]>");
                    } else {
                        writer.write(text);
                    }
                }
            };
        }
    });
}
