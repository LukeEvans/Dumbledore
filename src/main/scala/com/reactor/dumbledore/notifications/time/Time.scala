package com.reactor.dumbledore.notifications.time

import com.github.nscala_time.time.Imports._

case class Time(hour:Int, minute:Int) {
  
  def getTotalTime():Double = (hour + (minute/60).toDouble)
}

case class Date(time:Time, day:Int){
  require(day >= 1 && day < 8)
  var utcTime = new DateTime(DateTimeZone.forOffsetHours(0))
  var offTime:Time = Time(utcTime.getHourOfDay(), utcTime.getMinuteOfHour())
  var offDay:Int = utcTime.getDayOfWeek()
  
  def this(now:DateTime) = this(Time(now.getHourOfDay(), now.getMinuteOfHour()), now.getDayOfWeek())
  
  def this(now:DateTime, offset:Int){
    this(Time(now.getHourOfDay(), now.getMinuteOfHour()), now.getDayOfWeek())
    utcTime =  new DateTime(DateTimeZone.forOffsetHours(offset))
    offTime = Time(utcTime.getHourOfDay(), utcTime.getMinuteOfHour())
    offDay = utcTime.getDayOfWeek()
  }
  
  def isInRange(range:TimeRange):Boolean = {
    if(day == range.start.day){
      val total = time.getTotalTime
      if(total >= range.start.time.getTotalTime
          && total <= range.stop.time.getTotalTime){
        return true
      }
    }
    false
  }
  
  def isInOffsetRange(range:TimeRange):Boolean = {
    if(offDay == range.start.day){
      val total = offTime.getTotalTime
      if(total >= range.start.time.getTotalTime
          && total <= range.stop.time.getTotalTime){
        return true
      }
    }
    false    
  }
}