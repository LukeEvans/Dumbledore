package com.reactor.dumbledore.notifications.time

import com.github.nscala_time.time.Imports._
import scala.collection.mutable.Map

case class TimeRange(start:Date, stop:Date, params:Option[Map[String, String]]) { 
  
  def inRange(time:Date):Boolean = {
    if(time.day == start.day){
      if(time.time.getTotalTime >= start.time.getTotalTime
          && time.time.getTotalTime <= stop.time.getTotalTime){
        return true
      }
    }
    false
  }
}