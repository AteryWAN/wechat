package com.wechat.wechat.module.menu;

/**
 * @title: wechat-service
 * @author: Young
 * @desc: 微信 - 菜单实体
 * @date: Created at 7/5 0005 14:09
 */
public class Menu {
    /**
     * 一级菜单数组，个数应为1~3个
     */
    private Button[] button;

    public Button[] getButton() {
        return button;
    }

    public void setButton(Button[] button) {
        this.button = button;
    }
}
