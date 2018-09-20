package com.coupon.user.service.impl;

import com.coupon.user.constant.Constants;
import com.coupon.user.service.IUserService;
import com.coupon.user.vo.Response;
import com.coupon.user.vo.User;
import com.spring4all.spring.boot.starter.hbase.api.HbaseTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * <h1>创建用户服务实现</h1>
 */
@Slf4j
@Service
public class UserServiceImpl implements IUserService {

    /**
     * redis 客户端
     */
    private final StringRedisTemplate redisTemplate;
    /**
     * HBase 客户端
     */
    private HbaseTemplate hbaseTemplate;

    @Autowired
    public UserServiceImpl(HbaseTemplate hbaseTemplate, StringRedisTemplate redisTemplate) {
        this.hbaseTemplate = hbaseTemplate;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Response createUser(User user) throws Exception {

        /** FAMILY_B 所有 key 的 byte 形式 */
        byte[] family_b = Constants.UserTable.FAMILY_B.getBytes();
        byte[] name_k = Constants.UserTable.NAME.getBytes();
        byte[] age_k = Constants.UserTable.AGE.getBytes();
        byte[] sex_k = Constants.UserTable.SEX.getBytes();

        /** FAMILY_O 所有 key 的 byte 形式 */
        byte[] family_o = Constants.UserTable.FAMILY_O.getBytes();
        byte[] phone_k = Constants.UserTable.PHONE.getBytes();
        byte[] address_k = Constants.UserTable.ADDRESS.getBytes();

        /** 用户 user 所有属性的值的 byte 形式 */
        byte[] name_v = Bytes.toBytes(user.getBaseInfo().getName());
        byte[] age_v = Bytes.toBytes(user.getBaseInfo().getAge());
        byte[] sex_v = Bytes.toBytes(user.getBaseInfo().getSex());
        byte[] phone_v = Bytes.toBytes(user.getOtherInfo().getPhone());
        byte[] address_v = Bytes.toBytes(user.getOtherInfo().getAddress());

        Long curCount = redisTemplate.opsForValue().increment(Constants.USE_COUNT_REDIS_KEY, 1);
        Long userId = genUserId(curCount);

        ArrayList<Mutation> data = new ArrayList<Mutation>();
        Put put = new Put(Bytes.toBytes(userId));

        put.addColumn(family_b, name_k, name_v);
        put.addColumn(family_b, age_k, age_v);
        put.addColumn(family_b, sex_k, sex_v);
        put.addColumn(family_o, phone_k, phone_v);
        put.addColumn(family_o, address_k, address_v);

        data.add(put);

        hbaseTemplate.saveOrUpdates(Constants.UserTable.TABLE_NAME, data);

        user.setId(userId);

        return new Response(user);
    }

    /**
     * <h2>生成 userId</h2>
     *
     * @param prefix 当前用户数
     * @return 用户 id
     */
    private Long genUserId(Long prefix) {

        String suffix = RandomStringUtils.randomNumeric(5);
        return Long.valueOf(prefix + suffix);
    }
}
