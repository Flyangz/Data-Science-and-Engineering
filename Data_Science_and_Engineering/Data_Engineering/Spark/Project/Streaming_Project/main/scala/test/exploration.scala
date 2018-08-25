package test

import project.domain.ClickLog
import project.utils.DateUtils

object exploration {
  def main(args: Array[String]): Unit = {
//    val cleanData = kafkaStream.map(_._2)
//      .map(line => {
//        val info = line.split("\t")

        //info(2) = "GET /class/130.html HTTP/1.1"
        // url = /class/130.html
        val url = "GET /class/130.html HTTP/1.1".split(" ")(1)
    println(url)
        var courseId = 0

        if (url.startsWith("/class")) {
          val courseIdHTML = url.split("/")(2)
          println(courseIdHTML.lastIndexOf("."))
          courseId = courseIdHTML.substring(0, courseIdHTML.lastIndexOf(".")).toInt

        }
    println(courseId)
//    println(courseIdHTML.substring(0, 3))

//        val a = ClickLog(info(0), DateUtils.parseToMinute(info(1)), courseId, info(3).toInt, info(4))
//      a.filter(clicklog => clicklog.courseId != 0)
  }

}
