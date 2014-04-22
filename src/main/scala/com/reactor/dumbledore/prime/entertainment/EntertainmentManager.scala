package com.reactor.dumbledore.prime.entertainment

import scala.collection.mutable.Map
import scala.collection.mutable.ListBuffer
import com.reactor.dumbledore.prime.services.youtube.Youtube
import com.reactor.dumbledore.prime.services.comics.Comics
import com.reactor.dumbledore.prime.notifications.request.Request
import com.reactor.dumbledore.prime.constants.Prime
import com.reactor.dumbledore.prime.services.itunes.Itunes
import com.reactor.dumbledore.prime.services.events.Events
import com.reactor.dumbledore.prime.services.quote.QuoteOfTheDay
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent._
import scala.concurrent.duration._

/** EntertainmentService wrapper class
 */
abstract class EntertainmentService(id:String){
  
  // Abstract method process should return a tuple containing an ID, a rank, and a ListBuffer[Object] of data
  def process:(String, Int, ListBuffer[Object])
  
  def processWithTimeout(proc:() => (String, Int, ListBuffer[Object]), timeOut: Int){
	try{
      
      val futureData = future{proc.apply}
      
      (getId, 50, Await.result(futureData, atMost = timeOut seconds))
      
    } catch{
      case e:Exception =>
        e.printStackTrace()
        (getId, 50, ListBuffer[Object]())
    }
  }
  
  def getId:String = id
}


/** Entertainment Service Manager
 */
class EntertainmentManager {
  
  val entServices = initServices() // Entertainment Services
  
  
  /** Get all entertainment services */
  def getAllServices():Map[String, EntertainmentService] = entServices
  
  
  /** Get all services in list of requests (ListBuffer[Request) */
  def getServices(list:ListBuffer[Request]):Map[String, EntertainmentService] = {
    
    val services = Map[String, EntertainmentService]()
    
    if(list == null || list.isEmpty)
      return services
    
    list.foreach{ request => 
      entServices.get(request.id) match{
        
        case Some(service) => services.put(request.id, service)
        
        case None => println("Service: " +request.id + " not found")
      }
    }
    
    return services
  }
  
  
  /** Initialize entertainment services */
  private def initServices():Map[String, EntertainmentService] = {
    
    val services = Map[String, EntertainmentService]()
    
    services.put(Prime.POPULAR_VIDEOS, YoutubeService())
    services.put(Prime.COMICS, ComicService())
    services.put(Prime.TOP_RENTALS, RentalService())
    services.put(Prime.TOP_SONGS, MusicService())
    services.put(Prime.QUOTE, QuoteService())
    
    return services
  }
  
}


/** Youtube Service
 */
case class YoutubeService() extends EntertainmentService(Prime.POPULAR_VIDEOS){
  
  /** get 8 most popular videos from Youtube */
  def process ={
	    try{
	      
	      val futureData = future{Youtube.getYoutube(None, 8)}
	      
	      (getId, 50, Await.result(futureData, atMost = 2 seconds))
	      
	    } catch{
	      case e:Exception =>
	        e.printStackTrace()
	        (getId, 50, ListBuffer[Object]())
	    }
  }
}


/** Comic Service 
 */
case class ComicService() extends EntertainmentService(Prime.COMICS){
  
  /** get Random comics from comic api */
  def process = {
    try{
      
      val futureData = future{Comics.getRandomToday}
      
      (getId, 50, Await.result(futureData, atMost = 2 seconds))
      
    } catch{
      case e:Exception =>
        e.printStackTrace()
        (getId, 50, ListBuffer[Object]())
    }
  }
}

/** Itunes Service
 */
case class MusicService() extends EntertainmentService(Prime.TOP_SONGS){
  
  /** get top itunes rentals */
  def process = {
	try{
      
      val futureData = future{Itunes.getTopSongs(5)}
      
      (getId, 50, Await.result(futureData, atMost = 2 seconds))
      
    } catch{
      case e:Exception =>
        e.printStackTrace()
        (getId, 50, ListBuffer[Object]())
    }
  }
}

/** Itunes Service
 */
case class RentalService() extends EntertainmentService(Prime.TOP_RENTALS){
  
  /** get top itunes rentals */
  def process = {
	try{
      
      val futureData = future{Itunes.getTopMovies(5)}
      
      (getId, 50, Await.result(futureData, atMost = 2 seconds))
      
    } catch{
      case e:Exception =>
        e.printStackTrace()
        (getId, 50, ListBuffer[Object]())
    }
  }
}

/** Events Service
 */
case class EventService() extends EntertainmentService(Prime.EVENTS){
  
  /** get top events */
  def process = {
	try{
      
      val futureData = future{Events.getEvents(5)}
      
      (getId, 50, Await.result(futureData, atMost = 2 seconds))
      
    } catch{
      case e:Exception =>
        e.printStackTrace()
        (getId, 50, ListBuffer[Object]())
    }
  }
}

/** Quote Service
 */
case class QuoteService() extends EntertainmentService(Prime.QUOTE){
  
  /** get quote of day */
  def process = {
	try{
      
      val futureData = future{QuoteOfTheDay.getToday}
      
      (getId, 50, Await.result(futureData, atMost = 2 seconds))
      
    } catch{
      case e:Exception =>
        e.printStackTrace()
        (getId, 50, ListBuffer[Object]())
    }
  }
  
}
