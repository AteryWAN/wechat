package com.wechat.wechat.module.menu;

/**
 * @title: wechat-service
 * @author: Young
 * @desc: 微信 - 网页链接菜单
 * @date: Created at 7/5 0005 14:09
 */
public class ViewButton extends Button {
    /**
     * view、miniprogram类型必须
     * 网页 链接，用户点击菜单可打开链接，不超过1024字节。 type为miniprogram时，不支持小程序的老版本客户端将打开本url。
     */
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
