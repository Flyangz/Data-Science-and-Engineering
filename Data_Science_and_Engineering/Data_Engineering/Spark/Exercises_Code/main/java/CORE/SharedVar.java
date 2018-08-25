package CORE;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.util.LongAccumulator;

import java.util.Arrays;
import java.util.List;

public class SharedVar {
    public static void main(String[] args) {

        SparkConf conf = new SparkConf()
                .setAppName("Transformation")
                .setMaster("local");

        JavaSparkContext sc = new JavaSparkContext(conf);

        final int factor= 3;
        final Broadcast<Integer> bc = sc.broadcast(factor);
//        final LongAccumulator ac = sc.longAccumulator();

        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);

        JavaRDD<Integer> tList = sc.parallelize(list);

//        JavaRDD<Integer> bcResult = tList.map(v -> v * bc.value());

//        JavaRDD<Object> acResult = tList.map(v -> ac.add(v));

//        bcResult.foreach(v -> System.out.println(v));

    }
}
