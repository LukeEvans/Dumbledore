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
import scala.concurrent._
import com.reactor.dumbledore.messaging.NotificationRequestContainer
import com.reactor.dumbledore.messaging.EntertainmentRequestContainer
import com.reactor.dumbledore.messaging.requests.NotificationRequest
import scala.util.Failure
import scala.util.Success
import com.reactor.dumbledore.notifications.request.Request
import com.reactor.dumbledore.messaging.PrimeRankContainer

case class PrimeActorArgs(channelActor:ActorRef, notificationActor:ActorRef, entertainmentActor:ActorRef, rankActor:ActorRef) extends FlowControlArgs{  
  override def workerArgs(): FlowControlArgs ={
    val newArgs = new PrimeActorArgs(channelActor, notificationActor, entertainmentActor, rankActor)
    newArgs.addMaster(master)
    return newArgs
  }
}

class PrimeActor(args:PrimeActorArgs) extends FlowControlActor(args) {
  
  val channelActor = args.channelActor
  val notificationActor = args.notificationActor
  val entertainmentActor = args.entertainmentActor
  val rankActor = args.rankActor
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
    
    val futureSets = getFutureSets(request)
    
    Future.sequence(futureSets) onComplete{
      
      case Success(completed) => 
        
        completed.foreach{
          set => primeSet ++= set
        }

        val futureRankedSet = (rankActor ? PrimeRankContainer(primeSet, request.time.offsetTime)).mapTo[PrimeSet]
        
        futureRankedSet.onComplete{
          case Success(rankedSet) =>
            
            rankedSet.sort.foreach(set => println("Set ID: " + set.card_id + "  Set Rank: " + set.rank))
            
            reply(origin, rankedSet.sort)
            complete()
            
          case Failure(e) => e.printStackTrace()
        }
        
      case Failure(e) => e.printStackTrace()
    }
  }
  
  private def getFutureSets(request:PrimeRequest):ListBuffer[Future[ListBuffer[ListSet[Object]]]] = {
    
    implicit val timeout = Timeout(30 seconds)
    
    val futureSets = ListBuffer[Future[ListBuffer[ListSet[Object]]]]()
    
    if(request.feedRequests != null && !request.feedRequests.isEmpty)
      futureSets += (channelActor ? FeedData(request.feedRequests)).mapTo[ListBuffer[ListSet[Object]]]
    
    if(request.socialRequests != null && !request.socialRequests.isEmpty)
      futureSets += getSocial(request.socialRequests)
    
    request.all match{
      
      case true =>
        val notificationRequest = new NotificationRequest(request)
        notificationRequest.dev = true
        
        futureSets += (notificationActor ? NotificationRequestContainer(notificationRequest)).mapTo[ListBuffer[ListSet[Object]]]
        
        futureSets += (entertainmentActor ? EntertainmentRequestContainer(request.entertainmentRequests, request.all)).mapTo[ListBuffer[ListSet[Object]]]
        
      case false =>{
        
        if(request.entertainmentRequests != null && !request.entertainmentRequests.isEmpty)
          futureSets += (entertainmentActor ? EntertainmentRequestContainer(request.entertainmentRequests, request.all)).mapTo[ListBuffer[ListSet[Object]]]
        
        if(request.notificationsRequests != null && !request.notificationsRequests.isEmpty)
          futureSets += (notificationActor ? NotificationRequestContainer(new NotificationRequest(request))).mapTo[ListBuffer[ListSet[Object]]]

      }
    }
    
    futureSets
  }
  
  /** Grab social place holder futures if included in request
   */
  private def getSocial(requests:ListBuffer[Request]):Future[ListBuffer[ListSet[Object]]] = {
    val futureSocial = ListBuffer[ListSet[Object]]()
    
    requests.foreach{
      request => 
        if(request.id.equalsIgnoreCase("facebook") || request.id.equalsIgnoreCase("twitter")){
          futureSocial += ListSet(request.id, 99, ListBuffer[Object]())
        }
    }
    return future{futureSocial}
  }
}