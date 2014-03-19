package com.reactor.dumbledore.prime

import com.reactor.patterns.pull.FlowControlActor
import com.reactor.patterns.pull.FlowControlArgs
import com.reactor.dumbledore.prime.youtube.Youtube
import com.reactor.dumbledore.messaging.requests.YoutubeRequest
import com.reactor.dumbledore.messaging.requests.PrimeRequest
import akka.actor.ActorRef
import scala.collection.mutable.ListBuffer
import scala.concurrent.Await
import com.reactor.dumbledore.data.ListSet
import scala.concurrent.Future
import com.fasterxml.jackson.databind.JsonNode
import akka.pattern.ask
import akka.util.Timeout
import com.reactor.dumbledore.messaging.FeedData
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import com.reactor.dumbledore.messaging.NotificationRequestContainer
import com.reactor.dumbledore.messaging.EntertainmentRequestContainer
import com.reactor.dumbledore.messaging.requests.NotificationRequest
import scala.util.Failure
import scala.util.Success

case class PrimeActorArgs(channelActor:ActorRef, notificationActor:ActorRef, entertainmentActor:ActorRef) extends FlowControlArgs{  
  override def workerArgs(): FlowControlArgs ={
    val newArgs = new PrimeActorArgs(channelActor, notificationActor, entertainmentActor)
    newArgs.addMaster(master)
    return newArgs
  }
}

class PrimeActor(args:PrimeActorArgs) extends FlowControlActor(args) {
  
  val channelActor = args.channelActor
  val notificationActor = args.notificationActor
  val entertainmentActor = args.entertainmentActor
  ready()
  
  override def preStart = println("Prime Actor Starting...")
  override def postStop = println("Prime Actor Terminated...")

  override def receive = {
    case request:PrimeRequest =>
      primeTime(request, sender)
      
    case request:YoutubeRequest => 
      reply(sender, Youtube.getYoutube(request.channelID, request.number))
      complete()
    case a:Any => println("Unknown Message - " + a)
  }
  
  
  def primeTime(request:PrimeRequest, origin:ActorRef){
    
    implicit val timeout = Timeout(30 seconds)
    
    val primeSet = new PrimeSet
    
    val futureSets = ListBuffer[Future[ListBuffer[ListSet[Object]]]]()
    
    if(request.feedRequests != null && !request.feedRequests.isEmpty){
      futureSets += (channelActor ? FeedData(request.feedRequests)).mapTo[ListBuffer[ListSet[Object]]]
    }
    
    if(request.notificationsRequests != null && !request.notificationsRequests.isEmpty){
      futureSets += (notificationActor ? NotificationRequestContainer(new NotificationRequest(request))).mapTo[ListBuffer[ListSet[Object]]]
    }
    
    if(request.entertainmentRequests != null && !request.entertainmentRequests.isEmpty){
      futureSets += (entertainmentActor ? EntertainmentRequestContainer(request.entertainmentRequests)).mapTo[ListBuffer[ListSet[Object]]]
    }
    
    Future.sequence(futureSets) onComplete{
      
      case Success(completed) => 
        
        completed.map{
          set => primeSet ++= set
        }
        
        reply(origin, primeSet.sort)
        complete()
        
      case Failure(e) => e.printStackTrace()
    }
  }
}