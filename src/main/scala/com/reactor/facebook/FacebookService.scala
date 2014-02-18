package com.reactor.facebook

import scala.collection.JavaConversions._
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Success
import com.reactor.facebook.messages.FBRequestContainer
import com.reactor.facebook.messages.FacebookRequest
import com.reactor.facebook.messages.ListContainer
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.util.Timeout
import akka.pattern.ask
import com.reactor.facebook.messages.FetchContainer
import scala.util.Failure

class FacebookService(fetcherActor:ActorRef) extends Actor with ActorLogging {

  
   override def receive ={
     case request:FBRequestContainer => process(request.fbRequest, sender)
     case a:Any => println("Unknown: "+ a.toString + " received")
   }
  
  
  def process(request:FacebookRequest, origin:ActorRef){
    implicit val timeout = Timeout(5 seconds)

    val fetched =  (fetcherActor ? FetchContainer(request.facebook_token, request.timezone_offset)).mapTo[ListContainer]
    
    fetched onComplete{
      case Success(result) =>
      	origin ! result
      case Failure(result) =>
        origin ! ListContainer(null)
    }
  }
}