package com.reactor.dumbledore.notifications.time

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.Map
import com.github.nscala_time.time.Imports._

case class NotificationConfig(notifEndpoint:String, rank:Int) {
  val timeRanges:ArrayBuffer[TimeRange] = new ArrayBuffer[TimeRange]
  
  def addTimeRange(start:Date, stop:Date, params:Option[Map[String, String]]):NotificationConfig ={
    timeRanges += new TimeRange(start, stop, params) 
    this
  }
  
  def add247(params:Option[Map[String, String]]):NotificationConfig = {
    addRange(Time(0,0), Time(23,59), 0, 6, params)
    this
  }
  
  def addRange(startTime:Time, stopTime:Time, start:Int, 
      stop:Int, params:Option[Map[String, String]]):NotificationConfig ={
    
    for(i <- start to stop){
     addTimeRange(Date(startTime, i), Date(stopTime, i), params) 
    }
    this
  }
  
  def isValidTime(nowDate:Date):Boolean ={
    timeRanges.map{
      timeRange =>{
    	if(nowDate.isInRange(timeRange)){
    	  return true
    	}
      }
    }
    false
  }
  
  def getRangeAction(nowDate:Date):Option[TimeRange] = {
    timeRanges.map{
      timeRange =>{
        if(nowDate.isInRange(timeRange)){
          return Some(timeRange)
        }
      }
    }
    None
  }
}