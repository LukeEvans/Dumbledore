package com.reactor.dumbledore.notifications

import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Map

import com.github.nscala_time.time.Imports._
import com.reactor.dumbledore.notifications.request.Request
import com.reactor.dumbledore.notifications.time.Date
import com.reactor.dumbledore.notifications.time.NotificationConfig
import com.reactor.dumbledore.notifications.time.StaticNotificationConfig
import com.reactor.dumbledore.notifications.time.Time
import com.reactor.dumbledore.services.ServiceData

class NotificationManager {
  val services = Map[String, NotificationConfig]()
  val devServices = Map[String, NotificationConfig]()
  init()
  devInit()
  
  def init():Unit = {
    
    // Traffic Service
    
    val weather = new NotificationConfig("/weather")
    					.addRange(Time(5, 0), Time(13, 59), 0, 6, None)
    services put ("weather", weather)
    
    // Topic Alerts
    
	val nearbyPlaces = new NotificationConfig("/yelp")
							.addRange(Time(7, 0), Time(9, 59), 0, 6, Some(Map("type" -> "coffee")))
							.addRange(Time(11, 0), Time(12, 59), 0, 6, Some(Map("type" -> "lunch")))
							.addRange(Time(15, 0), Time(16, 59), 0, 6, Some(Map("type" -> "coffee")))
							.addRange(Time(17, 0), Time(19, 59), 0, 6, Some(Map("type" -> "dinner")))
    services put ("nearby_places", nearbyPlaces)
    
    val stocks = new StaticNotificationConfig("/stocks")
						.addRange(Time(9, 30), Time(16, 30), 1, 5, None)
    services put ("stocks", stocks)
    
    val fbBdays = new NotificationConfig("/social/facebook/birthdays")
						.addRange(Time(5, 0), Time(10,59), 0, 6, None)
						.addRange(Time(20, 0), Time(23,59), 0, 6, Some(Map("tomorrow" -> "true")))
    services put ("facebook_birthdays", fbBdays)
    
    val fbMessages = new NotificationConfig("/social/facebook/inbox").add247(None)
    services put ("facebook_messages", fbMessages)
    
    val fbNotifications = new NotificationConfig("/social/facebook/notifications").add247(None)
    services put ("facebook_notifications", fbNotifications)
    
    val nearbyPhotos = new NotificationConfig("/instagram/location").add247(None)
    services put ("nearby_photos", nearbyPhotos)
  }
  
  def devInit():Unit = {
    
    // Traffic Service
    
    // Weather Service
    val weather = new NotificationConfig("/weather").add247(None)
    devServices put ("weather", weather)
    
    // Topic Alerts
    
    val nearbyPlaces = new NotificationConfig("/yelp").add247(None)
    devServices put ("nearby_places", nearbyPlaces)
    
    val stocks = new StaticNotificationConfig("/stocks").add247(None)
    devServices put ("stocks", stocks)
    
    val fbBdays = new NotificationConfig("/social/facebook/birthdays").add247(None)  
    devServices put ("facebook_birthdays", fbBdays)
    
    val fbMessages = new NotificationConfig("/social/facebook/inbox").add247(None)
    devServices put ("facebook_messages", fbMessages)
    
    val fbNotifications = new NotificationConfig("/social/facebook/notifications").add247(None)
    devServices put ("facebook_notifications", fbNotifications)
    
    val nearbyPhotos = new NotificationConfig("/instagram/location").add247(None)
    devServices put ("nearby_photos", nearbyPhotos)
  }
  
  def getServices(requests:ListBuffer[Request], now:DateTime):Map[String, ServiceData] = {
    val date = new Date(now)
    val validServices = Map[String, ServiceData]()
    
    requests.map{
     request =>
       services.get(request.id) match{
         case Some(service) =>
           service.getRangeAction(date) match{
             case Some(range) =>
               validServices.put(request.id, ServiceData(service.notifEndpoint, range.params, request.cards))
             case None => println("Not a valid time - " + request.id)
           }
         case None => println("Service not found")
       }
    }   
    validServices
  }
  
  def getDevServices(now:DateTime):Map[String, ServiceData] = {
    val date = new Date(now)
    val validServices = Map[String, ServiceData]()
    
    devServices.map{
      service =>
        service._2.getRangeAction(date) match{
          case Some(range) => 
            validServices.put(service._1, ServiceData(service._2.notifEndpoint, range.params, ListBuffer[String]()))
          case None => println("Not a valid time")
        }
    }
    validServices
  }
}