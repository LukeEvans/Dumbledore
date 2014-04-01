package com.reactor.dumbledore.prime.entertainment

import com.reactor.patterns.pull.FlowControlActor
import com.reactor.patterns.pull.FlowControlArgs
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Map
import com.reactor.dumbledore.prime.notifications.request.Request
import akka.actor.ActorRef
import com.reactor.dumbledore.prime.data.ListSet
import com.reactor.dumbledore.messaging.EntertainmentRequestContainer
import com.reactor.dumbledore.prime.youtube.Youtube
import com.reactor.dumbledore.prime.services.comics.Comics
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.util.Failure
import scala.util.Success

class EntertainmentActor(args:FlowControlArgs) extends FlowControlActor(args) {
  
  val entertainManager = new EntertainmentManager
  
  ready()
  
  override def preStart() = println("EntertainmentActor started...")
  
  override def receive = {
    
    case EntertainmentRequestContainer(list, all) => getEntertainment(list, all, sender)
    
    case a:Any => println("Unknown message - " + a)
  }
  
  def getEntertainment(requests:ListBuffer[Request], allEntertainment:Boolean, origin:ActorRef){
    
    val entertainData =  ListBuffer[ListSet[Object]]()
    
    var services:Map[String, EntertainmentService] = null
    
    if(allEntertainment)
      services = entertainManager.getAllServices()
    else
      services = entertainManager.getServices(requests)
      
    val futureData = ListBuffer[Future[(String, Int, ListBuffer[Object])]]()
    
    
    services.foreach{
      service => futureData += future{service._2.process}
    }
    
    Future.sequence(futureData) onComplete{
      
      case Success(dataList) => 
        dataList.foreach(data => entertainData += ListSet(data._1, data._2, data._3))
        reply(origin, entertainData)
        
      case Failure(e) =>
        e.printStackTrace()
        reply(origin, entertainData)
    }
  }

  
  
  
}