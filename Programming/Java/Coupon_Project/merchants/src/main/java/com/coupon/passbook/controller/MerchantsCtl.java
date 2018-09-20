package com.coupon.passbook.controller;

import com.alibaba.fastjson.JSON;
import com.coupon.passbook.service.IMerchantsServ;
import com.coupon.passbook.vo.CreateMerchantsRequest;
import com.coupon.passbook.vo.PassTemplate;
import com.coupon.passbook.vo.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <h1>商户服务</h1>
 */
@Slf4j
@RestController
@RequestMapping("/merchants")
public class MerchantsCtl {

    /** 商户服务接口 */
    private IMerchantsServ merchantsServ;

    @Autowired
    public MerchantsCtl(IMerchantsServ merchantsServ) {
        this.merchantsServ = merchantsServ;
    }

    @ResponseBody
    @PostMapping("/create")
    public Response createMerchants(@RequestBody CreateMerchantsRequest request) {

        log.info("CreateMerchants:{}", JSON.toJSONString(request));
        return merchantsServ.createMerchants(request);
    }

    @ResponseBody
    @GetMapping("/{id}")
    public Response buildMerchantsInfo(@PathVariable Integer id) {

        log.info("BuildMerchantsInfo:{}", id);
        return merchantsServ.buildMerchantsInfo(id);
    }

    @ResponseBody
    @PostMapping("/drop")
    public Response dropPassTemplate(@RequestBody PassTemplate passTemplate) {

        log.info("CreateMerchants:{}", passTemplate);
        return merchantsServ.dropPassTemplate(passTemplate);
    }
}
