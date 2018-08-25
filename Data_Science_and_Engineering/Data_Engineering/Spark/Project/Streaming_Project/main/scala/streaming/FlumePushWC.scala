package streaming

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.flume._

object FlumePushWC {
  def main(args: Array[String]): Unit = {

    if (args.length != 2) {
      System.err.println("Usage: FlumePushWC <hostname> <port>")
      System.exit(1)
    }

    val Array(hostname, port) = args

    val conf = new SparkConf()//.setMaster("local[2]").setAppName("FlumePushWC")
    val ssc = new StreamingContext(conf, Seconds(5))

    val flumeStream = FlumeUtils.createStream(ssc, hostname, port.toInt)

    flumeStream.map(x => new String(x.event.getBody.array()).trim)
        .flatMap(_.split(" ")).map((_, 1)).reduceByKey(_+_).print()

    ssc.start()
    ssc.awaitTermination()

  }

}
