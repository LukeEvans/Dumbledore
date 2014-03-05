package com.reactor.dumbledore.services

import com.reactor.patterns.pull.FlowControlArgs
import com.reactor.patterns.pull.FlowControlActor
import akka.actor.ActorRef
import com.reactor.dumbledore.utilities.Location
import com.reactor.dumbledore.messaging.SingleDataContainer
import scala.util.Random
import scala.collection.mutable.ListBuffer
import com.reactor.dumbledore.messaging.ServiceRequest
import com.reactor.dumbledore.messaging.ArraySetContainer

class ServiceActor(args:FlowControlArgs) extends FlowControlActor(args){

  ready()
  override def preStart() = println("Service Actor Starting")  
  
  def receive = {
    case request:ServiceRequest => 
      handleServiceRequest(request, sender)
      complete()
    case a:Any => 
      println("ServiceActor:Unknown message received - " + a)
      complete()
  }
  
  def handleServiceRequest(request:ServiceRequest, origin:ActorRef){   
    request.params match {
      case Some(params) => reply(origin, ArraySetContainer(Service.request(request.service_id, request.endpoint, Some(params), request.ids)))
      case None => reply(origin, ArraySetContainer(Service.request(request.service_id, request.endpoint, None, request.ids)))
    }   
  }
}