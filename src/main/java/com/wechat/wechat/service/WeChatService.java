package com.wechat.wechat.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @title: wechat-service
 * @author: Young
 * @desc: 微信
 * @date: Created at 7/3 0003 14:52
 */
public interface WeChatService {

    /**
     * 微信通讯入口
     *
     * @param request
     * @param response
     */
    void weChatEntry(HttpServletRequest request, HttpServletResponse response) throws Exception;

    /**
     * 生成场景值二维码
     *
     * @param codeInfo
     * @param response
     * @throws IOException
     */
    void createQrCode(String codeInfo, HttpServletResponse response) throws IOException;
}
