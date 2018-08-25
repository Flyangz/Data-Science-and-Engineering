package SQL.Web_Log_Analysis

import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.collection.mutable.ListBuffer

object TopNStatJobYarn {
  def main(args: Array[String]): Unit = {

    if (args.length != 2) {
      println("Usage: logCleanYarn <inputPath> <day>")
      System.exit(1)
    }

    val Array(inputPath, day) = args

    val spark = SparkSession.builder()
      .config("spark.sql.sources.partitionColumnTypeInference.enabled", "false")
      .getOrCreate()

    val accessDF = spark.read.format("parquet").load(inputPath)

    import spark.implicits._
    val commonDF = accessDF.filter($"day" === day && $"cmsType" === "video")
    commonDF.cache()

    StatDAO.deleteData(day)
    //groupBy video
    videoAccessTopNStat(spark, commonDF)

    //groupBy city
    cityAccessTopNStat(spark, commonDF)

    commonDF.unpersist(true)

    //    videoAccessTopDF.show(false)

    spark.stop()
  }

  case class DayVideoAccessStat(day: String, cmsId: Long, times: Long)

  def videoAccessTopNStat(spark: SparkSession, comDF: DataFrame): Unit = {

    import spark.implicits._
    val videoAccessTopNStat = comDF
      .groupBy($"day", $"cmsId")
      .agg(count("cmsId").as("times"))
      .orderBy(desc("times"))

    try {
      videoAccessTopNStat.foreachPartition(partitionOfRecords =>{
        val list = new ListBuffer[DayVideoAccessStat]

        partitionOfRecords.foreach(info => {
          val day = info.getAs[String]("day")
          val cmsId = info.getAs[Long]("cmsId")
          val times = info.getAs[Long]("times")

          list.append(DayVideoAccessStat(day, cmsId, times))
        })

        StatDAO.inserDayVideoAccessTopN(list)//case class没有单独在开一个.scala，使得方法中的引用出错。
      })
    } catch {
      case e:Exception => e.printStackTrace()
    }
  }

  case class DayCityVideoAccessStat(day: String, cmsId: Long, city: String, times: Long, timesRank: Int)

  def cityAccessTopNStat(spark: SparkSession, comDF: DataFrame): Unit = {

    import spark.implicits._

    val videoAccessTopNStat = comDF
      .groupBy($"day", $"city", $"cmsId")
      .agg(count("cmsId").as("times"))

    val windowSpec = Window.partitionBy($"city").orderBy(desc("times"))
    val videoAccessTopNStatDF = videoAccessTopNStat.select(expr("*"), rank().over(windowSpec).as("times_rank"))
      .filter($"times_rank" <= 3)

    try {
      videoAccessTopNStatDF.foreachPartition(partitionOfRecords => {
        val list = new ListBuffer[DayCityVideoAccessStat]

        partitionOfRecords.foreach(info => {
          val day = info.getAs[String]("day")
          val cmsId = info.getAs[Long]("cmsId")
          val city = info.getAs[String]("city")
          val times = info.getAs[Long]("times")
          val timesRank = info.getAs[Int]("times_rank")

          list.append(DayCityVideoAccessStat(day, cmsId, city, times, timesRank))
        })

        StatDAO.inserDayCityVideoAccessTopN(list)
      })
    } catch {
      case e: Exception => e.printStackTrace()
    }
  }
}
