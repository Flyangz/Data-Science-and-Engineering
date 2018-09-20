package com.coupon.user.service;

import com.alibaba.fastjson.JSON;
import com.coupon.user.vo.Pass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * <h1>用户优惠券服务测试</h1>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class UserPassServiceTest extends AbstractServiceTest {

    @Autowired
    private IUserPassService userPassService;

    @Test
    public void testGetUserPassInfo() throws Exception {

        System.out.println(JSON.toJSONString(
                userPassService.getUserPassInfo(userId))
        );
    }

    // {"data":[],"errorCode":0,"errorMsg":""}
    @Test
    public void testGetUserUsedPassInfo() throws Exception {

        System.out.println(JSON.toJSONString(
                userPassService.getUserUsedPassInfo(userId)
        ));
    }

    @Test
    public void testGetUserAllPassInfo() throws Exception {

        System.out.println(JSON.toJSONString(
                userPassService.getUserAllPassInfo(userId)
        ));
    }

    // {"errorCode":0,"errorMsg":""}
    @Test
    public void testUserUsePass() {

        Pass pass = new Pass();
        pass.setUserId(userId);
        pass.setTemplateId("134f2d2a1e78e985805c7bc5ccde2570");

        System.out.println(JSON.toJSONString(
                userPassService.userUsePass(pass)
        ));
    }
}
