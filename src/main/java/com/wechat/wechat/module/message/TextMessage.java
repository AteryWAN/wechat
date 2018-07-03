package com.wechat.wechat.module.message;

/**
 * @title: wechat-service
 * @author: Young
 * @desc: 微信 - 文本消息
 * @date: Created at 7/3 0003 14:53
 */
public class TextMessage extends BaseMessage {
    /**
     * 回复的消息内容（换行：在content中能够换行，微信客户端就支持换行显示）
     */
    private String Content;


    public String getContent() {
        return Content;
    }

    public void setContent(String content) {
        Content = content;
    }
}
