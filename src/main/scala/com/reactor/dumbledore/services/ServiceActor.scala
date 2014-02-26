package com.reactor.dumbledore.services

import com.reactor.patterns.pull.FlowControlArgs
import com.reactor.patterns.pull.FlowControlActor
import akka.actor.ActorRef
import com.reactor.dumbledore.utilities.Location
import com.reactor.dumbledore.messaging.SingleDataContainer
import scala.util.Random
import scala.collection.mutable.ListBuffer
import com.reactor.dumbledore.messaging.ServiceRequest

class ServiceActor(args:FlowControlArgs) extends FlowControlActor(args){

  ready()
  override def preStart() = println("Service Actor Starting")  
  
  def receive = {
    case service:ServiceRequest => 
      handleServiceRequest(service, sender)
      complete()
    case a:Any => 
      println("ServiceActor:Unknown message received - " + a)
      complete()
  }
  
  def handleServiceRequest(request:ServiceRequest, origin:ActorRef){   
    request.params match {
      case Some(params) => reply(origin, SingleDataContainer(Service.request(request.endpoint, Some(params), request.ids))) //origin ! SingleDataContainer(Service.getData(request.endpoint, params))
      case None => reply(origin, SingleDataContainer(Service.request(request.endpoint, None, request.ids)))
    }   
  }
}