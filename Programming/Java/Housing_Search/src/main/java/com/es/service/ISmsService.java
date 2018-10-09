package com.es.service;

/**
 * Created by LiuYang on 2018/10/8 12:51 AM
 */
public interface ISmsService {

    /**
     * 发送验证码到 手机 并 缓存验证码 10分钟 及 请求间隔时间1分钟
     */
    ServiceResult<String> sendSms(String telephone);

    /**
     * 获取缓存中的验证码
     */
    String getSmsCode(String telephone);

    /**
     * 移除指定手机号的验证码缓存
     */
    void remove(String telephone);
}
