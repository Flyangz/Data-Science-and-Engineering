package CORE

import org.apache.spark.sql.SparkSession

object ScalaWCount {
  def main(args: Array[String]): Unit = {

    val spark = SparkSession
      .builder()
      .appName("Count")
      .master("local")
      .getOrCreate()
    val sc = spark.sparkContext

    val lines = sc.textFile("test1.txt")
    val words = lines.flatMap(_.split(" "))//将每行的结果合成一个
    val pairs = words.map((_, 1))
    val count = pairs.reduceByKey(_+_)//对Tuple2的RDD进行隐式转换成PairRDDFunction，提供reduceByKey
    val sorted = count.sortBy(_._2, false)

    //打印结果
    sorted.foreach(println)
    println(sorted.collect().toBuffer) //不加toBuffer会显示Array的引用


    spark.stop()
  }

}
