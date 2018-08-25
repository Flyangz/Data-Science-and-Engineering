package CORE


import org.apache.spark.{Partitioner, SparkConf, SparkContext}

import scala.collection.mutable

object ObjectCount {
  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setAppName("ObjectCount").setMaster("local[2]")
    val sc = new SparkContext(conf)

    //obtain data
    val file = sc.textFile("1.txt")

    //abstract URL
    val urlAndOne = file.map(line => {
      val fields = line.split("\t")
      val url = fields(1)
      (url, 1)
    })

    //partition3
//    urlAndOne.map(t => {
//      val key = t._1
//      val r = Random(100)
//      (key + "_" + r, 1)
//    }).reduceByKey(_+_).map(t => {
//      val key = t._1
//      val count = t._2
//      val w = key.split("_")(0)
//      (w, count)
//    })
    //aggregate by same URL
    val sameUrl = urlAndOne.reduceByKey(_ + _)

    //obtain subject information
    val cachedSubject = sameUrl.map(x => {
      val url = x._1
      val count = x._2
      val subject = new java.net.URL(url).getHost
      (subject, (url, count))
    }).cache()

    //group by subject and get the result
    //    (subject, url, count)
    //    val result = subject.groupBy(_._1).mapValues(_.toList.sortBy(_._3).reverse.take(3))

    //    //partition1
    //    val result = cachedSubject.partitionBy(new HashPartitioner(3))
    //    result.saveAsTextFile("")

    //partition2
    val subjects = cachedSubject.keys.distinct().collect()
    val partitioner = new ProjectPartitioner(subjects)

    val partitioned = cachedSubject.partitionBy(partitioner) //这里原数据cachedSubject就根据subject进行分区了
    val result = partitioned.mapPartitions(it => {
      it.toList.sortBy(_._2._2).reverse.take(3).iterator //这里(_._2._2)取了count，(subject, (url, count)
    })


    result.saveAsTextFile("/out")
    //    println(result.collect().toBuffer)

    sc.stop()

  }
}

class ProjectPartitioner(subjects: Array[String]) extends Partitioner{
  //store subjects and partition number
  private val subjectAndPartNum = new mutable.HashMap[String, Int]()
  //counter used to record partition number
  var n = 0

  for (pro <- subjects){
    subjectAndPartNum += (pro -> n)
    n += 1
  }
  override def numPartitions: Int = subjects.length

  override def getPartition(key: Any): Int = {
    subjectAndPartNum.getOrElse(key.toString, 0)
  }
}
