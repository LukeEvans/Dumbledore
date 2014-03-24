package com.reactor.dumbledore.notifications.time

class StaticNotificationConfig(serviceType:String, notifEndpoint:String, rank:Int) extends NotificationConfig(serviceType, notifEndpoint, rank) {
  
  override def isValidTime(nowDate:Date):Boolean = {
    
    timeRanges.foreach{
      timeRange =>{
        if(nowDate.isInOffsetRange(timeRange)){
          return true
        }
      }
    }
    return false
  }
}