package streaming

import kafka.serializer.StringDecoder
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

object KafkaDirectWC {
  def main(args: Array[String]): Unit = {

    if (args.length != 2) {
      System.err.println("Usage: FlumePushWC <brokers> <topics>")
      System.exit(1)
    }

    val Array(brokers, topics) = args

    val conf = new SparkConf().setMaster("local[2]").setAppName("KafkaDirectWC")
    val ssc = new StreamingContext(conf, Seconds(5))

    val topicsSet = topics.split(",").toSet
    val kafkaParams = Map[String, String]("bootstrap.servers" -> brokers)

    val kafkaStream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topicsSet)
    kafkaStream.map(_._2).flatMap(_.split(" ")).map((_, 1)).reduceByKey(_ + _).print()

    ssc.start()
    ssc.awaitTermination()
  }

}
