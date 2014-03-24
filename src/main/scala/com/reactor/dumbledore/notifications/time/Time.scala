package com.reactor.dumbledore.notifications.time

import com.github.nscala_time.time.Imports._
import com.reactor.dumbledore.prime.constants.Day


/** Time Class
 */
case class Time(hour:Int, minute:Int) {
  
  /** Get double representation of time 6:30 = 6.5 */
  def getTotalTime():Double = (hour + (minute/60).toDouble)
  
}


/** Date Class
 */
case class Date(time:Time, day:Int){
  
  require(day >= Day.MONDAY && day <= Day.SUNDAY)
  
  var utcTime = new DateTime(DateTimeZone.forOffsetHours(0))
  var offTime:Time = Time(utcTime.getHourOfDay(), utcTime.getMinuteOfHour())
  var offDay:Int = utcTime.getDayOfWeek()
  
  /** DateTime Constructor */
  def this(now:DateTime) = this(Time(now.getHourOfDay(), now.getMinuteOfHour()), now.getDayOfWeek())
  
  
  /** DateTime with offset Constructor */
  def this(now:DateTime, offset:Int){
    
    this(Time(now.getHourOfDay(), now.getMinuteOfHour()), now.getDayOfWeek())
    
    utcTime =  new DateTime(DateTimeZone.forOffsetHours(offset))
    offTime = Time(utcTime.getHourOfDay(), utcTime.getMinuteOfHour())
    offDay = utcTime.getDayOfWeek()
  }
  
  
  /** Check if this Date falls within the range:TimeRange   */
  def isInRange(range:TimeRange):Boolean = {
    
    if(day == range.start.day){
      
      val total = time.getTotalTime
      
      if(total >= range.start.time.getTotalTime
          && total <= range.stop.time.getTotalTime)
        return true
      
    }
    false
  }
  
  
  /** Check if this offset Date falls within the range:TimeRange */
  def isInOffsetRange(range:TimeRange):Boolean = {
    
    if(offDay == range.start.day){
      
      val total = offTime.getTotalTime
      
      if(total >= range.start.time.getTotalTime
          && total <= range.stop.time.getTotalTime)
        return true
        
    }
    false    
  }
}