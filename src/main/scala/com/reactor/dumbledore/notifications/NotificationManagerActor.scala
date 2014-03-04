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
import com.reactor.dumbledore.messaging.SingleDataContainer
import com.reactor.dumbledore.services.ServiceActor
import com.reactor.patterns.pull.FlowControlActor
import com.reactor.patterns.pull.FlowControlArgs
import com.reactor.prime.user.UserCredentials
import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import scala.util.Random
import com.github.nscala_time.time.Imports.DateTime
import com.reactor.dumbledore.services.ServiceData
import com.reactor.dumbledore.messaging.ServiceRequest


class NotificationManagerActor(args:NotificationArgs) extends FlowControlActor(args) {

  val notifManager = new NotificationManager

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
    implicit val timeout = Timeout(30 seconds);
	val params = getUserParams(getUserCreds(request))
    val data:ArrayList[ArrayList[Object]] = new ArrayList[ArrayList[Object]]

	val time = DateTime.now
	var services:Map[String, ServiceData] = null
	
    request.dev match{
	  case false => // Non dev notifications
	    services = notifManager.getServices(request.serviceRequest, time)
	  case true => // Dev Notifications
	    services = notifManager.getDevServices(time)
	}
	
	val futureResults = requestResults(services, args.serviceActor, params)
	
	Future.sequence(futureResults) onComplete{
	  case Success(dataList) => 

	    dataList map {
	    	dataContainer => 
	    	  val dataReceived = dataContainer.data
	    	  if(dataReceived != null)
	    		  data.add(dataReceived)
	    }
	    reply(origin, DataContainer(data))
	    
	  case Failure(failure) => println(failure)
	}
  }
  
  // request future results from service actor
  def requestResults(services:Map[String, ServiceData], 
		  serviceActor:ActorRef, params:Map[String, String]):ArrayBuffer[Future[SingleDataContainer]] = {
    implicit val timeout = Timeout(30 seconds)
    val results = new ArrayBuffer[Future[SingleDataContainer]]
    
    services.map{
      service =>
    	var allParams = Map[String, String]()
	        service._2.params match{
	          case Some(serviceParams) =>
	            allParams  = params ++ serviceParams
	          case None => allParams = params
	        }	        
	        println(allParams)
	        results += (serviceActor ? ServiceRequest(service._1, service._2.ids, Some(allParams))).mapTo[SingleDataContainer]
    }    
    results
  }
  
  // User credentials to Map[String, String]
  def getUserParams(userCreds:UserCredentials):Map[String, String] = {
    var userParams = Map[String, String]()
    
    if(userCreds.udid != null)
      userParams.put("udid", userCreds.udid)
    if(userCreds.location != null){
      userParams.put("lat", userCreds.location.lat.toString)
      userParams.put("long", userCreds.location.long.toString)     
      userParams.put("loc", userCreds.location.lat.toString+","+userCreds.location.long.toString)
    }
    userParams
  }
  
  // Grab User credentials from request
  def getUserCreds(request:NotificationRequest):UserCredentials = {
    return new UserCredentials(request.udid)
    			.setLocation(request.lat, request.long)
  }
}