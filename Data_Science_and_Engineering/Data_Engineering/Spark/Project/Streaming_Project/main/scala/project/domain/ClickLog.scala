package project.domain

/**
  *
  * @param ip
  * @param time
  * @param courseId
  * @param statusCode 日志访问的状态码
  * @param ref
  */
case class ClickLog (ip:String, time:String, courseId:Int, statusCode:Int ,ref:String)
