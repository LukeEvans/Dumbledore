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
      case Some(params) => reply(origin, ListSetContainer(WebService.request(request.service_id, request.requestData, Some(params))))
      case None => reply(origin, ListSetContainer(WebService.request(request.service_id, request.requestData, None)))
    }   
  }
  
  def handleDumbledoreRequest(request:ServiceRequest, origin:ActorRef){
    request.service_id match{
      case "traffic" => reply(origin, ListSetContainer(Traffic.getTraffic(request.requestData.rank)))
    }
  }
}