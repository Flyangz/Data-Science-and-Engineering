package SQL.Web_Log_Analysis

import org.apache.spark.sql.SparkSession

object logTrans {
  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("logTrans")
      .master("local[*]")
      .getOrCreate()

    val path = "file:///Users/flyang/Documents/self-teaching/Data_Resources/log/50000_access.log"
    val access = spark.sparkContext.textFile(path)

//    access.take(10).foreach(println)
//    log_content：id, time, Post http_version, operation, traffic ...

    val splited = access.map(line => {

      val splits = line.split(" ")
      val ip = splits(0)
      val time = splits(3) + " " + splits(4)
      val url = splits(11).replaceAll("\"", "") //remove quotation mark
      val traffic = splits(9)
//      (ip, DataUtils.parse(time), url, traffic)

      DataUtils.parse(time) + "\t" + url + "\t" + traffic + "\t" + ip
    })

    splited.saveAsTextFile("file:///Users/flyang/Documents/self-teaching/Data_Resources/log/output/")

    spark.stop()
  }
}
