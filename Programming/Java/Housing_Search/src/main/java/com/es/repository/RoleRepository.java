package com.es.repository;

import com.es.entity.Role;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * <h1>角色数据DAO</h1>
 * Created by LiuYang on 2018/10/3 12:19 PM
 */
public interface RoleRepository extends CrudRepository<Role, Long> {
    List<Role> findRolesByUserId(Long userId); // 自动生成 SQL语句
}
