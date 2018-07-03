package com.wechat.wechat.module;

import java.sql.Timestamp;
import java.util.Date;

/**
 * @title: wechat-service
 * @author: Young
 * @desc: 微信 - token令牌
 * @date: Created at 7/3 0003 15:39
 */
public class AccessToken {

    private String id;
    /**
     * 获取到的凭证
     */
    private String accessToken;

    /**
     * 凭证有效时间，单位：s
     */
    private int expiresIn;

    /**
     * 创建时间
     */
    private Date createTime;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
