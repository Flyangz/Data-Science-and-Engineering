package d7

import org.apache.spark.{ SparkConf}
import org.apache.spark.streaming.{Seconds, StreamingContext}

object StreamingWithWindow {
  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setMaster("local[2]").setAppName("StreamingWC")
    val ssc = new StreamingContext(conf, Seconds(5))

    ssc.checkpoint("..")

    val dStream = ssc.socketTextStream("localhose", 9999)

    val tuples = dStream.flatMap(_.split(" ")).map((_,1))

    val res = tuples.reduceByKeyAndWindow((a:Int, b:Int) => (a + b), Seconds(10), Seconds(10))
    res.print()

    ssc.start()
    ssc.awaitTermination()
  }
}
