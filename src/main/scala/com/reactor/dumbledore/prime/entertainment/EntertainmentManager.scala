package com.reactor.dumbledore.prime.entertainment

import scala.collection.mutable.Map
import scala.collection.mutable.ListBuffer
import com.reactor.dumbledore.prime.youtube.Youtube
import com.reactor.dumbledore.prime.services.comics.Comics
import com.reactor.dumbledore.notifications.request.Request

abstract class EntertainmentService(id:String){
  def process:(String, Int, ListBuffer[Object])
  
  def getId:String = id
}

class EntertainmentManager {
  val entServices = Map[String, EntertainmentService]()
  init()
  
  def getServices(list:ListBuffer[Request]):Map[String, EntertainmentService] = {
    val services = Map[String, EntertainmentService]()
    
    if(list == null)
      return services
    
    list.map{ request => 
      entServices.get(request.id) match{
        case Some(service) => services.put(request.id, service)
        case None => // Don't add
      }
    }
    
    return services
  }
  
  private def init(){
    
    entServices.put("youtube", YoutubeService())
    
    entServices.put("comics", ComicService())
  }
}

case class YoutubeService() extends EntertainmentService("youtube"){
  def process ={
    (getId, 50, Youtube.getYoutube(None, 8))
  }
}

case class ComicService() extends EntertainmentService("comics"){
  def process = {
    (getId, 50, Comics.getRandomToday)
  }
}