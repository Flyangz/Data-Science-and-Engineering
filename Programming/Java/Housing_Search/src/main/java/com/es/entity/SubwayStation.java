package com.es.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * Created by LiuYang on 2018/10/4 2:14 AM
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "subway_station")
public class SubwayStation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subway_id")
    private Long subwayId;

    private String name;

}
