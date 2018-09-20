package com.coupon.passbook.service.impl;

import com.alibaba.fastjson.JSON;
import com.coupon.passbook.constant.Constants;
import com.coupon.passbook.constant.Errorcode;
import com.coupon.passbook.dao.MerchantsDao;
import com.coupon.passbook.entity.Merchants;
import com.coupon.passbook.service.IMerchantsServ;
import com.coupon.passbook.vo.CreateMerchantsRequest;
import com.coupon.passbook.vo.CreateMerchantsResponse;
import com.coupon.passbook.vo.PassTemplate;
import com.coupon.passbook.vo.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <h1>商户服务接口实现</h1>
 */
@Slf4j
@Service
public class MerchantsServImpl implements IMerchantsServ {

    /**
     * Merchants 数据库接口
     */
    private final MerchantsDao merchantsDao;

    /**
     * Kafka 客户端
     */
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public MerchantsServImpl(MerchantsDao merchantsDao,
                             KafkaTemplate<String, String> kafkaTemplate) {
        this.merchantsDao = merchantsDao;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    @Transactional
    public Response createMerchants(CreateMerchantsRequest request) {

        Response response = new Response();
        CreateMerchantsResponse merchantsResponse = new CreateMerchantsResponse();

        Errorcode errorcode = request.validate(merchantsDao);
        if (errorcode != Errorcode.SUCCESS) {
            merchantsResponse.setId(-1);
            response.setErrorCode(errorcode.getCode());
            response.setErrorMsg(errorcode.getDesc());
        } else {
            merchantsResponse.setId(merchantsDao.save(request.toMerchants()).getId());
        }

        response.setData(merchantsResponse);

        return response;
    }

    @Override
    public Response buildMerchantsInfo(Integer id) {

        Response response = new Response();

        Merchants merchants = merchantsDao.findById(id);
        if (merchants == null) {
            response.setErrorCode(Errorcode.MERCHANTS_NOT_EXIST.getCode());
            response.setErrorMsg(Errorcode.MERCHANTS_NOT_EXIST.getDesc());
        }

        response.setData(merchants);

        return response;
    }

    @Override
    public Response dropPassTemplate(PassTemplate template) {

        Response response = new Response();
        Errorcode errorcode = template.validate(merchantsDao);

        if (errorcode != Errorcode.SUCCESS) {
            response.setErrorCode(errorcode.getCode());
            response.setErrorMsg(errorcode.getDesc());
        } else {
            String passTemplate = JSON.toJSONString(template);
            kafkaTemplate.send(
                    Constants.TEMPLATE_TOPIC,
                    Constants.TEMPLATE_TOPIC,
                    passTemplate
            );
            log.info("DropPassTemplates:{}", passTemplate);
        }

        return response;
    }
}
