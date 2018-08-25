package streaming

import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

object Flume_Kafka_StreamingApp {
  def main(args: Array[String]): Unit = {

    if (args.length != 4) {
      System.err.println("Usage: FlumePushWC <zkQuorum> <group> <topics> <numTreads>")
      System.exit(1)
    }

    val Array(zkQuorum, group, topics, numTreads) = args

    val conf = new SparkConf().setMaster("local[2]").setAppName("FlumePollWC")
    val ssc = new StreamingContext(conf, Seconds(5))

    val topicMap = topics.split(",").map((_, numTreads.toInt)).toMap

    val kafkaStream = KafkaUtils.createStream(ssc, zkQuorum, group, topicMap)
    kafkaStream.map(_._2).count().print()
    //flatMap(_.split(" ")).map((_, 1)).reduceByKey(_ + _).print()

    ssc.start()
    ssc.awaitTermination()
  }

}
