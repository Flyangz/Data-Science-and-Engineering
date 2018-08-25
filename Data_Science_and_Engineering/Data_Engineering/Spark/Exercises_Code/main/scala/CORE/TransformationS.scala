package CORE

import org.apache.spark.sql.SparkSession

object ScalaTransformation {
  def main(args: Array[String]): Unit = {

    //设置Spark
    val spark = SparkSession
      .builder()
      .appName("Count")
      .master("local")
      .getOrCreate()
    val sc = spark.sparkContext

    //生成数据
//    val list = Array(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
//    val sKey = Array(
//      ("class1", 80),
//      ("class2", 90),
//      ("class1", 70),
//      ("class2", 100),
//      ("class2", 10))
//
//    val iKey = Array(
//      (86,"e"),
//      (90,"a"),
//      (77, "b"),
//      (89,"c"),
//      (100,"d"))

    val studentList = Array(
      (1, "a"),
      (2, "b"),
      (3, "c"),
      (4, "d"),
      (5, "e")
    )

    val scoreList = Array(
      (1, 90),
      (2, 100),
      (3, 88),
      (4, 70),
      (2, 60),
      (1, 60),
      (1, 60),
      (5, 60)
    )


    //生成RDD
    val students = sc.parallelize(studentList,4)
    val scores = sc.parallelize(scoreList,4)

    //对RDD操作
//    val time2 = listRDD.map(v => v * 2)
//    val Sum = listRDD.reduce(_+_)
//    val evenNum = listRDD.filter(v => v % 2 == 0)

    //对pairRDD操作
//    val grouped = listRDD.groupByKey()
//    val reduced = listRDD.reduceByKey(_+_)
//    val sorted = listRDD.sortByKey()

    val joined = students.cogroup(scores)



    //打印结果
//    println(sorted)

    joined.foreach(v => {
      println(v._1)
      println(v._2._1)
      println(v._2._2)
      println("======")
    })

//    sorted.foreach(println)

    sc.stop()
  }

}
