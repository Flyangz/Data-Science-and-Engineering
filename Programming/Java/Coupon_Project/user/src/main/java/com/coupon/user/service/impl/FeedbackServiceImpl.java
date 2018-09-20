package com.coupon.user.service.impl;

import com.alibaba.fastjson.JSON;
import com.coupon.user.constant.Constants;
import com.coupon.user.mapper.FeedbackRowMapper;
import com.coupon.user.service.IFeedbackService;
import com.coupon.user.utils.RowKeyGenUtil;
import com.coupon.user.vo.Feedback;
import com.coupon.user.vo.Response;
import com.spring4all.spring.boot.starter.hbase.api.HbaseTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.PrefixFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <h1>评论功能实现类</h1>
 */
@Slf4j
@Service
public class FeedbackServiceImpl implements IFeedbackService {

    /**
     * HBase 客户端
     */
    private final HbaseTemplate hbaseTemplate;

    @Autowired
    public FeedbackServiceImpl(HbaseTemplate hbaseTemplate) {
        this.hbaseTemplate = hbaseTemplate;
    }

    @Override
    public Response createFeedback(Feedback feedback) {

        if (!feedback.validate()) {
            log.error("Feedback Error: {}", JSON.toJSONString(feedback));
            return Response.failure("Feedback creation Error");
        }

        /** FAMILY_I 所有 key 的 byte 形式 */
        byte[] family_i = Constants.Feedback.FAMILY_I.getBytes();
        byte[] userId_k = Constants.Feedback.USER_ID.getBytes();
        byte[] type_k = Constants.Feedback.TYPE.getBytes();
        byte[] templateId_k = Constants.Feedback.TEMPLATE_ID.getBytes();
        byte[] comment_k = Constants.Feedback.COMMENT.getBytes();

        /** 反馈 feedback 所有属性的值的 byte 形式 */
        byte[] userId_v = Bytes.toBytes(feedback.getUserId());
        byte[] type_v = Bytes.toBytes(feedback.getType());
        byte[] templateId_v = Bytes.toBytes(feedback.getTemplateId());
        byte[] comment_v = Bytes.toBytes(feedback.getComment());

        Put put = new Put(Bytes.toBytes(RowKeyGenUtil.genFeedbackRowKey(feedback)));

        put.addColumn(family_i, userId_k, userId_v);
        put.addColumn(family_i, type_k, type_v);
        put.addColumn(family_i, templateId_k, templateId_v);
        put.addColumn(family_i, comment_k, comment_v);

        hbaseTemplate.saveOrUpdate(Constants.Feedback.TABLE_NAME, put);

        return Response.success();
    }

    @Override
    public Response getFeedback(Long userId) {

        // 对 userId 进行 reverse 是因为存在 HBase 的 userId 都是经过 reverse 的，原因看下面类的 genFeedbackRowKey 方法。
        /** {@link RowKeyGenUtil} */
        byte[] reverseUserId = new StringBuilder(String.valueOf(userId)).reverse().toString().getBytes();

        // HBase 提供的 Scan 和 PrefixFilter 。
        Scan scan = new Scan();
        scan.setFilter(new PrefixFilter(reverseUserId));

        // find 返回 List ，如果没找到则为空 List。如果用 get 就只能得到一条数据。
        List<Feedback> feedbackList = hbaseTemplate.find(Constants.Feedback.TABLE_NAME, scan, new FeedbackRowMapper());

        return new Response(feedbackList);
    }
}
