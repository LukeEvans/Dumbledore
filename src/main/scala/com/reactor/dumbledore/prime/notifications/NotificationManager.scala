package com.reactor.dumbledore.prime.notifications

import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Map
import com.github.nscala_time.time.Imports._
import com.reactor.dumbledore.prime.notifications.request.Request
import com.reactor.dumbledore.prime.notifications.time.Date
import com.reactor.dumbledore.prime.notifications.time.NotificationConfig
import com.reactor.dumbledore.prime.notifications.time.StaticNotificationConfig
import com.reactor.dumbledore.prime.notifications.time.Time
import com.reactor.dumbledore.prime.services.WebRequestData
import com.reactor.dumbledore.prime.constants._


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
    
    requests.foreach{
     request => services.get(request.id) match{
         
         case Some(service) => // Service with request.id found

           service.getRangeAction(date) match{
             
             case Some(range) =>     
               
               if( !service.isDismissed(request.dismissTime, now)) // If card has not been dismissed within the regeneration time frame 
            	   validServices.put(request.id, WebRequestData(service.serviceType, service.notifEndpoint, range.params, request.cards))            	   
               else 
                 println("Card is dismissed")
                             
             case None => println("Not a valid time - " + request.id)
           }
           
         case None => println("Service not found")
       }
    }   
    return validServices
  }
  
  
  /** Get Developer Web Services from config
   */
  def getDevWebServices(now:DateTime):Map[String, WebRequestData] = {
    val date = new Date(now)
    val validServices = Map[String, WebRequestData]()
    
    devServices.foreach{
      service =>
        service._2.getRangeAction(date) match{
          case Some(range) => 
            validServices.put(service._1, WebRequestData(service._2.serviceType, service._2.notifEndpoint, range.params, ListBuffer[String]()))
          case None => println("Not a valid time")
        }
    }
    validServices
  }
  
  
  /** Initialize Services
   */
  def init():Unit = {

    val traffic = new NotificationConfig(Prime.DUMBLEDORE, "/").add247(None)
    
    services put (Prime.TRAFFIC, traffic)
    
    
    val weather = new NotificationConfig(Prime.V036, "/weather").add247(None)

    services put ("weather", weather)
    
    
    val nearbyPlaces = new NotificationConfig(Prime.DUMBLEDORE, "/")
    						.addRange(Time(6, 0), Time(10, 59), Day.MONDAY, Day.SUNDAY, Some(Map("type" -> "coffee")))
							.addRange(Time(11, 0), Time(13, 59), Day.MONDAY, Day.SUNDAY, Some(Map("type" -> "lunch")))
							.addRange(Time(14, 0), Time(16, 59), Day.MONDAY, Day.SUNDAY, Some(Map("type" -> "coffee")))
							.addRange(Time(17, 0), Time(19, 59), Day.MONDAY, Day.SUNDAY, Some(Map("type" -> "dinner")))
							.addRange(Time(20, 0), Time(23, 59), Day.MONDAY, Day.SUNDAY, Some(Map("type" -> "nightlife")))
							.add247(None)
    
	services put (Prime.NEARBY_PLACES, nearbyPlaces)
    
    
    val stocks = new StaticNotificationConfig(Prime.DUMBLEDORE, "/").add247(None)

    services put (Prime.STOCKS, stocks)
    
    
    val fbBdays = new NotificationConfig(Prime.V036, "/social/facebook/birthdays")
						.addRange(Time(20, 0), Time(23,59), Day.MONDAY, Day.SUNDAY, Some(Map("tomorrow" -> "true")))
						.add247(None)
						
    services put (Prime.FACEBOOK_BIRTHDAYS, fbBdays)
    
    
    val fbMessages = new NotificationConfig(Prime.V036, "/social/facebook/inbox").add247(None)
    
    services put (Prime.FACEBOOK_MESSAGES, fbMessages)
    
    
    val fbNotifications = new NotificationConfig(Prime.V036, "/social/facebook/notifications").add247(None)
    
    services put (Prime.FACEBOOK_NOTIFICATIONS, fbNotifications)
    
    
    val nearbyPhotos = new NotificationConfig(Prime.V036, "/instagram/location").add247(None)
    
    services put (Prime.NEARBY_PHOTOS, nearbyPhotos)
    
    
    val donations = new NotificationConfig(Prime.DUMBLEDORE, "/").add247(None)
    
    services put (Prime.DONATIONS, donations)
    
  }
  
  
  /** Initialize dev services
   */
  def devInit():Unit = {
    
    
    val traffic = new NotificationConfig(Prime.DUMBLEDORE, "/").add247(None)
    
    devServices put (Prime.TRAFFIC, traffic)
    
    
    val weather = new NotificationConfig(Prime.V036, "/weather").add247(None)
    
    devServices put (Prime.WEATHER, weather)

    
    val nearbyPlaces = new NotificationConfig(Prime.DUMBLEDORE, "/")
    						.addRange(Time(7, 0), Time(9, 59), Day.MONDAY, Day.SUNDAY, Some(Map("type" -> "coffee")))
							.addRange(Time(11, 0), Time(12, 59), Day.MONDAY, Day.SUNDAY, Some(Map("type" -> "lunch")))
							.addRange(Time(15, 0), Time(16, 59), Day.MONDAY, Day.SUNDAY, Some(Map("type" -> "coffee")))
							.addRange(Time(17, 0), Time(19, 59), Day.MONDAY, Day.SUNDAY, Some(Map("type" -> "dinner")))
							.add247(None)
    
    devServices put (Prime.NEARBY_PLACES, nearbyPlaces)
    
    
    val stocks = new StaticNotificationConfig(Prime.DUMBLEDORE, "/").add247(None)

    devServices put (Prime.STOCKS, stocks)
    
    
    val fbBdays = new NotificationConfig(Prime.V036, "/social/facebook/birthdays").add247(None) 
    
    devServices put (Prime.FACEBOOK_BIRTHDAYS, fbBdays)
    
    
    val fbMessages = new NotificationConfig(Prime.V036, "/social/facebook/inbox").add247(None)
    
    devServices put (Prime.FACEBOOK_MESSAGES, fbMessages)
    
    
    val fbNotifications = new NotificationConfig(Prime.V036, "/social/facebook/notifications").add247(None)
    
    devServices put (Prime.FACEBOOK_NOTIFICATIONS, fbNotifications)
    
    
    val nearbyPhotos = new NotificationConfig(Prime.V036, "/instagram/location").add247(None)
    
    devServices put (Prime.NEARBY_PHOTOS, nearbyPhotos)
  
  }
}