package com.wechat.wechat.impl;

import com.wechat.wechat.service.WeChatService;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @title: wechat-service
 * @author: Young
 * @desc: 微信
 * @date: Created at 7/3 0003 14:53
 */
@Service
public class WeChatServiceImpl implements WeChatService{
    /**
     * 微信通讯入口
     *
     * @param request
     * @param response
     */
    @Override
    public void weChatEntry(HttpServletRequest request, HttpServletResponse response) {
        //  区分接口配置/接口调用

    }
}
