package project.dao

import org.apache.hadoop.hbase.client.Get
import project.domain.CourseClickCount
import utils.HBaseUtils
import org.apache.hadoop.hbase.util.Bytes

import scala.collection.mutable.ListBuffer

object CourseClickCoutDAO {
  private val tableName = "course_clickcount"
  private val columnfamily = "info"
  private val qualifer = "click_count"

  def save(list: ListBuffer[CourseClickCount]): Unit ={
    val table = HBaseUtils.getInstance().getTable(tableName)

    for (elem <- list) {
      table.incrementColumnValue(Bytes.toBytes(elem.day_course),
        Bytes.toBytes(columnfamily),
        Bytes.toBytes(qualifer),
        elem.click_count)
    }
  }

  def queryByKeyRow(day_couse: String): Long ={
    val table = HBaseUtils.getInstance().getTable(tableName)
    val get = new Get(Bytes.toBytes(day_couse))
    val value = table.get(get).getValue(columnfamily.getBytes(), qualifer.getBytes())

    if (value == null) {
      0l
    } else {
      Bytes.toLong(value)
    }
  }

  def main(args: Array[String]): Unit = {

    val list = new ListBuffer[CourseClickCount]
    list.append(CourseClickCount("20171111_8", 8))
    list.append(CourseClickCount("20171111_9", 9))
    list.append(CourseClickCount("20171111_1", 100))

    save(list)
    println(queryByKeyRow("20171111_8") + ":" +
      queryByKeyRow("20171111_9") + ":" +
      queryByKeyRow("20171111_1"))
  }
}
