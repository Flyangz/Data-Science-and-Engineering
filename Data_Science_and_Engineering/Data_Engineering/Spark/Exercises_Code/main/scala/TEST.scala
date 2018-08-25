import org.apache.spark
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.types.{LongType, StringType, StructField, StructType}
import org.apache.spark.sql.functions.{expr, col, column}


object TEST {
  def main(args: Array[String]): Unit = {

    val spark = SparkSession
      .builder()
      .master("local")
      .appName("TEST")
      .getOrCreate()

    val df = spark.read.format("csv")
      .option("header", "true")
      .option("inferSchema", "true")
      .load("/Users/flyang/Documents/self-teaching/Data_Resources/Spark-The-Definitive-Guide-master/data/retail-data/by-day/2010-12-01.csv")



      //    val myManualSchema = StructType(Array(
//      StructField("some", StringType, true),
//      StructField("col", StringType, true),
//      StructField("names", LongType, false)))
//    val myRows = Seq(Row("Hello", "cat", 1L))
//    val myRDD = spark.sparkContext.parallelize(myRows)
//    val myDf = spark.createDataFrame(myRDD, myManualSchema)
//
//    myDf.select(expr("some as A").alias("C")).printSchema()

    spark.stop()
  }
}
