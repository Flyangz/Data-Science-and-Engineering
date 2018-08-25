package CORE.Sort;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.SparkSession;
import scala.Array;
import scala.Tuple2;

import java.util.regex.Pattern;

public class CustomSort {

    private static final Pattern SPACE = Pattern.compile(" ");
    
    public static void main(String[] args) {

        //Spark设置
        SparkSession spark = SparkSession
                .builder()
                .master("local")
                .appName("CustomSort")
                .getOrCreate();
        JavaRDD<String> lines = spark.read().textFile("test2.txt").javaRDD();
//        JavaRDD<String[]> splitedLine = lines.map(SPACE::split);
//        JavaRDD<String> res = lines.sortBy((line -> line.split("\\s")[1]), false, 1);

        JavaPairRDD<SecondarySortKey, String> pairs = lines.mapToPair(line -> {
            String[] lineSplited = line.split("\\s");
            SecondarySortKey key = new SecondarySortKey(
                    Integer.valueOf(lineSplited[1]),
                    Integer.valueOf(lineSplited[2]));
            return new Tuple2<>(key, line);
        });

        JavaPairRDD<SecondarySortKey, String> result = pairs.sortByKey();
//        result.foreach(v -> System.out.println(v._2));
//        tuple3JavaRDD.foreach(v -> System.out.println(v));

//        res.foreach(v -> System.out.println(v));

        spark.stop();
    }
}

