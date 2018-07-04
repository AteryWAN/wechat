package com.wechat.wechat.web;

import com.wechat.wechat.service.WeChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @title: wechat-service
 * @author: Young
 * @desc: 微信
 * @date: Created at 7/3 0003 14:52
 */
@RestController
@RequestMapping(value = "/wechat")
public class WeChatController {

    @Autowired
    private WeChatService weChatService;

    /**
     * 微信通讯入口
     *
     * @param request
     * @param response
     */
    @RequestMapping(value = "/entry")
    public void weChatEntry(HttpServletRequest request, HttpServletResponse response) throws Exception {
        weChatService.weChatEntry(request, response);
    }
}
