package com.es.repository;

import com.es.entity.HousePicture;
import com.es.entity.HouseTag;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by LiuYang on 2018/10/4 2:09 PM
 */
public interface HouseTagRepository extends CrudRepository<HouseTag, Long> {
    List<HouseTag> findAllByHouseId(Long id);

    HouseTag findByNameAndHouseId(String tag, Long houseId);

    List<HouseTag> findAllByHouseIdIn(List<Long> houseIds);
}
