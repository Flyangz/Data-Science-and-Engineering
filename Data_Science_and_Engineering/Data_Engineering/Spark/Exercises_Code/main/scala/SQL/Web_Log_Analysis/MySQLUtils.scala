package SQL.Web_Log_Analysis

import java.sql.{Connection, DriverManager, PreparedStatement}

object MySQLUtils {

  def getConnect() = {
    DriverManager.getConnection("jdbc:mysql://localhost:3306/log_project","flyang", "password")
  }

  def release(connection: Connection, pstmt: PreparedStatement): Unit ={
    try{
      if (pstmt != null) {
        pstmt.close()
      }
    } catch {
      case e: Exception => e.printStackTrace()
    } finally {
      if (connection != null) {
        connection.close()
      }
    }
  }

  def main(args: Array[String]): Unit = {
    println(getConnect())
  }

}
