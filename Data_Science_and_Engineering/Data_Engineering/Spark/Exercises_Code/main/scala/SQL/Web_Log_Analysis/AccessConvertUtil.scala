package SQL.Web_Log_Analysis

import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}

object AccessConvertUtil {

  val struct = StructType(Seq(
    StructField("url", StringType),
    StructField("cmsType", StringType),
    StructField("cmsId", IntegerType),
    StructField("traffic", IntegerType),
    StructField("ip", StringType),
    StructField("city", StringType),
    StructField("time", StringType),
    StructField("day", StringType)
  ))

  def parseLog(log: String) = {

    try{
      val splited = log.split("\t")

      val url = splited(1)
      val traffic = splited(2).toInt
      val ip = splited(3)

      val domain = "http://www.imooc.com/"
      val cms = url.substring(url.indexOf(domain) + domain.length)
      val cmsTypeId = cms.split("/")

      var cmsType = ""
      var cmsId = 0
      if (cmsTypeId.length > 1) {
        cmsType = cmsTypeId(0)
        cmsId = cmsTypeId(1).toInt
      }

      val city = IpUtils.getCity(ip)
      val time = splited(0)
      val day = time.substring(0, 10).replaceAll("-", "")

      Row(url, cmsType, cmsId, traffic, ip, city, time, day)
    } catch {
      case _ => {
        Row(null, null, null, null, null, null, null, null)
      }
    }


  }
}
