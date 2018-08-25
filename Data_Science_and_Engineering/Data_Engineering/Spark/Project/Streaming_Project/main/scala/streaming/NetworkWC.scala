package streaming

import java.sql.DriverManager

import org.apache.spark._
import org.apache.spark.streaming._
import org.spark_project.jetty.client.ConnectionPool

object NetworkWC {
  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setMaster("local[3]").setAppName("NetworkWordCount")
    val ssc = new StreamingContext(conf, Seconds(4))

    val lines = ssc.socketTextStream("localhost", 6789)
    val res = lines.flatMap(_.split(" ")).map((_, 1)).reduceByKey(_+_)
//    val result = pairs.updateStateByKey[Int](updateFunction _)

//    result.print()
//    res.foreachRDD { rdd =>
//      rdd.foreachPartition { partitionOfRecords =>
//        // ConnectionPool is a static, lazily initialized pool of connections
//        val connection = ConnectionPool.getConnection()
//        partitionOfRecords.foreach(record => connection.send(record))
//        ConnectionPool.returnConnection(connection)  // return to the pool for future reuse
//      }
//    }

    ssc.start()
    ssc.awaitTermination()
  }
  def updateFunction(newValues: Seq[Int], runningCount: Option[Int]): Option[Int] = {
    val curCount = newValues.sum
    val preCount = runningCount.getOrElse(0)
    val newCount = curCount + preCount
    Some(newCount)
  }

  def createConnect() = {
    Class.forName("com.mysql.jdbc.Driver")
    DriverManager.getConnection("jdbc:mysql://localhost:3306/log_project", "root", "password")
  }


}
