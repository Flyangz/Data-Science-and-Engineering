package utils;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;

public class HBaseUtils {

    HBaseAdmin admin = null;
    Configuration conf = null;

    private HBaseUtils() {
        conf = new Configuration();
        conf.set("hbase.zookeeper.quorum", "localhost:2181");
        conf.set("hbase.rootdir", "hdfs://localhost:8020/hbase");

        try {
            admin = new HBaseAdmin(conf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static HBaseUtils instance = null;

    public static synchronized HBaseUtils getInstance() {
        if (null == instance) {
            instance = new HBaseUtils();
        }
        return instance;
    }

    public HTable getTable(String tableName) {
        HTable table = null;
        try {
            table = new HTable(conf, tableName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return table;
    }

    private void put(String tableName, String rowkey, String columnfamily, String column, String value) {
        HTable table = getTable(tableName);
        Put put = new Put(Bytes.toBytes(rowkey));
        put.add(Bytes.toBytes(columnfamily), Bytes.toBytes(column), Bytes.toBytes(value));

        try {
            table.put(put);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据表名和输入条件获取HBase的记录数
     */
//    public Map<String, Long> query(String tableName, String condition) throws Exception {
//
//        Map<String, Long> map = new HashMap<>();
//
//        HTable table = getTable(tableName);
//        String cf = "info";
//        String qualifier = "click_count";
//
//        Scan scan = new Scan();
//
//        Filter filter = new PrefixFilter(Bytes.toBytes(condition));
//        scan.setFilter(filter);
//
//        ResultScanner rs = table.getScanner(scan);
//        for(Result result : rs) {
//            String row = Bytes.toString(result.getRow());
//            long clickCount = Bytes.toLong(result.getValue(cf.getBytes(), qualifier.getBytes()));
//            map.put(row, clickCount);
//        }
//
//        return  map;
//    }

    public static void main(String[] args) throws Exception {
//        Htable table = HBaseUtils.getInstance().getTable("course_clickcount");
        //query("yys_course_clickcount" , "20180220");

        String tableName = "course_clickcount";
        String rowkey = "20171111_88";
        String columnfamily = "info";
        String column = "click_count";
        String value = "2";

        HBaseUtils.getInstance().put(tableName, rowkey, columnfamily, column, value);
//        System.out.println(table.getName().getNameAsString());
//
//
//        for (Map.Entry<String, Long> entry : map.entrySet()) {
//            System.out.println(entry.getKey() + " : " + entry.getValue());
//        }
    }
}
