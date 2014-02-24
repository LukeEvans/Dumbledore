package com.reactor.dumbledore.notifications.time

import com.github.nscala_time.time.Imports._

case class Time(hour:Int, minute:Int, day:Int){
  
  def this(now:DateTime) = this(now.getHourOfDay(), now.getMinuteOfHour(), now.getDayOfWeek())   
  
  def getTotalTime():Double = {
    val total = hour + (minute/60).toDouble
    total
  }
}