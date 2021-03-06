package com.wechat.wechat.util;

/**
 * @title: wechat-service
 * @author: Young
 * @desc: 微信 - 常量定义
 * @date: Created at 7/3 0003 15:03
 */
public class Constants {
    /**
     * APPID-测试号
     */
    public static String APPID = "wxfb6f5be2f2c10ef5";

    /**
     * SECRET-测试号
     */
    public static String SECRET = "16d8cd5d3f4ad13fb1f4bfd8d128c2b1";


    /**
     * 获取ACCESS_TOKEN接口
     */
    public static String GET_ACCESSTOKEN_URL =
            "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET";

    /**
     * 微信获取用户信息
     */
    public static String GET_USERINFO_URL =
            "https://api.weixin.qq.com/cgi-bin/user/info?access_token=ACCESS_TOKEN&openid=OPENID&lang=zh_CN";

    /**
     * 微信获取ticket
     */
    public static String GET_TICKET_URL =
            "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=ACCESSTOKEN&type=jsapi";

    /**
     * 获取关注者列表
     */
    public static String GET_FOCUSLIST_URL =
            "https://api.weixin.qq.com/cgi-bin/user/get?access_token=ACCESSTOKEN&next_openid=";

    /**
     * 创建临时二维码ticket
     */
    public static String GET_QRCODE_URL =
            "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=TOKEN";

    /**
     * 打印公众号二维码
     */
    public static String GET_SHOWCODE_URL =
            "https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=TICKET";


    /**
     * 二维码参数json
     */
    public static String CODE_PARAMS_STR =
            "{\"expire_seconds\": EXPIRE_SECONDS, \"action_name\": \"QR_STR_SCENE\", \"action_info\": {\"scene\": {\"scene_str\": \"SCENE_STR\"}}}";

    /**
     * 手机号验证
     */
    public static final String REGEX_MOBILE = "^[1][3,4,5,7,8][0-9]{9}$";

    /**
     * 网页授权登陆URL
     */
    public static String AUTH_URL =
            "https://open.weixin.qq.com/connect/oauth2/authorize?appid=APPID&amp;" +
                    "redirect_uri=REDIRECT_URI&amp;response_type=CODE&amp;scope=SCOPE&amp;state=STATE#wechat_redirect";

    /**
     * 授权登陆跳转URL
     * 转义
     */
    public static String REDIRECT_URL = "http%3a%2f%2fwww.baidu.com";

    /**
     * 网页授权code获取基本信息URL
     */
    public static String AUTH_INFO_URL =
            "https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code";


    /**
     * 阿里地图获取地理位置
     */
    public static String ADDRESS_URL =
            "http://gc.ditu.aliyun.com/regeocoding?l=LATITUDE,LONGITUDE&type=TYPE";

    /**
     * 自定义菜单
     */
    public static String CREATE_MENU_URL = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token=ACCESS_TOKEN";

    /**
     * 查询菜单
     */
    public static String GET_MENU_URL = "https://api.weixin.qq.com/cgi-bin/menu/get?access_token=ACCESS_TOKEN";

    /**
     * 删除菜单
     */
    public static String DELETE_MENU_URL = "https://api.weixin.qq.com/cgi-bin/menu/delete?access_token=ACCESS_TOKEN";

}
