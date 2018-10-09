package com.es.service.house;

import com.es.entity.SupportAddress;
import com.es.service.ServiceMultiResult;
import com.es.service.ServiceResult;
import com.es.web.dto.SubwayDTO;
import com.es.web.dto.SubwayStationDTO;
import com.es.web.dto.SupportAddressDTO;

import java.util.List;
import java.util.Map;

/**
 * <h1>地址服务接口</h1>
 * Created by LiuYang on 2018/10/3 11:17 PM
 */
public interface IAddressService {

    /** 获取所有支持的城市列表 */
    ServiceMultiResult<SupportAddressDTO> findAllCities();

    /** 根据英文简写获取具体区域的信息 */
    Map<SupportAddress.Level, SupportAddressDTO> findCityAndRegion(String cityEnName, String regionEnName);

    /** 根据城市英文简写获取该城市所有支持的区域信息 */
    ServiceMultiResult<SupportAddressDTO> findAllRegionsByCityName(String cityName);

    /** 获取该城市所有的地铁线路 */
    ServiceMultiResult<SubwayDTO> findAllSubwayByCity(String cityEnName);

    /** 获取地铁线路所有的站点 */
    ServiceMultiResult<SubwayStationDTO> findAllStationBySubway(Long subwayId);

    /** 获取地铁线信息 */
    ServiceResult<SubwayDTO> findSubway(Long subwayLineId);

    /** 找地铁站 */
    ServiceResult<SubwayStationDTO> findSubwayStation(Long subwayStationId);

    /** 根据城市英文名找城市 */
    ServiceResult<SupportAddressDTO> findCity(String cityEnName);
}
