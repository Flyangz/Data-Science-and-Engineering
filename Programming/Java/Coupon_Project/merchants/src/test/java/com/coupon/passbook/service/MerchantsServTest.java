package com.coupon.passbook.service;

import com.alibaba.fastjson.JSON;
import com.coupon.passbook.vo.CreateMerchantsRequest;
import com.coupon.passbook.vo.PassTemplate;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * <h1>商户服务测试类</h1>
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class MerchantsServTest {

    @Autowired
    private IMerchantsServ merchantsServ;

    /**
     * {"data":{"id":17},"errorCode":0,"errorMsg":""}
     * {"data":{"id":18},"errorCode":0,"errorMsg":""}
     */
    @Test
    @Transactional
    public void testCreateMerchantServ(){

        CreateMerchantsRequest request = new CreateMerchantsRequest();
        request.setName("兔");
        request.setLogoUrl("www.moon.com");
        request.setBusinessLicenseUrl("www.moon.com");
        request.setPhone("123");
        request.setAddress("月球");

        System.out.println(JSON.toJSONString(merchantsServ.createMerchants(request)));
    }

    /**
     * {"data":{"address":"月球","businessLicenseUrl":"www.moon.com","id":19,"isAudit":false,"logoUrl":"www.moon.com",
     * "name":"兔","phone":"123"},"errorCode":0,"errorMsg":""}
     */
    @Test
    public void testBuildMerchantsInfoById(){

        System.out.println(JSON.toJSONString(merchantsServ.buildMerchantsInfo(10)));
    }

    /**
     * {"background":2,"desc":"description: moon","end":1537953659989,
     * "hasToken":false,"id":19,"limit":100,"start":1537089659989,
     * "summary":"summary: moon","title":"tittle: moon"}
     */
    @Test
    public void testDropPassTemplate(){

        PassTemplate passTemplate = new PassTemplate();
        passTemplate.setId(19);
        passTemplate.setTitle("tittle: moon -10");
        passTemplate.setSummary("summary: moon");
        passTemplate.setDesc("description: moon");
        passTemplate.setLimit(10000L);
        passTemplate.setHasToken(false);
        passTemplate.setBackground(2);
        passTemplate.setStart(DateUtils.addDays(new Date(), -10));
        passTemplate.setEnd(DateUtils.addDays(new Date(), 10));

        System.out.println(JSON.toJSONString(
                merchantsServ.dropPassTemplate(passTemplate)
        ));
    }
}
