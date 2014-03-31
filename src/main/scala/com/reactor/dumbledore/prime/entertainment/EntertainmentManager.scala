package com.reactor.dumbledore.prime.entertainment

import scala.collection.mutable.Map
import scala.collection.mutable.ListBuffer
import com.reactor.dumbledore.prime.youtube.Youtube
import com.reactor.dumbledore.prime.services.comics.Comics
import com.reactor.dumbledore.notifications.request.Request
import com.reactor.dumbledore.prime.constants.Prime
import com.reactor.dumbledore.prime.itunes.Itunes

/** EntertainmentService wrapper class
 */
abstract class EntertainmentService(id:String){
  
  // Abstract method process should return a tuple containing an ID, a rank, and a ListBuffer[Object] of data
  def process:(String, Int, ListBuffer[Object])
  
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
    
    return services
  }
  
}


/** Youtube Service
 */
case class YoutubeService() extends EntertainmentService(Prime.POPULAR_VIDEOS){
  
  /** get 8 most popular videos from Youtube */
  def process ={
    (getId, 50, Youtube.getYoutube(None, 8))
  }
}


/** Comic Service 
 */
case class ComicService() extends EntertainmentService(Prime.COMICS){
  
  /** get Random comics from comic api */
  def process = {
    (getId, 50, Comics.getRandomToday)
  }
}

/** Itunes Service
 */
case class RentalService() extends EntertainmentService(Prime.TOP_RENTALS){
  
  /** get top itunes rentals */
  def process = {
    (getId, 0, Itunes.getTopMovies(5))
  }
}