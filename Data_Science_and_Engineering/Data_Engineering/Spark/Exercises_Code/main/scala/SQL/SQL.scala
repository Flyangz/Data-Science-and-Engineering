package SQL

import org.apache.spark.sql.SparkSession

object SQL {
  def main(args: Array[String]): Unit = {
    val ss = SparkSession
      .builder()
      .appName("SQL")
      .config("spark.master", "local")
      .getOrCreate()


    val df = ss.read.json("...")

    df.createTempView("table_name")

    val query = "select * from table_name order by age desc limit 2"
    val df1 = ss.sql(query)

    df1.write.json(args(1))

    ss.stop()
  }
}

