package project.dao

import org.apache.hadoop.hbase.client.Get
import org.apache.hadoop.hbase.util.Bytes
import project.domain.CourseSearchClickCount
import utils.HBaseUtils

import scala.collection.mutable.ListBuffer

object CourseSearchClickCoutDAO {
  private val tableName = "course_search_clickcount"
  private val columnfamily = "info"
  private val qualifer = "click_count"

  def save(list: ListBuffer[CourseSearchClickCount]): Unit ={
    val table = HBaseUtils.getInstance().getTable(tableName)

    for (elem <- list) {
      table.incrementColumnValue(Bytes.toBytes(elem.day_search_course),
        Bytes.toBytes(columnfamily),
        Bytes.toBytes(qualifer),
        elem.click_count)
    }
  }

  def queryByKeyRow(day_search_course: String): Long ={
    val table = HBaseUtils.getInstance().getTable(tableName)
    val get = new Get(Bytes.toBytes(day_search_course))
    val value = table.get(get).getValue(columnfamily.getBytes(), qualifer.getBytes())

    if (value == null) {
      0l
    } else {
      Bytes.toLong(value)
    }
  }

  def main(args: Array[String]): Unit = {

    val list = new ListBuffer[CourseSearchClickCount]
    list.append(CourseSearchClickCount("20171111_www.baidu.com_8", 8))
    list.append(CourseSearchClickCount("20171111_cn.bing.com_9", 9))
    list.append(CourseSearchClickCount("20171111_www.bing.com_1", 100))

    save(list)
    println(queryByKeyRow("20171111_www.baidu.com_8") + ":" +
      queryByKeyRow("20171111_cn.bing.com_9") + ":" +
      queryByKeyRow("20171111_www.bing.com_1"))
  }
}
