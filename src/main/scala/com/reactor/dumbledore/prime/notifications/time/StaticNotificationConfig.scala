package com.reactor.dumbledore.prime.notifications.time


class StaticNotificationConfig(serviceType:String, notifEndpoint:String) extends NotificationConfig(serviceType, notifEndpoint) {

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