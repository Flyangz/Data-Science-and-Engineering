package SQL.Web_Log_Analysis

import org.apache.spark.sql.SparkSession

object logCleanYarn {

  def main(args: Array[String]): Unit = {

    if (args.length != 2) {
      println("Usage: logCleanYarn <inputPath> <outputPath>")
      System.exit(1)
    }

    val Array(inputPath, outputPath) = args

    val spark = SparkSession.builder().getOrCreate()

    val access = spark.sparkContext.textFile(inputPath)

    val accessDF = spark.createDataFrame(access.map(line => AccessConvertUtil.parseLog(line)), AccessConvertUtil.struct)

    accessDF.coalesce(1).write.format("parquet").partitionBy("day")
      .save(outputPath)

    spark.stop()
  }
}
