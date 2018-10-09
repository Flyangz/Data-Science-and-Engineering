package com.es.service.search;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * <h1>索引结构模版</h1>
 * Created by LiuYang on 2018/10/5 10:37 PM
 */
@Getter
@Setter
public class HouseIndexTemplate {

    private Long houseId;

    private String title;

    private int price;

    private int area;

    private Date createTime;

    private Date lastUpdateTime;

    private String cityEnName;

    private String regionEnName;

    private int direction;

    private int distanceToSubway;

    private String subwayLineName;

    private String subwayStationName;

    private String street;

    private String district;

    private String description;

    private String layoutDesc;

    private String traffic;

    private String roundService;

    private int rentWay;

    private List<String> tags;

    private List<HouseSuggest> suggest;

}
