package SQL.Web_Log_Analysis

import java.sql.{Connection, PreparedStatement}

import SQL.Web_Log_Analysis.TopNStatJob.{DayCityVideoAccessStat, DayVideoAccessStat}

import scala.collection.mutable.ListBuffer

object StatDAO {

  def inserDayVideoAccessTopN(list: ListBuffer[DayVideoAccessStat]): Unit = {

    var connection: Connection = null
    var pstmt: PreparedStatement = null

    try{
      connection = MySQLUtils.getConnect()

      val sql = "insert into day_video_access_topn_stat(day, cms_id, times) values (?, ?, ?)"
      val pstmt = connection.prepareStatement(sql)

      connection.setAutoCommit(false)

      for (ele <- list) {
        pstmt.setString(1, ele.day)
        pstmt.setLong(2, ele.cmsId)
        pstmt.setLong(3, ele.times)

        pstmt.addBatch()
      }

      pstmt.executeBatch()
      connection.commit()

    } catch {
      case e:Exception => e.printStackTrace()
    } finally {
      MySQLUtils.release(connection, pstmt)
    }
  }

  def inserDayCityVideoAccessTopN(list: ListBuffer[DayCityVideoAccessStat]): Unit = {

    var connection: Connection = null
    var pstmt: PreparedStatement = null

    try{
      connection = MySQLUtils.getConnect()

      val sql = "insert into day_video_city_access_topn_stat(day, cms_id, city, times, times_rank) values (?, ?, ?, ?, ?)"
      val pstmt = connection.prepareStatement(sql)

      connection.setAutoCommit(false)

      for (ele <- list) {
        pstmt.setString(1, ele.day)
        pstmt.setLong(2, ele.cmsId)
        pstmt.setString(3, ele.city)
        pstmt.setLong(4, ele.times)
        pstmt.setInt(5, ele.timesRank)

        pstmt.addBatch()
      }

      pstmt.executeBatch()
      connection.commit()

    } catch {
      case e:Exception => e.printStackTrace()
    } finally {
      MySQLUtils.release(connection, pstmt)
    }
  }

  def deleteData(day: String): Unit = {

    val tables = Array("day_video_access_topn_stat", "day_video_city_access_topn_stat")
    var connection: Connection = null
    var pstmt: PreparedStatement = null

    try {
      connection = MySQLUtils.getConnect()

      for (table <- tables) {
        val sql = s"delete from $table where day = ?"
        val pstmt = connection.prepareStatement(sql)
        pstmt.setString(1, day)
        pstmt.executeUpdate()

      }
    } catch {
      case e: Exception => e.printStackTrace()
    } finally {
      MySQLUtils.release(connection, pstmt)
    }

  }
}
