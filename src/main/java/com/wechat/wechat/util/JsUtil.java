package com.wechat.wechat.util;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * @title: wechat-service
 * @author: Young
 * @desc: 微信 - 签名验证工具
 * @date: Created at 7/4 0004 14:29
 */
@Component
public class JsUtil {

    private static String token;

    @Value("${custom.token}")
    public void setToken(String token) {
        JsUtil.token = token;
    }




    /**
     * 签名
     *
     * @param timestamp
     * @param nonce
     * @param signature
     * @return signature, timestamp, nonce, echostr
     * 1）将token、timestamp、nonce三个参数进行字典序排序
     * 2）将三个参数字符串拼接成一个字符串进行sha1加密
     * 3）开发者获得加密后的字符串可与signature对比，标识该请求来源于微信
     */
    public static Boolean checkSignature(String timestamp, String nonce, String signature) {
        String resStr = null;
        //  将token、timestamp、nonce三个参数进行字典序排序
        String[] arr = new String[]{timestamp, token, nonce};
        //  排序
        Arrays.sort(arr);
        //  将三个参数字符串拼接成一个字符串进行sha1加密
        String checkStr = arr[0] + arr[1] + arr[2];
        try {
            //  sha-1加密
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] byteDigest = md.digest(checkStr.getBytes());
            //  加密后数据转为字符串进行比对
            resStr = byteToStr(byteDigest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return Strings.isNotEmpty(resStr) && resStr.toLowerCase().equals(signature.toLowerCase());
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param byteArray
     * @return
     */
    private static String byteToStr(byte[] byteArray) {
        String strDigest = "";
        for (int i = 0; i < byteArray.length; i++) {
            strDigest += byteToHexStr(byteArray[i]);
        }
        return strDigest;
    }

    /**
     * 将字节转换为十六进制字符串
     *
     * @param mByte
     * @return
     */
    private static String byteToHexStr(byte mByte) {
        char[] Digit = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        char[] tempArr = new char[2];
        tempArr[0] = Digit[(mByte >>> 4) & 0X0F];
        tempArr[1] = Digit[mByte & 0X0F];
        String s = new String(tempArr);
        return s;
    }

}
