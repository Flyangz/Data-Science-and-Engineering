package com.es.web.dto;

/**
 * Created by LiuYang on 2018/10/4 7:09 PM
 */
public final class QiNiuPutRet {
    public String key;
    public String hash;
    public String bucket;
    public int width;
    public int height;

    public QiNiuPutRet(String key, String hash, String bucket, int width, int height) {
        this.key = key;
        this.hash = hash;
        this.bucket = bucket;
        this.width = width;
        this.height = height;
    }

    @Override
    public String toString() {
        return "QiNiuPutRet{" +
                "key='" + key + '\'' +
                ", hash='" + hash + '\'' +
                ", bucket='" + bucket + '\'' +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}