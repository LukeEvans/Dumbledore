package com.reactor.dumbledore.notifications

import java.util.ArrayList
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.Map
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Failure
import scala.util.Success
import com.reactor.dumbledore.messaging.NotificationRequest
import com.reactor.dumbledore.messaging.NotificationRequestContainer
import com.reactor.dumbledore.services.ServiceActor
import com.reactor.patterns.pull.FlowControlActor
import com.reactor.patterns.pull.FlowControlArgs
import com.reactor.prime.user.UserCredentials
import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import scala.util.Random
import com.github.nscala_time.time.Imports.DateTime
import com.reactor.dumbledore.services.WebRequestData
import com.reactor.dumbledore.messaging.ServiceRequest
import com.reactor.dumbledore.data.ListSet
import com.reactor.dumbledore.messaging.DataSetContainer
import scala.collection.mutable.ListBuffer
import com.reactor.dumbledore.utilities.Tools
import com.reactor.dumbledore.data.Parameters
import com.reactor.dumbledore.messaging.ListSetContainer

case class NotificationArgs(serviceActor:ActorRef) extends FlowControlArgs{  
  override def workerArgs(): FlowControlArgs ={
    val newArgs = new NotificationArgs(serviceActor)
    newArgs.addMaster(master)
    return newArgs
  }
}

class NotificationActor(args:NotificationArgs) extends FlowControlActor(args) {

  val notifManager = new NotificationManager
  val webServiceActor = args.serviceActor // Handles services
  ready
  
  override def preStart() = println("Creating NotificationManagerActor")
  override def postStop() = println("Stopping NotificationManagerActor")
  
  /** Handle messages */
  override def receive = {
    
    // Notification Request
    case reqContainer:NotificationRequestContainer => 
      manage(reqContainer.request, sender)
    
    // Unkown Message
    case a:Any => 
      println("Unknown Message: " + a.toString)
      complete()
  }

  /** Handle notification request and return response */
  private def manage(request:NotificationRequest, origin:ActorRef){ 
    implicit val timeout = Timeout(30 seconds);
    
	val params = new Parameters(request.getUserCredentials)
    val responseData:ListBuffer[ListSet[Object]] = ListBuffer[ListSet[Object]]()

	val time = request.time.offsetTime
	var webServices:Map[String, WebRequestData] = null
	
	// Determine Dev or Non-Dev Logic
    request.dev match{
	  case false => // Non dev notifications
	    webServices = notifManager.getWebServices(request.serviceRequest, time)
	  case true => // Dev Notifications
	    webServices = notifManager.getDevWebServices(time)
	}
	
	// Get list of future results
	val futureResults = requestResults(webServices, params.map)
	
	// Sequence List to Future[ListBuffer]
	Future.sequence(futureResults) onComplete{
	  case Success(dataList) => 

	    dataList map {
	    	listSetContainer => // Single Data Set Container
	    	  val serviceData = listSetContainer.data
	    	  if(serviceData.set_data != null && !serviceData.set_data.isEmpty)
	    		  responseData += serviceData
	    }
	    
	    reply(origin, DataSetContainer(responseData)) // Return Data to API Service
	    complete()
	    
	  case Failure(failure) => 
	    println(failure)
	    complete()
	}
  }
  
  /*** Create list of future results ***
   */
  def requestResults(services:Map[String, WebRequestData], 
      params:Map[String, String]):ListBuffer[Future[ListSetContainer]] = {
    
    implicit val timeout = Timeout(30 seconds) //Request timeout
    val results = new ListBuffer[Future[ListSetContainer]]
    
    services.map{
      service =>
    	var allParams = Map[String, String]()
    	service._2.params match{
    	  case Some(serviceParams) =>
    	  	allParams  = params ++ serviceParams
    	  case None => allParams = params
    	}	        
    	println(allParams)
    	results += (webServiceActor ? ServiceRequest(service._1, service._2.endpoint, service._2.ids, Some(allParams))).mapTo[ListSetContainer]
    }    
    results
  }
  
}