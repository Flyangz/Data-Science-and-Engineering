package CORE;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;
import shapeless.ops.tuple;

import java.util.Arrays;
import java.util.List;

public class Transformation {
    public static void main(String[] args) {

        //Spark设置
        SparkConf conf = new SparkConf()
                .setAppName("Transformation")
                .setMaster("local");

        JavaSparkContext sc = new JavaSparkContext(conf);

        //生成数据
//        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
//        List<Tuple2<String, Integer>> groupList = Arrays.asList(
//                new Tuple2<>("class1", 80),
//                new Tuple2<>("class2", 80),
//                new Tuple2<>("class1", 70),
//                new Tuple2<>("class3", 80),
//                new Tuple2<>("class2", 90)
//        );

        List<Tuple2<Integer, String>> studentList = Arrays.asList(
                new Tuple2<>(1, "a"),
                new Tuple2<>(2, "b"),
                new Tuple2<>(3, "c"),
                new Tuple2<>(4, "d"),
                new Tuple2<>(5, "e")
        );

        List<Tuple2<Integer, Integer>> scoreList = Arrays.asList(
                new Tuple2<>(1, 90),
                new Tuple2<>(2, 100),
                new Tuple2<>(3, 88),
                new Tuple2<>(4, 70),
                new Tuple2<>(2, 60),
                new Tuple2<>(1, 60),
                new Tuple2<>(1, 60),
                new Tuple2<>(5, 60)
                );

        //生成RDD
//        JavaRDD<Integer> listRDD = sc.parallelize(list,4);
//        JavaPairRDD<String, Integer> groupPairs = sc.parallelizePairs(groupList, 4);
        JavaPairRDD<Integer, String> students = sc.parallelizePairs(studentList);
        JavaPairRDD<Integer, Integer> scores = sc.parallelizePairs(scoreList);

        //对RDD的操作
        //parallelize生成的RDD
//        JavaRDD<Integer> times2 = listRDD.map(v -> v * 2);
//        Integer sum = times2.reduce(Integer::sum);
//        JavaRDD<Integer> evenNum = listRDD.filter(v -> v % 2 == 0);

        //parallelizePairs生成的RDD
//        JavaPairRDD<String, Iterable<Integer>> grouped = groupPairs.groupByKey();
//        JavaPairRDD<String, Integer> reduced = groupPairs.reduceByKey(Integer::sum);
//        JavaPairRDD<String, Integer> sorted = groupPairs.sortByKey();

        //下面两个不知道为什么类型推断出错，不赋值的话可直接打印结果
//        JavaPairRDD<Integer, Tuple2<String, String>> joined = students.join(scores);
//        JavaPairRDD<Integer, Tuple2<Iterable<String>, Iterable<Integer>>> cogroup = students.cogroup(scores);

        //打印结果
        students.cogroup(scores).foreach(v -> {
            System.out.println(v._1);
//            for(Integer i : v._2) System.out.println(i);
            System.out.println(v._2._1);
            System.out.println(v._2._2);
            System.out.println("===========");
        });
        sc.close();

    }
}
