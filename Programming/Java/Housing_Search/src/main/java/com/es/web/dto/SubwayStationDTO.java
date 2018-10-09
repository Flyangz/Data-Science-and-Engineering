package com.es.web.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by LiuYang on 2018/10/4 12:28 AM
 */
@Getter
@Setter
@ToString
public class SubwayStationDTO {
    private Long id;
    private Long subwayId;
    private String name;
}
