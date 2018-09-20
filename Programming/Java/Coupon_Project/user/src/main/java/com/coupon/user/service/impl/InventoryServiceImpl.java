package com.coupon.user.service.impl;

import com.coupon.user.constant.Constants;
import com.coupon.user.dao.MerchantsDao;
import com.coupon.user.entity.Merchants;
import com.coupon.user.mapper.PassTemplateRowMapper;
import com.coupon.user.service.IInventoryService;
import com.coupon.user.service.IUserPassService;
import com.coupon.user.utils.RowKeyGenUtil;
import com.coupon.user.vo.*;
import com.spring4all.spring.boot.starter.hbase.api.HbaseTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.LongComparator;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <h1>获取库存信息，只返回用户没有领取的</h1>
 */
@Slf4j
@Service
public class InventoryServiceImpl implements IInventoryService {

    private final HbaseTemplate hbaseTemplate;

    private final MerchantsDao merchantsDao;

    private final IUserPassService userPassService;

    @Autowired
    public InventoryServiceImpl(HbaseTemplate hbaseTemplate,
                                MerchantsDao merchantsDao,
                                IUserPassService userPassService) {
        this.hbaseTemplate = hbaseTemplate;
        this.merchantsDao = merchantsDao;
        this.userPassService = userPassService;
    }


    @Override
    @SuppressWarnings("unchecked")
    public Response getInventoryInfo(Long userId) throws Exception {

        // 1）先获取用户所有的优惠券
        Response allPassInfo = userPassService.getUserAllPassInfo(userId);
        List<PassInfo> allPassInfoData = (List<PassInfo>) allPassInfo.getData();
        List<PassTemplate> excludeObject = allPassInfoData.stream()
                .map(PassInfo::getPassTemplate)
                .collect(Collectors.toList());

        // 2）构建需要排除的优惠券的列表
        List<String> excludeIds = new ArrayList<>();
        excludeObject.forEach(e -> excludeIds.add(
                RowKeyGenUtil.genPassTemplateRowKey(e)));

        return new Response(new InventoryResponse(userId,
                buildPassTemplateInfo(getAvailablePassTemplate(excludeIds))));
    }

    /**
     * <h2>获取系统中可用的优惠券</h2>
     * 可用领取的优惠券：
     * 1）库存优惠券数量检验：是否还有 or 没有上限。
     * 2）用户是否已领取
     * 3）优惠券是否在可使用时间范围
     * 注意：此处实现方法并不适合优惠券种类过多的情况。因为这里是先把符合条件 1 的优惠券都取出。
     * 改进：分页处理，缓存等。
     * @param excludeIds 需要排除的优惠券 ids
     * @return {@link PassTemplate}
     */
    private List<PassTemplate> getAvailablePassTemplate(List<String> excludeIds) {

        // 库存优惠券数量检验
        FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ONE);

        filterList.addFilter(
                new SingleColumnValueFilter(
                        Bytes.toBytes(Constants.PassTemplateTable.FAMILY_C),
                        Bytes.toBytes(Constants.PassTemplateTable.LIMIT),
                        CompareFilter.CompareOp.GREATER,
                        new LongComparator(0L)
                )
        );

        filterList.addFilter(
                new SingleColumnValueFilter(
                        Bytes.toBytes(Constants.PassTemplateTable.FAMILY_C),
                        Bytes.toBytes(Constants.PassTemplateTable.LIMIT),
                        CompareFilter.CompareOp.EQUAL,
                        Bytes.toBytes("-1")
                )
        );

        Scan scan = new Scan();
        scan.setFilter(filterList);

        List<PassTemplate> validTemplates = hbaseTemplate.find(
                Constants.PassTemplateTable.TABLE_NAME, scan, new PassTemplateRowMapper()
        );
        ArrayList<PassTemplate> availablePassTemplates = new ArrayList<>();

        Date cur = new Date();

        for (PassTemplate validTemplate : validTemplates) {

            // 用户是否已领取
            if (excludeIds.contains(RowKeyGenUtil.genPassTemplateRowKey(validTemplate))) {
                continue;
            }

            // 优惠券是否在可使用时间范围
            if (cur.getTime() >= validTemplate.getStart().getTime()
                    && cur.getTime() <= validTemplate.getEnd().getTime()) {
                availablePassTemplates.add(validTemplate);
            }
        }

        return availablePassTemplates;
    }

    /**
     * <h2>构造优惠券的信息</h2>
     * 1）根据优惠券取得对应的商户 id
     * 2）从数据库中取出这些商户对象
     * 3）用 HashMap 来存 商户
     * 4）遍历 passTemplates 并取出对应的 商户 ，两者组合实例化 PassTemplateInfo
     * 疑问：merchantsIds 改为 set 更好？
     *
     * @param passTemplates {@link PassTemplate}
     * @return {@link PassTemplateInfo}
     */
    private List<PassTemplateInfo> buildPassTemplateInfo(List<PassTemplate> passTemplates) {

        // 1）根据优惠券取得对应的商户 id
        List<Integer> merchantsIds = passTemplates.stream()
                .map(PassTemplate::getId)
                .collect(Collectors.toList());

        // 2）从数据库中取出这些商户对象
        List<Merchants> merchants = merchantsDao.findByIdIn(merchantsIds);

        // 3）用 map 来存 商户，往后通过 优惠券的商户id 来取
        HashMap<Integer, Merchants> merchantsMap = new HashMap<>();
        merchants.forEach(m -> merchantsMap.put(m.getId(), m));

        ArrayList<PassTemplateInfo> result = new ArrayList<>(passTemplates.size());

        // 4）遍历 passTemplates 并取出对应的 商户 ，两者组合实例化 PassTemplateInfo
        for (PassTemplate passTemplate : passTemplates) {

            Merchants mc = merchantsMap.getOrDefault(passTemplate.getId(), null);

            if (mc == null) {
                log.error("Merchants Error: {}", passTemplate.getId());
                continue;
            }

            result.add(new PassTemplateInfo(passTemplate, mc));
        }

        return result;
    }
}
