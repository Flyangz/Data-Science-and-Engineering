package com.es.web.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by LiuYang on 2018/10/4 3:25 PM
 */
@Getter
@Setter
@ToString
public class HouseDetailDTO {
    private String description;

    private String layoutDesc;

    private String traffic;

    private String roundService;

    private int rentWay;

    private Long adminId;

    private String address;

    private Long subwayLineId;

    private Long subwayStationId;

    private String subwayLineName;

    private String subwayStationName;

}
