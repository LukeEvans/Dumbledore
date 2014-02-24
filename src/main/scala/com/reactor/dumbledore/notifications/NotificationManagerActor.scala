package com.reactor.dumbledore.notifications

import java.util.ArrayList

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.Map
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Failure
import scala.util.Success

import com.reactor.dumbledore.messaging.DataContainer
import com.reactor.dumbledore.messaging.NotificationRequest
import com.reactor.dumbledore.messaging.NotificationRequestContainer
import com.reactor.dumbledore.messaging.ServiceRequest
import com.reactor.dumbledore.messaging.SingleDataContainer
import com.reactor.dumbledore.services.ServiceActor
import com.reactor.patterns.pull.FlowControlActor
import com.reactor.patterns.pull.FlowControlArgs
import com.reactor.prime.user.UserCredentials

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import scala.util.Random


class NotificationManagerActor(args:NotificationArgs) extends FlowControlActor(args) {
  val noteServices = Map("fb_messages" -> "/social/facebook/inbox",
		  				  "fb_notifications" -> "/social/facebook/notifications",
		  				  "fb_birthdays" -> "/social/facebook/birthdays",
		  				  "nearby_photos" -> "/instagram/location",
		  				  "nearby_places" -> "/yelp",
		  				  "stocks" -> "/stocks")	  				  

  ready
  override def preStart() = println("Creating NotificationManagerActor")
  override def postStop() = println("Stopping NotificationManagerActor")
	
  override def receive = {
    case reqContainer:NotificationRequestContainer => 
      manage(reqContainer.request, sender)
      complete()
    case a:Any => 
      println("Unknown Message: " + a.toString)
      complete()
  }
	
  def manage(request:NotificationRequest, origin:ActorRef){ 
    implicit val timeout = Timeout(10 seconds);
	val creds = getUserCreds(request)
	val params = getUserParams(creds)
    val data:ArrayList[ArrayList[Object]] = new ArrayList[ArrayList[Object]]
	
	val results = new ArrayBuffer[Future[SingleDataContainer]]
	
	noteServices.map {
	  service => 
	    results += (args.serviceActor ? ServiceRequest(service._2, Some(params))).mapTo[SingleDataContainer]
	}
		
	Future.sequence(results) onComplete{
	  case Success(dataList) => 

	    dataList map {
	    	dataContainer => data.add(dataContainer.data)
	    }
	    reply(origin, DataContainer(data))
	    
	  case Failure(failure) => println(failure)
	}
  }
  
  def getUserParams(userCreds:UserCredentials):Map[String, String] = {
    var userParams = Map[String, String]()
    
    if(userCreds.udid != null)
      userParams.put("udid", userCreds.udid)
    if(userCreds.location != null){
      userParams.put("lat", userCreds.location.lat.toString)
      userParams.put("long", userCreds.location.long.toString)      
    }
    userParams
  }
  
  def getUserCreds(request:NotificationRequest):UserCredentials = {
    return new UserCredentials(request.udid)
    			.setLocation(request.lat, request.long)
  }
}