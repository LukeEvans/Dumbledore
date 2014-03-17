package com.reactor.dumbledore.notifications.time

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.Map

import com.github.nscala_time.time.Imports._

case class NotificationConfig(serviceType:String, notifEndpoint:String, rank:Int, reloadMinutes:Long = 30) {
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
  
  def isDismissed(dismissalTime:Option[Long], date:DateTime):Boolean = {
    
    dismissalTime match{
      case Some(time) =>
        //val date = new java.util.Date
    	val now = date.getMillis
    	
    	println("now time: " + now/60000)
    	println("dismissal time: " + time/60)
    	
    	val difference = (now/60000) - (time/60) // minutes difference between dismissal and now
    	
    	println("Diff: " + difference)
    
    	if(difference < reloadMinutes) return true else return false     
      
      case None => false
    }
    

  }
}