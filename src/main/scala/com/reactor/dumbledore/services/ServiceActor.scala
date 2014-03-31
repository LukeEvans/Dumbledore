package com.reactor.dumbledore.services

import com.reactor.patterns.pull.FlowControlArgs
import com.reactor.patterns.pull.FlowControlActor
import akka.actor.ActorRef
import com.reactor.dumbledore.utilities.Location
import scala.util.Random
import scala.collection.mutable.ListBuffer
import com.reactor.dumbledore.messaging.ServiceRequest
import com.reactor.dumbledore.messaging.ListSetContainer
import com.reactor.dumbledore.services.traffic.Traffic
import com.reactor.dumbledore.prime.constants.Prime
import com.reactor.dumbledore.prime.services.yelp.Yelp
import com.reactor.dumbledore.data.ListSet

class ServiceActor(args:FlowControlArgs) extends FlowControlActor(args){

  ready()
  override def preStart() = println("Service Actor Starting")  
  
  def receive = {
    case request:ServiceRequest => request.requestData.requestType match{
      case "v036" =>       
        handleSpringRequest(request, sender)
        complete()
        
      case "dumbledore" =>
        handleDumbledoreRequest(request, sender)
        complete()
    }
    case a:Any => 
      println("ServiceActor:Unknown message received - " + a)
      complete()
  }
  
  def handleSpringRequest(request:ServiceRequest, origin:ActorRef){   
    request.params match {
      case Some(params) => 
        reply(origin, ListSetContainer(WebService.request(request.service_id, request.requestData, Some(params))))

      case None => 
        reply(origin, ListSetContainer(WebService.request(request.service_id, request.requestData, None)))

    }   
  }
  
  def handleDumbledoreRequest(request:ServiceRequest, origin:ActorRef){
    
    request.service_id match{
      case Prime.TRAFFIC => 
        reply(origin, ListSetContainer(Traffic.getTraffic(request.requestData.rank)))
        
      case "nearby_places" => 
        request.params match{
          case Some(param) =>
            if(param.get("type").isDefined && param.get("lat").isDefined && param.get("long").isDefined){

              val set = Yelp.searchNearby(param.getOrElse("type", "food"), 
                  param.getOrElse("lat", "40.0176").toDouble, 
                  param.getOrElse("long", "-105.2979").toDouble,
                  3)
                  
              reply(origin, ListSetContainer(ListSet(Prime.NEARBY_PLACES, 0, set)))
            }
            
          case None => reply(origin, null)
        }
    }
  }
}