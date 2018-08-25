package CORE

import org.apache.spark.sql.SparkSession

object SharedVarS {
  def main(args: Array[String]): Unit = {

    val spark = SparkSession
      .builder()
      .appName("Share")
      .master("local")
      .getOrCreate()
    val sc = spark.sparkContext

    val factor = 3
//    val bc = sc.broadcast(factor)
    val accum = sc.longAccumulator("My Accum")
    val slist = Array(1, 2, 3, 4, 5)

    val rdd1 = sc.parallelize(slist)

//    val bcResult = rdd1.map(v => v * bc.value)

    rdd1.foreach(v => accum.add(v + 1))

//    bcResult.foreach(println)
    println(accum.value)

    spark.stop()
  }
}
