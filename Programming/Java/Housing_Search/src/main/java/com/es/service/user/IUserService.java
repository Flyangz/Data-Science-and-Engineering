package com.es.service.user;

import com.es.entity.User;
import com.es.service.ServiceResult;
import com.es.web.dto.UserDTO;

/**
 * Created by LiuYang on 2018/10/3 3:08 AM
 */
public interface IUserService {
    User findUserByName(String userName);

    ServiceResult<UserDTO> findById(Long userId);

    User findUserByTelephone(String telephone);

    User addUserByPhone(String telephone);

    ServiceResult modifyUserProfile(String profile, String value);
}
