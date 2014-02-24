package com.reactor.dumbledore.notifications.time

import com.github.nscala_time.time.Imports._
import scala.collection.mutable.Map

class TimeRange {
  private var start:Time = null
  private var stop:Time = null
  private var params:Option[Map[String, String]] = null
  
  def this(start:Time, stop:Time, params:Option[Map[String, String]]){
    this()
    this.start = start
    this.stop = stop
    this.params = params
  }
  
  def inRange(time:Time):Boolean = {
    if(time.day == start.day){
      if(time.getTotalTime >= start.getTotalTime
          && time.getTotalTime <= stop.getTotalTime){
        return true
      }
    }
    false
  }
}