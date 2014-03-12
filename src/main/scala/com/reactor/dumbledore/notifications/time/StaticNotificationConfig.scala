package com.reactor.dumbledore.notifications.time

class StaticNotificationConfig(serviceType:String, notifEndpoint:String, rank:Int) extends NotificationConfig(serviceType, notifEndpoint, rank) {
  
  override def isValidTime(nowDate:Date):Boolean = {
    timeRanges.map{
      timeRange =>{
        if(nowDate.isInOffsetRange(timeRange)){
          return true
        }
      }
    }
    false
  }
}