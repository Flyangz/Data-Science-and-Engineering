package com.coupon.user.service.impl;

import com.alibaba.fastjson.JSON;
import com.coupon.user.constant.Constants;
import com.coupon.user.mapper.PassTemplateRowMapper;
import com.coupon.user.service.IGainPassTemplateService;
import com.coupon.user.utils.RowKeyGenUtil;
import com.coupon.user.vo.GainPassTemplateRequest;
import com.coupon.user.vo.PassTemplate;
import com.coupon.user.vo.Response;
import com.spring4all.spring.boot.starter.hbase.api.HbaseTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <h1>用户领取优惠券功能实现</h1>
 */
@Slf4j
@Service
public class GainPassTemplateServiceImpl implements IGainPassTemplateService {

    private HbaseTemplate hbaseTemplate;
    private StringRedisTemplate redisTemplate;

    public GainPassTemplateServiceImpl(HbaseTemplate hbaseTemplate, StringRedisTemplate redisTemplate) {
        this.hbaseTemplate = hbaseTemplate;
        this.redisTemplate = redisTemplate;
    }

    /**
     * <h2>领取优惠券</h2>
     * 1）从 request 获取 rowkey
     * 2）校验：是否有这种优惠券；优惠券数量是否充足；是否处于优惠券有效时间
     * 3）减少优惠券数量
     * 4）把优惠券添加到用户优惠券表中
     * @param request {@link GainPassTemplateRequest}
     * @return
     * @throws Exception
     */
    @Override
    public Response gainPassTemplate(GainPassTemplateRequest request) throws Exception {

        PassTemplate passTemplate;

        // 1）从 request 获取 rowkey
        String passTemplateId = RowKeyGenUtil.genPassTemplateRowKey(request.getPassTemplate());

        // 2）校验：是否有这种优惠券；优惠券数量是否充足；是否处于优惠券有效时间
        try {
            passTemplate = hbaseTemplate.get(
                    Constants.PassTemplateTable.TABLE_NAME,
                    passTemplateId,
                    new PassTemplateRowMapper()
            );
        } catch (Exception e) {
            log.error("Gain PassTemplate Error: {}", JSON.toJSONString(request.getPassTemplate()));
            return Response.failure("Gain PassTemplate Error!");
        }

        if (passTemplate.getLimit() <= 1 && passTemplate.getLimit() != 1) {
            log.error("PassTemplate Limit Max: {}", JSON.toJSONString(request.getPassTemplate()));
            return Response.failure("PassTemplate Limit Max");
        }

        Date cur = new Date();
        if (!(cur.getTime() >= passTemplate.getStart().getTime()
                && cur.getTime() < passTemplate.getEnd().getTime())) {
            log.error("PassTemplate ValidTime Error: {}", JSON.toJSONString(request.getPassTemplate()));
            return Response.failure("PassTemplate ValidTime Error!");
        }

        // 3）减少优惠券的 limit
        if (passTemplate.getLimit() != -1) {
            List<Mutation> data = new ArrayList<>();
            byte[] family_c = Constants.PassTemplateTable.FAMILY_C.getBytes();
            byte[] limit_k = Constants.PassTemplateTable.LIMIT.getBytes();

            Put put = new Put(Bytes.toBytes(passTemplateId));
            put.addColumn(family_c, limit_k,
                    Bytes.toBytes(passTemplate.getLimit() - 1));
            data.add(put);
            hbaseTemplate.saveOrUpdates(Constants.PassTemplateTable.TABLE_NAME, data);
        }

        // 4）将优惠券保存到用户优惠券表中
        if(!addPassForUser(request, passTemplate.getId(), passTemplateId)){
            return Response.failure("GainPassTemplate Failure!");
        }

        return Response.success();
    }

    /**
     * <h2>给用户添加优惠券</h2>
     * 1）检查该优惠券是否需要 token
     * 2）将存储所需的各属性的 key 和 value 转换为 Bytes
     * 3）将数据写入HBse
     * 4）将已使用的 token 记录到文件中
     * @param request        {@link GainPassTemplateRequest}
     * @param merchantsId    商户
     * @param passTemplateId 优惠券
     * @return
     */
    private boolean addPassForUser(GainPassTemplateRequest request,
                                   Integer merchantsId, String passTemplateId) throws IOException {

        String token = "-1";
        if (request.getPassTemplate().getHasToken()) {
            token = redisTemplate.opsForSet().pop(passTemplateId);
            if (null == token) {
                log.error("Token not exist: {}", passTemplateId);
                return false;
            }
        }

        byte[] family_i = Constants.PassTable.FAMILY_I.getBytes();
        byte[] user_id_k = Constants.PassTable.USER_ID.getBytes();
        byte[] template_id_k = Constants.PassTable.TEMPLATE_ID.getBytes();
        byte[] token_k = Constants.PassTable.TOKEN.getBytes();
        byte[] assigned_date_k = Constants.PassTable.ASSIGNED_DATE.getBytes();
        byte[] con_date_k = Constants.PassTable.CON_DATE.getBytes();

        byte[] rowKey = Bytes.toBytes(RowKeyGenUtil.genPassRowKey(request));
        byte[] user_id_v = Bytes.toBytes(request.getUserId());
        byte[] template_id_v = Bytes.toBytes(passTemplateId);
        byte[] token_v = Bytes.toBytes(token);
        byte[] assigned_data_v = Bytes.toBytes(DateFormatUtils.ISO_DATE_FORMAT.format(new Date()));
        byte[] con_date_v = Bytes.toBytes("-1");

        ArrayList<Mutation> data = new ArrayList<>();
        Put put = new Put(rowKey);

        put.addColumn(family_i, user_id_k, user_id_v);
        put.addColumn(family_i, template_id_k, template_id_v);
        put.addColumn(family_i, token_k, token_v);
        put.addColumn(family_i, assigned_date_k, assigned_data_v);
        put.addColumn(family_i, con_date_k, con_date_v);

        data.add(put);
        hbaseTemplate.saveOrUpdates(Constants.PassTable.TABLE_NAME, data);

        recordTokenToFile(merchantsId, passTemplateId, token);

        return true;
    }

    /**
     * <h2>将已使用的 token 记录到文件中</h2>
     *
     * @param merchantsId    商户 id
     * @param passTemplateId 优惠券 id
     * @param token          分配的优惠券 token
     */
    private void recordTokenToFile(Integer merchantsId, String passTemplateId, String token) throws IOException {

        Files.write(
                Paths.get(Constants.TOKEN_DIR, String.valueOf(merchantsId),
                        passTemplateId + Constants.USED_TOKEN_SUFFIX),
                (token + "\n").getBytes(),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND
        );
    }
}
