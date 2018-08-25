package d7

import org.apache.spark.{HashPartitioner, SparkConf}
import org.apache.spark.streaming.{Seconds, StreamingContext}

object StreamingTest {
  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setMaster("local[2]").setAppName("StreamingWC")
    val ssc = new StreamingContext(conf, Seconds(5))

    ssc.checkpoint("..")

    val dStream = ssc.socketTextStream("localhose", 9999)

    val tuples = dStream.flatMap(_.split(" ")).map((_,1))

    val res = tuples.updateStateByKey(func, new HashPartitioner(ssc.sparkContext.defaultParallelism), false)

    res.print()

    ssc.start()
    ssc.awaitTermination()
  }
  val func = (it: Iterator[(String, Seq[Int], Option[Int])]) => {
    it.map(t => {
      (t._1, t._2.sum + t._3.getOrElse(0))
    })
  }
}
