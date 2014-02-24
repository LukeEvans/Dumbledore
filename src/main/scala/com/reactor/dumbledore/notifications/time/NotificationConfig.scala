package com.reactor.dumbledore.notifications.time

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.Map
import com.github.nscala_time.time.Imports._

case class NotificationConfig(notifEndpoint:String) {
  val times:ArrayBuffer[TimeRange] = new ArrayBuffer[TimeRange]
  
  def addTimeRange(start:Time, stop:Time, params:Option[Map[String, String]]):NotificationConfig ={
    times += new TimeRange(start, stop, params) 
    this
  }
  
  def add247(params:Option[Map[String, String]]):NotificationConfig = {
    addTimeRange(Time(0,0,0), Time(23,59,0), params)
    addTimeRange(Time(0,0,1), Time(23,59,1), params)
    addTimeRange(Time(0,0,2), Time(23,59,2), params)
    addTimeRange(Time(0,0,3), Time(23,59,3), params)
    addTimeRange(Time(0,0,4), Time(23,59,4), params)
    addTimeRange(Time(0,0,5), Time(23,59,5), params)
    addTimeRange(Time(0,0,6), Time(23,59,6), params)
    this
  }
  
  def isValidTime(nowTime:Time):Boolean ={
    times.map{
      time =>{
    	if(time.inRange(nowTime)){
    	  return true
    	}
      }
    }
    false
  }
}