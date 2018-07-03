package com.wechat.wechat.module.message;

/**
 * @title: wechat-service
 * @author: Young
 * @desc: 微信 - 图片消息(image)/ 语音消息(voice)
 * @date: Created at 7/3 0003 15:44
 */
public class ImgVoiceMessage extends BaseMessage {

    /**
     * 通过素材管理中的接口上传多媒体文件，得到的id
     */
    private String MediaId;

    public String getMediaId() {
        return MediaId;
    }

    public void setMediaId(String mediaId) {
        MediaId = mediaId;
    }
}
