package com.es.service;

import java.util.List;

/**
 * <h1>通用多结果Service返回结构</h1>
 * Created by LiuYang on 2018/10/3 11:57 PM
 */
public class ServiceMultiResult<T> {

    private long total;
    private List<T> result;

    public ServiceMultiResult(long total, List<T> result) {
        this.total = total;
        this.result = result;
    }

    public int getResultSize(){
        if (this.result == null) {
            return 0;
        }
        return this.result.size();
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<T> getResult() {
        return result;
    }

    public void setResult(List<T> result) {
        this.result = result;
    }
}
