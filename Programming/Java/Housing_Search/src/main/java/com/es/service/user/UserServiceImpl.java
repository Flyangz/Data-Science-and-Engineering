package com.es.service.user;

import com.es.base.LoginUserUtil;
import com.es.entity.Role;
import com.es.entity.User;
import com.es.repository.RoleRepository;
import com.es.repository.UserRepository;
import com.es.security.CustomPasswordEncoderFactory;
import com.es.service.ServiceResult;
import com.es.web.dto.UserDTO;
import com.google.common.collect.Lists;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by LiuYang on 2018/10/3 3:09 AM
 */
@Service
public class UserServiceImpl implements IUserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ModelMapper modelMapper;

    private final PasswordEncoder passwordEncoder = CustomPasswordEncoderFactory
            .createDelegatingPasswordEncoder();

    @Override
    public User findUserByName(String userName) {
        final User user = userRepository.findByName(userName);

        if (user == null) {
            return null;
        }

        final List<Role> roles = roleRepository.findRolesByUserId(user.getId());
        if (roles == null || roles.isEmpty()) {
            throw new DisabledException("权限非法");
        }

        final ArrayList<GrantedAuthority> authorities = new ArrayList<>();
        roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName())));
        user.setAuthorityList(authorities);
        return user;
    }

    @Override
    public ServiceResult<UserDTO> findById(Long userId) {
        final User user = userRepository.findOne(userId);
        if (user == null) {
            return ServiceResult.notFound();
        }

        final UserDTO userDTO = modelMapper.map(user, UserDTO.class);
        return ServiceResult.of(userDTO);
    }

    @Override
    public User findUserByTelephone(String telephone) {
        final User user = userRepository.findUserByPhoneNumber(telephone);
        if (user == null) {
            return null;
        }

        final List<Role> roles = roleRepository.findRolesByUserId(user.getId());
        if (roles == null || roles.isEmpty()) {
            throw new DisabledException("权限非法");
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        roles.forEach(role -> authorities.add(
                new SimpleGrantedAuthority("ROLE_" + role.getName()))
        );
        user.setAuthorityList(authorities);
        return user;
    }

    @Override
    public User addUserByPhone(String telephone) {
        User user = new User();
        user.setPhoneNumber(telephone);
        user.setName(telephone.substring(0, 3) + "****"
        + telephone.substring(7, telephone.length()));
        final Date now = new Date();
        user.setCreateTime(now);
        user.setLastLoginTIme(now);
        user.setLastUpdateTime(now);
        user = userRepository.save(user);

        final Role role = new Role();
        role.setName("USER");
        role.setUserId(user.getId());
        roleRepository.save(role);

        user.setAuthorityList(Lists.newArrayList(new SimpleGrantedAuthority("ROLE_USER")));

        return user;
    }

    @Override
    @Transactional
    public ServiceResult modifyUserProfile(String profile, String value) {
        Long userId = LoginUserUtil.getLoginUserId();
        if (profile == null || profile.isEmpty()) {
            return new ServiceResult(false, "属性不可以为空");
        }
        switch (profile) {
            case "name":
                userRepository.updateUsername(userId, value);
                break;
            case "email":
                userRepository.updateEmail(userId, value);
                break;
            case "password":
                userRepository.updatePassword(userId, this.passwordEncoder.encode(value));
                break;
            default:
                return new ServiceResult(false, "不支持的属性");
        }
        return ServiceResult.success();
    }
}
