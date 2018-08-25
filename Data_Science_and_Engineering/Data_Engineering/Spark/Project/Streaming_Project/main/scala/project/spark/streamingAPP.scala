package project.spark

import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import project.dao.{CourseClickCoutDAO, CourseSearchClickCoutDAO}
import project.domain.{ClickLog, CourseClickCount, CourseSearchClickCount}
import project.utils.DateUtils

import scala.collection.mutable.ListBuffer

object streamingAPP {
  def main(args: Array[String]): Unit = {

    if (args.length != 4) {
      System.err.println("Usage: FlumePushWC <zkQuorum> <group> <topics> <numTreads>")
      System.exit(1)
    }

    val Array(zkQuorum, group, topics, numTreads) = args

    val conf = new SparkConf().setMaster("local[2]").setAppName("streamingAPP")
    val ssc = new StreamingContext(conf, Seconds(60))

    val topicMap = topics.split(",").map((_, numTreads.toInt)).toMap

    val kafkaStream = KafkaUtils.createStream(ssc, zkQuorum, group, topicMap)
    //kafkaStream.map(_._2).flatMap(_.split(" ")).map((_, 1)).reduceByKey(_ + _).print()

    val cleanData = kafkaStream.map(_._2)
      .map(line => {
        val info = line.split("\t")

        //info(2) = "GET /class/130.html HTTP/1.1"
        // url = /class/130.html
        val url = info(2).split(" ")(1)
        var courseId = 0

        if (url.startsWith("/class")) {
          val courseIdHTML = url.split("/")(2)
          courseId = courseIdHTML.substring(0, courseIdHTML.lastIndexOf(".")).toInt
        }

        ClickLog(info(0), DateUtils.parseToMinute(info(1)), courseId, info(3).toInt, info(4))
      }).filter(clicklog => clicklog.courseId != 0)

//    cleanData.print()

    cleanData.map(x => {
      (x.time.substring(0, 8) + "_" + x.courseId, 1)
    })
      .reduceByKey(_+_)
      .foreachRDD(rdd => {
      rdd.foreachPartition(partitionRecords => {
        val list = new ListBuffer[CourseClickCount]
        partitionRecords.foreach(pair => {
          list.append(CourseClickCount(pair._1, pair._2))
        })
        CourseClickCoutDAO.save(list)
      })
    })

    cleanData.map(x => {
      //https://cn.bing.com/search?q=Spark
      val refer = x.ref.replaceAll("//", "/")
      val splits = refer.split("/")
      var host = ""

      if (splits.length > 2) {
        host = splits(1)
      }
      (host, x.courseId, x.time)
    }).filter(_._1 != "")
      .map(x => (x._3.substring(0, 8) + "_" + x._1 + "_" + x._2, 1))
      .reduceByKey(_+_).foreachRDD(rdd => {
      rdd.foreachPartition(partitionRecords => {
        val list = new ListBuffer[CourseSearchClickCount]
        partitionRecords.foreach(pair => {
          list.append(CourseSearchClickCount(pair._1, pair._2))
        })
        CourseSearchClickCoutDAO.save(list)
      })
    })

    ssc.start()
    ssc.awaitTermination()
  }

}
