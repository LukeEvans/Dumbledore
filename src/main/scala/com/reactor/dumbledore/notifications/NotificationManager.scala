package com.reactor.dumbledore.notifications

import scala.collection.mutable.Map
import scala.collection.mutable.ListBuffer
import com.github.nscala_time.time.Imports._
import com.reactor.dumbledore.notifications.time.NotificationConfig
import com.reactor.dumbledore.notifications.time.Time

class NotificationManager {
  val services = Map[String, NotificationConfig]()
  init()
  
  def init():Unit = {
    val fbBdays = new NotificationConfig("/social/facebook/birthdays").add247(None)  
    services put ("facebook_birthdays", fbBdays)
    
    val fbMessages = new NotificationConfig("/social/facebook/inbox").add247(None)
    services put ("facebook_messages", fbMessages)
    
    val fbNotifications = new NotificationConfig("/social/facebook/notifications").add247(None)
    services put ("facebook_notifications", fbNotifications)
    
    val nearbyPhotos = new NotificationConfig("/instagram/location").add247(None)
    services put ("nearby_photos", nearbyPhotos)
    
    val nearbyPlaces = new NotificationConfig("/yelp").add247(None)
    services put ("nearby_places", nearbyPlaces)
    
    val stocks = new NotificationConfig("/stocks").add247(None)
    services put ("stocks", stocks)
  }
  
  def getServices(ids:List[String], now:DateTime):ListBuffer[String] ={
    val nowTime = new Time(now)
    val validServices = ListBuffer[String]()
    
    services.map{
      service =>
        if(ids.contains(service._1)){
          // If time is within service range
          if(service._2.isValidTime(nowTime)){
            validServices += service._2.notifEndpoint
          }
        }
    }
    validServices
  }
}