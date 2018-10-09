package com.es.service.house;

import com.es.entity.Subway;
import com.es.entity.SubwayStation;
import com.es.entity.SupportAddress;
import com.es.repository.SubwayRepository;
import com.es.repository.SubwayStationRepository;
import com.es.repository.SupportAddressRepository;
import com.es.service.ServiceMultiResult;
import com.es.service.ServiceResult;
import com.es.web.dto.SubwayDTO;
import com.es.web.dto.SubwayStationDTO;
import com.es.web.dto.SupportAddressDTO;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by LiuYang on 2018/10/3 11:22 PM
 */
@Service
public class AddressServiceImpl implements IAddressService {

    @Autowired
    private SupportAddressRepository supportAddressRepository;

    @Autowired
    private SubwayRepository subwayRepository;

    @Autowired
    private SubwayStationRepository subwayStationRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public ServiceMultiResult<SupportAddressDTO> findAllCities() {
        List<SupportAddress> addresses = supportAddressRepository
                .findAllByLevel(SupportAddress.Level.CITY.getValue());
        if (addresses == null) {
            return new ServiceMultiResult<>(0, null);
        }

        List<SupportAddressDTO> addressDTOS = new ArrayList<>();
        // 将每个 SupportAddress 映射为 SupportAddressDTO
        addresses.forEach(supportAddress -> {
            SupportAddressDTO target = modelMapper.map(supportAddress, SupportAddressDTO.class);
            addressDTOS.add(target);
        });

        return new ServiceMultiResult<>(addressDTOS.size(), addressDTOS);
    }

    @Override
    public ServiceMultiResult<SupportAddressDTO> findAllRegionsByCityName(String cityName) {
        if (cityName == null) {
            return new ServiceMultiResult<>(0, null);
        }

        List<SupportAddress> regions = supportAddressRepository.findAllByLevelAndBelongTo(
                SupportAddress.Level.REGION.getValue(), cityName);
        if (regions == null) {
            return new ServiceMultiResult<>(0, null);
        }

        List<SupportAddressDTO> result = new ArrayList<>();
        regions.forEach(region ->
                result.add(modelMapper.map(region, SupportAddressDTO.class))
        );

        return new ServiceMultiResult<>(regions.size(), result);
    }

    @Override
    public ServiceMultiResult<SubwayDTO> findAllSubwayByCity(String cityEnName) {
        List<SubwayDTO> result = new ArrayList<>();
        List<Subway> subways = subwayRepository.findAllByCityEnName(cityEnName);
        if (subways == null) {
            return new ServiceMultiResult<>(0, null);
        }

        subways.forEach(subway -> result.add(modelMapper.map(subway, SubwayDTO.class)));

        return new ServiceMultiResult<>(subways.size(), result);
    }

    @Override
    public ServiceMultiResult<SubwayStationDTO> findAllStationBySubway(Long subwayId) {

        List<SubwayStation> stations = subwayStationRepository.findAllBySubwayId(subwayId);
        if (stations == null) {
            return new ServiceMultiResult<>(0, null);
        }

        List<SubwayStationDTO> result = new ArrayList<>();
        stations.forEach(station -> result.add(modelMapper.map(station, SubwayStationDTO.class)));
        return new ServiceMultiResult<>(stations.size(), result);
    }

    @Override
    public ServiceResult<SubwayDTO> findSubway(Long subwayId) {
        if (subwayId == null) {
            return ServiceResult.notFound();
        }
        Subway subway = subwayRepository.findOne(subwayId);
        if (subway == null) {
            return ServiceResult.notFound();
        }
        return ServiceResult.of(modelMapper.map(subway, SubwayDTO.class));
    }

    @Override
    public ServiceResult<SubwayStationDTO> findSubwayStation(Long stationId) {
        if (stationId == null) {
            return ServiceResult.notFound();
        }
        SubwayStation station = subwayStationRepository.findOne(stationId);
        if (station == null) {
            return ServiceResult.notFound();
        }
        return ServiceResult.of(modelMapper.map(station, SubwayStationDTO.class));
    }

    @Override
    public ServiceResult<SupportAddressDTO> findCity(String cityEnName) {
        if (cityEnName == null) {
            return ServiceResult.notFound();
        }

        final SupportAddress supportAddress = supportAddressRepository
                .findByEnNameAndLevel(cityEnName, SupportAddress.Level.CITY.getValue());
        if (supportAddress == null) {
            return ServiceResult.notFound();
        }
        final SupportAddressDTO addressDTO = modelMapper.map(supportAddress, SupportAddressDTO.class);
        return ServiceResult.of(addressDTO);
    }

    @Override
    public Map<SupportAddress.Level, SupportAddressDTO> findCityAndRegion(String cityEnName, String regionEnName) {
        Map<SupportAddress.Level, SupportAddressDTO> result = new HashMap<>();

        SupportAddress city = supportAddressRepository.findByEnNameAndLevel(cityEnName, SupportAddress.Level.CITY
                .getValue());
        SupportAddress region = supportAddressRepository.findByEnNameAndBelongTo(regionEnName, city.getEnName());

        result.put(SupportAddress.Level.CITY, modelMapper.map(city, SupportAddressDTO.class));
        result.put(SupportAddress.Level.REGION, modelMapper.map(region, SupportAddressDTO.class));
        return result;
    }
}
