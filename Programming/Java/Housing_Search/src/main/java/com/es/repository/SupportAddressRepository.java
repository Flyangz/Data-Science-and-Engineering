package com.es.repository;

import com.es.entity.SupportAddress;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by LiuYang on 2018/10/3 11:13 PM
 */
public interface SupportAddressRepository extends CrudRepository<SupportAddress, Long> {

    /** 获取所以对应行政级别的信息 */
    List<SupportAddress> findAllByLevel(String level);

    SupportAddress findByEnNameAndLevel(String enName, String level);

    SupportAddress findByEnNameAndBelongTo(String enName, String belongTo);

    List<SupportAddress> findAllByLevelAndBelongTo(String level, String belongTo);
}
