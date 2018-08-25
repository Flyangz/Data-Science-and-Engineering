package SQL.Web_Log_Analysis

import java.util.{Date, Locale}

import org.apache.commons.lang3.time.FastDateFormat

/**
  *  date and time parser tool
  *  notes: SimpleDateFormat is not thread-safe, so we use FastDateFormat
  */
object DataUtils {

  //input_format: [10/Nov/2016:00:01:02 +0800]
  val YYYYMMDDHHMM_TIME_FORMAT = FastDateFormat.getInstance("dd/MMM/yyyy:HH:mm:SS Z", Locale.ENGLISH)

  //output_format: yyyy-MM-dd HH:mm:ss
  val TARGET_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss")

  /**
    * example: [10/Nov/2016:00:01:02 +0800] ==> 2016-11-10 00:01:00
    */
  def parse(time: String) = {
    TARGET_FORMAT.format(new Date(getTime(time)))
  }

  def getTime(time: String) = {
    try {
      YYYYMMDDHHMM_TIME_FORMAT.parse(time.substring(time.indexOf("[") + 1, time.lastIndexOf("]"))).getTime
    } catch {
      case _ => 0l
    }
  }

  def main(args: Array[String]): Unit = {
    println(parse("[10/Nov/2016:00:01:02 +0800]"))
  }
}
