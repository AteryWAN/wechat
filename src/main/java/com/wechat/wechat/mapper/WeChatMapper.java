package com.wechat.wechat.mapper;

import com.wechat.wechat.module.AccessToken;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @title: wechat-service
 * @author: Young
 * @desc: 微信
 * @date: Created at 7/3 0003 14:52
 */
@Mapper
@Repository
public interface WeChatMapper {
    /**
     * 获取当前数据库最新token数据
     *
     * @return
     */
    AccessToken getToken();

    /**
     * 新增token记录
     *
     * @param accessToken
     */
    void newToken(AccessToken accessToken);
}
