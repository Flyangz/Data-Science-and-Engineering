package CORE;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.SparkSession;
import scala.Tuple2;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class WCount {
    private static final Pattern SPACE = Pattern.compile(" ");

    public static void main(String[] args) throws Exception {

//        if (args.length < 1) {
//            System.err.println("Usage: JavaWordCount <file>");
//            System.exit(1);
//        }

        SparkSession spark = SparkSession
                .builder()
                .master("local")//submit时默认为local，这行要删去
                .appName("JavaWordCount")
                .getOrCreate();

        JavaRDD<String> lines = spark.read().textFile("test1.txt").javaRDD().cache();//在submit时，path改为hadoop的文件

        JavaRDD<String> words = lines.flatMap(s -> Arrays.asList(SPACE.split(s)).iterator());

        JavaPairRDD<String, Integer> ones = words.mapToPair(s -> new Tuple2<>(s, 1));

        JavaPairRDD<String, Integer> counts = ones.reduceByKey(Integer::sum);

        //下面推断出错，正常swap后依然是JavaPairRDD。如果属正常，则只能每次new Tuple2了。
//        counts.map(Tuple2::swap).sortByKey().map(Tuple2::swap)
        JavaPairRDD<String, Integer> sortedByValue = counts.mapToPair(p -> new Tuple2<>(p._2, p._1))
                .sortByKey()
                .mapToPair(p -> new Tuple2<>(p._2, p._1));


//        List<Tuple2<String, Integer>> output = counts.collect();
//        for (Tuple2<?,?> tuple : output) {
//            System.out.println(tuple._1() + ": " + tuple._2());
//        }
//        counts.foreach(s -> System.out.println(s));
        sortedByValue.foreach(v -> System.out.println(v));
        spark.stop();
    }
}
