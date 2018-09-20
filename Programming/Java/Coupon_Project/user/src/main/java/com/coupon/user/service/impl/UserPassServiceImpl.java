package com.coupon.user.service.impl;

import com.alibaba.fastjson.JSON;
import com.coupon.user.constant.Constants;
import com.coupon.user.constant.PassStatus;
import com.coupon.user.dao.MerchantsDao;
import com.coupon.user.entity.Merchants;
import com.coupon.user.mapper.PassRowMapper;
import com.coupon.user.service.IUserPassService;
import com.coupon.user.vo.Pass;
import com.coupon.user.vo.PassInfo;
import com.coupon.user.vo.PassTemplate;
import com.coupon.user.vo.Response;
import com.spring4all.spring.boot.starter.hbase.api.HbaseTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <h1>用户优惠券相关功能实现</h1>
 */
@Slf4j
@Service
public class UserPassServiceImpl implements IUserPassService {

    private final HbaseTemplate hbaseTemplate;

    private final MerchantsDao merchantsDao;

    @Autowired
    public UserPassServiceImpl(HbaseTemplate hbaseTemplate, MerchantsDao merchantsDao) {
        this.hbaseTemplate = hbaseTemplate;
        this.merchantsDao = merchantsDao;
    }

    @Override
    public Response getUserPassInfo(Long userId) throws Exception {
        return getPassInfoByStatus(userId, PassStatus.UNUSED);
    }

    @Override
    public Response getUserUsedPassInfo(Long userId) throws Exception {
        return getPassInfoByStatus(userId, PassStatus.USED);
    }

    @Override
    public Response getUserAllPassInfo(Long userId) throws Exception {
        return getPassInfoByStatus(userId, PassStatus.ALL);
    }

    @Override
    public Response userUsePass(Pass pass) {

        //先对用户传递进来的优惠券进行校验：1)存在，2)未被使用，3)只有一张
        Scan scan = new Scan();
        ArrayList<Filter> filters = new ArrayList<>();

        filters.add(new PrefixFilter(
                Bytes.toBytes(new StringBuffer(
                String.valueOf(pass.getUserId())).reverse().toString())));

        filters.add(new SingleColumnValueFilter(
                Constants.PassTable.FAMILY_I.getBytes(),
                Constants.PassTable.TEMPLATE_ID.getBytes(),
                CompareFilter.CompareOp.EQUAL,
                Bytes.toBytes(pass.getTemplateId())
        ));

        filters.add(new SingleColumnValueFilter(
                Constants.PassTable.FAMILY_I.getBytes(),
                Constants.PassTable.CON_DATE.getBytes(),
                CompareFilter.CompareOp.EQUAL,
                Bytes.toBytes("-1")
        ));

        scan.setFilter(new FilterList(filters));

        List<Pass> passes = hbaseTemplate.find(Constants.PassTable.TABLE_NAME, scan, new PassRowMapper());

        if (passes == null || passes.size() != 1) {
            log.error("UserUserPass Error: {}", JSON.toJSONString(pass));
            return Response.failure("UserUsePass Error");
        }

        byte[] family_i = Constants.PassTable.FAMILY_I.getBytes();
        byte[] con_date_k = Constants.PassTable.CON_DATE.getBytes();
        byte[] con_date_v = Bytes.toBytes(DateFormatUtils.ISO_DATE_FORMAT.format(new Date()));

        List<Mutation> data = new ArrayList<>();
        Put put = new Put(passes.get(0).getRowKey().getBytes());
        put.addColumn(family_i, con_date_k, con_date_v);
        data.add(put);

        hbaseTemplate.saveOrUpdates(Constants.PassTable.TABLE_NAME, data);

        return Response.success();
    }

    /**
     * <h2>根据优惠券状态获取优惠券信息</h2>
     *
     * @param userId
     * @param status {@link PassStatus}
     * @return {@link Response}
     * @throws Exception
     */
    private Response getPassInfoByStatus(Long userId, PassStatus status) throws Exception {

        Scan scan = new Scan();

        // 过滤条件1
        byte[] rowPrefix = Bytes.toBytes(new StringBuffer(String.valueOf(userId)).reverse().toString());

        // 过滤条件2。这里 status 等于 1 过滤未被使用的，等于 2 反之。
        CompareFilter.CompareOp compareOp =
                status == PassStatus.UNUSED ?
                        CompareFilter.CompareOp.EQUAL : CompareFilter.CompareOp.NOT_EQUAL;

        List<Filter> filters = new ArrayList<>();

        // 前缀过滤器，提取 userId 的所有优惠券
        filters.add(new PrefixFilter(rowPrefix));

        // 如果不是查看用户的所有优惠券，那只返回"未被使用"或者"已被使用"的优惠券。
        // 注意这里是跟消费日期比较，如果日期等于 -1 表示优惠券未被使用。
        if (status != PassStatus.ALL) {
            filters.add(new SingleColumnValueFilter(
                    Constants.PassTable.FAMILY_I.getBytes(),
                    Constants.PassTable.CON_DATE.getBytes(),
                    compareOp,
                    Bytes.toBytes("-1")
            ));
        }

        scan.setFilter(new FilterList(filters));

        List<Pass> passes = hbaseTemplate.find(Constants.PassTable.TABLE_NAME, scan, new PassRowMapper());

        Map<String, PassTemplate> passTemplateMap = buildPassTemplateMap(passes);
        Map<Integer, Merchants> merchantsMap = buildMerchantsMap(new ArrayList<>(passTemplateMap.values()));

        ArrayList<PassInfo> result = new ArrayList<>();

        for (Pass pass : passes) {
            PassTemplate passTemplate = passTemplateMap.getOrDefault(pass.getTemplateId(), null);

            if (passTemplate == null) {
                log.error("PassTemplate Null : {}", pass.getTemplateId());
                continue;
            }

            Merchants merchants = merchantsMap.getOrDefault(passTemplate.getId(), null);

            if (merchants == null) {
                log.error("Merchants Null : {}", passTemplate.getId());
                continue;
            }

            result.add(new PassInfo(pass, passTemplate, merchants));
        }

        return new Response(result);
    }

    /**
     * <h2>通过获取的 Passes 对象构造</h2>
     *
     * @param passes {@link Pass}
     * @return Map {@link PassTemplate}
     * @throws Exception
     */
    private Map<String, PassTemplate> buildPassTemplateMap(List<Pass> passes) throws Exception {

        String[] patterns = {"yyyy-MM-dd"};

        /** FAMILY_B 所有 key 的 byte 形式 */
        byte[] family_b = Constants.PassTemplateTable.FAMILY_B.getBytes();
        byte[] id_k = Constants.PassTemplateTable.ID.getBytes();
        byte[] title_k = Constants.PassTemplateTable.TITLE.getBytes();
        byte[] summary_k = Constants.PassTemplateTable.SUMMARY.getBytes();
        byte[] desc_k = Constants.PassTemplateTable.DESC.getBytes();
        byte[] has_token_k = Constants.PassTemplateTable.HAS_TOKEN.getBytes();
        byte[] background_k = Constants.PassTemplateTable.BACKGROUND.getBytes();

        /** FAMILY_C 所有 key 的 byte 形式 */
        byte[] family_c = Constants.PassTemplateTable.FAMILY_C.getBytes();
        byte[] limit_k = Constants.PassTemplateTable.LIMIT.getBytes();
        byte[] start_k = Constants.PassTemplateTable.START.getBytes();
        byte[] end_k = Constants.PassTemplateTable.END.getBytes();

        List<String> templateIds = passes.stream()
                .map(Pass::getTemplateId)
                .collect(Collectors.toList());
        ArrayList<Get> templateGets = new ArrayList<>(templateIds.size());
        templateIds.forEach(t -> templateGets.add(new Get(Bytes.toBytes(t))));

        Result[] templateResults = hbaseTemplate.getConnection()
                .getTable(TableName.valueOf(Constants.PassTemplateTable.TABLE_NAME))
                .get(templateGets);

        // 构造 PassTemplateId -> PassTemplate Object 的 Map，用于构造 PassInfo
        HashMap<String, PassTemplate> templateId2Object = new HashMap<>();

        for (Result item : templateResults) {
            PassTemplate passTemplate = new PassTemplate();

            passTemplate.setId(Bytes.toInt(item.getValue(family_b, id_k)));
            passTemplate.setTitle(Bytes.toString(item.getValue(family_b, title_k)));
            passTemplate.setSummary(Bytes.toString(item.getValue(family_b, summary_k)));
            passTemplate.setDesc(Bytes.toString(item.getValue(family_b, desc_k)));
            passTemplate.setHasToken(Bytes.toBoolean(item.getValue(family_b, has_token_k)));
            passTemplate.setBackground(Bytes.toInt(item.getValue(family_b, background_k)));

            passTemplate.setLimit(Bytes.toLong(item.getValue(family_c, limit_k)));
            passTemplate.setStart(DateUtils.parseDate(
                    Bytes.toString(item.getValue(family_c, start_k)), patterns));
            passTemplate.setEnd(DateUtils.parseDate(
                    Bytes.toString(item.getValue(family_c, end_k)), patterns
            ));

            templateId2Object.put(Bytes.toString(item.getRow()), passTemplate);
        }

        return templateId2Object;
    }

    /**
     * <h2>通过获取的 PassTemplate 对象构造 Merchants Map</h2>
     *
     * @param passTemplates {@link PassTemplate}
     * @return {@link Merchants}
     */
    private Map<Integer, Merchants> buildMerchantsMap(List<PassTemplate> passTemplates) {

        HashMap<Integer, Merchants> merchantsMap = new HashMap<>();
        List<Integer> merchantsIds = passTemplates.stream()
                .map(PassTemplate::getId)
                .collect(Collectors.toList());
        List<Merchants> merchants = merchantsDao.findByIdIn(merchantsIds);
        merchants.forEach(m -> merchantsMap.put(m.getId(), m));

        return merchantsMap;
    }
}
