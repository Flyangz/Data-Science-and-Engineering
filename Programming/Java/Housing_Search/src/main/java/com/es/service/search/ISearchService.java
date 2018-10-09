package com.es.service.search;

import com.es.service.ServiceMultiResult;
import com.es.service.ServiceResult;
import com.es.web.form.RentSearch;
import jdk.nashorn.internal.ir.VarNode;

import java.util.List;

/**
 * Created by LiuYang on 2018/10/5 10:59 PM
 */
public interface ISearchService {

    /**
     * 索引目标房源
     */
    void index(Long houseId);

    /**
     * 移除房源
     */
    void remove(Long houseId);

    /** 查询房源接口 */
    ServiceMultiResult<Long> query(RentSearch rentSearch);

    /** 获取补全建议关键词 */
    ServiceResult<List<String>> suggest(String prefix);

    /**
     * 聚合特定小区的房间数
     */
    ServiceResult<Long> aggregateDistrictHouse(String cityEnName, String regionEnName,
                                               String district);
}
