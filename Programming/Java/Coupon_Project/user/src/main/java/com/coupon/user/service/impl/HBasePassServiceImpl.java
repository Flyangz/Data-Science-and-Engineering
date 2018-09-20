package com.coupon.user.service.impl;

import com.coupon.user.constant.Constants;
import com.coupon.user.service.IHBasePassService;
import com.coupon.user.utils.RowKeyGenUtil;
import com.coupon.user.vo.PassTemplate;
import com.spring4all.spring.boot.starter.hbase.api.HbaseTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * <h1>Pass HBase 服务</h1>
 */
@Slf4j
@Service
public class HBasePassServiceImpl implements IHBasePassService {

    private final HbaseTemplate hbaseTemplate;

    // 这个构造函数其实可以不写，但 IDEA 会有警告提示
    @Autowired
    public HBasePassServiceImpl(HbaseTemplate hbaseTemplate) {
        this.hbaseTemplate = hbaseTemplate;
    }

    @Override
    public boolean dropPassTemplateToHBase(PassTemplate passTemplate) {

        if (passTemplate == null) {
            return false;
        }

        String rowKey = RowKeyGenUtil.genPassTemplateRowKey(passTemplate);

        try {
            if (hbaseTemplate.getConnection().getTable(TableName.valueOf(Constants.PassTemplateTable.TABLE_NAME)).exists(new Get(Bytes.toBytes(rowKey)))) {
                log.warn("RowKey {} already exists!", rowKey);
                return false;
            }
        } catch (IOException e) {
            log.error("DropPassTemplateToHBase Error: {}", e.getMessage());
            return false;
        }

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

        /** 优惠券 passTemplate 所有属性值的 byte 形式 */
        byte[] id_v = Bytes.toBytes(passTemplate.getId());
        byte[] title_v = Bytes.toBytes(passTemplate.getTitle());
        byte[] summary_v = Bytes.toBytes(passTemplate.getSummary());
        byte[] desc_v = Bytes.toBytes(passTemplate.getDesc());
        byte[] has_token_v = Bytes.toBytes(passTemplate.getHasToken());
        byte[] background_v = Bytes.toBytes(passTemplate.getBackground());
        byte[] limit_v = Bytes.toBytes(passTemplate.getLimit());
        byte[] start_v = Bytes.toBytes(DateFormatUtils.ISO_DATE_FORMAT.format(passTemplate.getStart()));
        byte[] end_v = Bytes.toBytes(DateFormatUtils.ISO_DATE_FORMAT.format(passTemplate.getEnd()));

        Put put = new Put(Bytes.toBytes(rowKey));

        ArrayList<byte[]> families = new ArrayList<byte[]>(Arrays.asList(family_b, family_c));

        ArrayList<byte[]> keys = new ArrayList<byte[]>(Arrays.asList(id_k,
                title_k, summary_k, desc_k, has_token_k, background_k, limit_k, start_k, end_k));

        ArrayList<byte[]> values = new ArrayList<byte[]>(Arrays.asList(id_v,
                title_v, summary_v, desc_v, has_token_v, background_v, limit_v, start_v, end_v));

        /** 9 是 优惠券 passTemplate 的属性数量。其中 6 个属于FAMILY_B，3 个属于FAMILY_O */
        for (int i = 0; i < 9; i++) {
            if (i <= 5) {
                put.addColumn(families.get(0), keys.get(i), values.get(i));
            } else {
                put.addColumn(families.get(1), keys.get(i), values.get(i));
            }
        }

        hbaseTemplate.saveOrUpdate(Constants.PassTemplateTable.TABLE_NAME, put);

        return true;
    }
}
