package com.coupon.user.service;

import com.alibaba.fastjson.JSON;
import com.coupon.user.vo.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * <h1>用户服务测试</h1>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class UserServiceTest {

    @Autowired
    private IUserService userService;

    @Test
    public void testCreateUser() throws Exception{
        User user = new User();
        user.setBaseInfo(new User.BaseInfo("moon", 10, "a"));
        user.setOtherInfo(new User.OtherInfo("123", "moon"));

        // {"data":{"baseInfo":{"age":10,"name":"moon","sex":"a"},
        // "id":523930,"otherInfo":{"address":"moon","phone":"123"}},
        // "errorCode":0,"errorMsg":""}
        System.out.println(JSON.toJSONString(userService.createUser(user)));
    }
}
