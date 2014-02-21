package com.reactor.dumbledore.services

import com.reactor.patterns.pull.FlowControlArgs
import com.reactor.patterns.pull.FlowControlActor
import com.reactor.dumbledore.messaging.ServiceRequest
import akka.actor.ActorRef
import com.reactor.dumbledore.utilities.Location
import com.reactor.dumbledore.messaging.SingleDataContainer

class ServiceActor(args:FlowControlArgs) extends FlowControlActor(args){

  ready
  
  def receive = {
    case service:ServiceRequest => 
      println("received service request")
      handleServiceRequest(service, sender)
      complete
    case a:Any => println("ServiceActor:Unknown message received - " + a)
  }
  
  def handleServiceRequest(request:ServiceRequest, origin:ActorRef){
    
    request.params match {
      case Some(params) => origin ! SingleDataContainer(Service.getData(request.endpoint, params))
      case None => origin ! SingleDataContainer(Service.getData(request.endpoint))
    }   
  }
}