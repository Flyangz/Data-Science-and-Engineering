package SQL.Web_Log_Analysis

import org.apache.spark.sql.SparkSession

object logClean {

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("logTrans")
      .master("local[*]")
      .getOrCreate()

    val path = "file:///Users/flyang/Documents/self-teaching/Data_Resources/log/access.log"
    val access = spark.sparkContext.textFile(path)

//    access.take(10).foreach(println)
    val accessDF = spark.createDataFrame(access.map(line => AccessConvertUtil.parseLog(line)), AccessConvertUtil.struct)

//    accessDF.printSchema()
//    accessDF.show(false)

    accessDF.coalesce(1).write.format("parquet").partitionBy("day")
      .save("Users/flyang/Documents/self-teaching/Data_Resources/log/clean")

    spark.stop()
  }
}
