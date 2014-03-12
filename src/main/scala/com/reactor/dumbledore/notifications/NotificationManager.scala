package com.reactor.dumbledore.notifications

import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Map

import com.github.nscala_time.time.Imports._
import com.reactor.dumbledore.notifications.request.Request
import com.reactor.dumbledore.notifications.time.Date
import com.reactor.dumbledore.notifications.time.NotificationConfig
import com.reactor.dumbledore.notifications.time.StaticNotificationConfig
import com.reactor.dumbledore.notifications.time.Time
import com.reactor.dumbledore.services.WebRequestData

/**  Handle web service configurations and give notification actor a list of 
 *   Service Data to send WebServiceActor
 */
class NotificationManager {
  val services = Map[String, NotificationConfig]()
  val devServices = Map[String, NotificationConfig]()
  init() // Initialize Web Services
  devInit() // Initialize Dev Web Services
  
  /** Get WebService Map from config
   */
  def getWebServices(requests:ListBuffer[Request], now:DateTime):Map[String, WebRequestData] = {
    val date = new Date(now)
    val validServices = Map[String, WebRequestData]()
    
    requests.map{
     request =>
       services.get(request.id) match{
         case Some(service) =>
           service.getRangeAction(date) match{
             case Some(range) =>
               validServices.put(request.id, WebRequestData(service.serviceType, service.notifEndpoint, service.rank, range.params, request.cards))
             case None => println("Not a valid time - " + request.id)
           }
         case None => println("Service not found")
       }
    }   
    validServices
  }
  
  
  /** Get Developer Web Services from config
   */
  def getDevWebServices(now:DateTime):Map[String, WebRequestData] = {
    val date = new Date(now)
    val validServices = Map[String, WebRequestData]()
    
    devServices.map{
      service =>
        service._2.getRangeAction(date) match{
          case Some(range) => 
            validServices.put(service._1, WebRequestData(service._2.serviceType, service._2.notifEndpoint, service._2.rank, range.params, ListBuffer[String]()))
          case None => println("Not a valid time")
        }
    }
    validServices
  }
  
  def init():Unit = {

    val traffic = new NotificationConfig("dumbledore", "/", 1)
    					.addRange(Time(5,0), Time(9, 59), 1, 5, None)
    					.addRange(Time(3,0), Time(5,59), 1, 5, None)
    services put ("traffic", traffic)
    
    val weather = new NotificationConfig("v036", "/weather", 2)
    					.addRange(Time(5, 0), Time(13, 59), 0, 6, None)
    services put ("weather", weather)
    
    // Topic Alerts
    
	val nearbyPlaces = new NotificationConfig("v036", "/yelp", 3)
							.addRange(Time(7, 0), Time(9, 59), 0, 6, Some(Map("type" -> "coffee")))
							.addRange(Time(11, 0), Time(12, 59), 0, 6, Some(Map("type" -> "lunch")))
							.addRange(Time(15, 0), Time(16, 59), 0, 6, Some(Map("type" -> "coffee")))
							.addRange(Time(17, 0), Time(19, 59), 0, 6, Some(Map("type" -> "dinner")))
    services put ("nearby_places", nearbyPlaces)
    
    val stocks = new StaticNotificationConfig("v036", "/stocks", 4)
						.addRange(Time(9, 30), Time(16, 30), 1, 5, None)
    services put ("stocks", stocks)
    
    val fbBdays = new NotificationConfig("v036", "/social/facebook/birthdays", 5)
						.addRange(Time(5, 0), Time(10,59), 0, 6, None)
						.addRange(Time(20, 0), Time(23,59), 0, 6, Some(Map("tomorrow" -> "true")))
    services put ("facebook_birthdays", fbBdays)
    
    val fbMessages = new NotificationConfig("v036", "/social/facebook/inbox", 6).add247(None)
    services put ("facebook_messages", fbMessages)
    
    val fbNotifications = new NotificationConfig("v036", "/social/facebook/notifications", 7).add247(None)
    services put ("facebook_notifications", fbNotifications)
    
    val nearbyPhotos = new NotificationConfig("v036", "/instagram/location", 8).add247(None)
    services put ("nearby_photos", nearbyPhotos)
  }
  
  def devInit():Unit = {
    
    val traffic = new NotificationConfig("dumbledore", "/", 1).add247(None)
    devServices put ("traffic", traffic)
    
    val weather = new NotificationConfig("v036", "/weather", 2).add247(None)
    devServices put ("weather", weather)
    
    // Topic Alerts
    
    val nearbyPlaces = new NotificationConfig("v036", "/yelp", 3).add247(None)
    devServices put ("nearby_places", nearbyPlaces)
    
    val stocks = new StaticNotificationConfig("v036", "/stocks", 4).add247(None)
    devServices put ("stocks", stocks)
    
    val fbBdays = new NotificationConfig("v036", "/social/facebook/birthdays", 5).add247(None)  
    devServices put ("facebook_birthdays", fbBdays)
    
    val fbMessages = new NotificationConfig("v036", "/social/facebook/inbox", 6).add247(None)
    devServices put ("facebook_messages", fbMessages)
    
    val fbNotifications = new NotificationConfig("v036", "/social/facebook/notifications", 7).add247(None)
    devServices put ("facebook_notifications", fbNotifications)
    
    val nearbyPhotos = new NotificationConfig("v036", "/instagram/location", 8).add247(None)
    devServices put ("nearby_photos", nearbyPhotos)
  }
}