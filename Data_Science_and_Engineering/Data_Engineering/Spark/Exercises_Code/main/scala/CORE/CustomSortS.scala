package CORE

import org.apache.spark.sql.SparkSession



object CustomSortS {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName("CustomSort")
      .master("local")
      .getOrCreate()
    val sc = spark.sparkContext

    val info = sc.parallelize(Array(("aaa", 80, 25), ("bbb", 90, 26), ("ccc", 90, 27)))
//    val result = info.sortBy(_._2, false)

    //one
//    import MySort.girlOrdering
//    val result = info.sortBy(x => Girl(x._2, x._3), false)

    //two
//    val result = info.sortBy(x => Girl(x._2, x._3), false)

    //three，最简便
    val result = info.sortBy{case (id, fv, age) => (-fv, age)}

    println(result.collect().toBuffer)

    sc.stop()
  }
}



// method one
//case class Girl(faceValue : Int, age : Int){}

//object MySort{
//  implicit val girlOrdering = new Ordering[Girl]{
//    override def compare(x: Girl, y: Girl)  = {
//      if (x.faceValue != y.faceValue){
//        x.faceValue - y.faceValue
//      }else{
//        y.age - x.age
//      }
//    }
//  }
//}

// method two

// case class Girl(faceValue : Int, age : Int) extends Ordered[Girl]{
//  override def compare(that: Girl) = {
//    if (this.faceValue != that.faceValue){
//      this.faceValue - that.faceValue
//    }else{
//      that.age - this.age
//    }
//  }
//}