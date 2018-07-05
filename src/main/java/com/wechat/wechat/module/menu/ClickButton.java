package com.wechat.wechat.module.menu;

/**
 * @title: wechat-service
 * @author: Young
 * @desc: 微信 - 点击菜单
 * @date: Created at 7/5 0005 14:09
 */
public class ClickButton extends Button {
    /**
     * click等点击类型必须
     * 菜单KEY值，用于消息接口推送，不超过128字节
     */
    private String key;

    public String getKey() {

        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
