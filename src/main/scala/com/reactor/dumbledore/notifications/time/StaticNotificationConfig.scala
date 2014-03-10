package com.reactor.dumbledore.notifications.time

class StaticNotificationConfig(notifEndpoint:String, rank:Int) extends NotificationConfig(notifEndpoint, rank) {
  
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