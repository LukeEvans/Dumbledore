package com.reactor.dumbledore.notifications.time

import com.github.nscala_time.time.Imports._

class TimeRange {
  private var startTime:DateTime = null
  private var stopTime:DateTime = null
  private var params:Map[String, String] = null
  
  def this(start:DateTime, stop:DateTime, params:Map[String, String]){
    this()
    this.startTime = start
    this.stopTime = stop
    this.params = params
  }
  
  def inRange(time:DateTime):Boolean = {
    if(time.getDayOfWeek() == startTime.getDayOfWeek()){
      if(startTime.getMinuteOfDay() < time.getMinuteOfDay()
          && time.getMinuteOfDay() < stopTime.getMinuteOfDay()){
        return true
      }
    }
    false
  }
}