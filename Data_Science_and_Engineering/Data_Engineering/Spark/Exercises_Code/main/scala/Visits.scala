//import java.sql.DriverManager
//
//import org.apache.spark.{SparkConf, SparkContext}
//
//object Visits {
//  def main(args: Array[String]): Unit = {
//
//    val conf = new SparkConf().setAppName("ObjectCount").setMaster("local[2]")
//    val sc = new SparkContext(conf)
//
//    //obtain data
//    val ipInfo = sc.textFile("1.txt").map(line => {
//      val fields = line.split("|")
//      val startIp = fields(2)
//      val endIp = fields(3)
//      val province = fields(6)
//      (startIp, endIp, province)
//    }).collect()
//
//    //broadcast task table
//    val broadcast = sc.broadcast(ipInfo)
//    val provinceAndOne = sc.textFile("2.txt").map(line => {
//      val fields = line.split("\\|")
//      val ip = fields(1)
//      val ipToLong = ip2Long(ip)
//      val arr = broadcast.value
//      val index = binarySearch(arr,ipToLong)
//      val provice = arr(index)._3
//      (provice, 1)
//    })
//    //computer the result
//    val result = provinceAndOne.reduceByKey(_+_)
//    result.foreachPartition(data2MySql)
//
//    sc.stop()
//  }
//}
//
////transform Ip address to Long type
//def ip2Long(ip: String): Long = {
//  val fragments =ip.split("[.]")
//  var ipNum = 0L
//  for (i <- 0 until(fragments.length)){
//    ipNum = fragments(i).toLong | ipNum << 8L
//  }
//  ipNum
//}
//
//def binarySearch(arr : Array[(String, String, String)], ip: Long): Int = {
//  var low =0
//  var high = arr.length
//  while (low <= high){
//    val mid = (low + high) / 2
//    if ((ip >= arr(mid)._1.toLong) && (ip <= arr(mid)._2.toLong)){
//      return mid
//    }
//    if (ip < arr(mid)._1.toLong){
//      high = mid - 1
//    }
//    else{
//      low = mid + 1
//    }
//  }
//  -1
//}
//
//val data2MySql = (it: Iterator[(String, Int)]) => {
//
//}