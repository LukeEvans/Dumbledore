package com.reactor.dumbledore.legacy

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import akka.actor.ActorLogging
import akka.actor.Actor
import akka.pattern.ask
import akka.actor.ActorRef
import spray.http.HttpRequest
import spray.http.HttpMethods
import spray.http.HttpResponse
import akka.io.IO
import spray.can.Http
import akka.actor.ActorSystem
import akka.util.Timeout
import scala.util.Success
import scala.util.Failure
import com.reactor.dumbledore.messaging._
import com.reactor.patterns.pull.FlowControlActor
import com.reactor.patterns.pull.FlowControlArgs

class WinstonAPIActor(args:FlowControlArgs) extends FlowControlActor(args){//extends Actor with ActorLogging {
  private implicit val system = ActorSystem("DumbledoreClusterSystem-0-1")
  private implicit val timeout = Timeout(30 seconds)
  
  ready
  
  def receive ={
    case req:RequestContainer => 
      processRequest(req.request, sender)
      complete
    case a:Any =>
      println("Unsupported message received: " + a.toString)
      complete
  }
  
  def processRequest(request:HttpRequest, origin:ActorRef){
    request.method.value match{
      case "POST" =>{
    	var requestObj = new HttpRequest(HttpMethods.POST, request.uri.withAuthority("v036.winstonapi.com", 0), Nil, request.entity)
    	val response = (IO(Http) ? requestObj).mapTo[HttpResponse]
    	response onComplete{
    	  case Success(json) => 
    	    origin ! ResponseContainer(json)
    	  case Failure(failure) => 
    	    println(failure)
    	}
      }
      case "PUT" =>{
    	var requestObj = new HttpRequest(HttpMethods.PUT, request.uri.withAuthority("v036.winstonapi.com", 0), Nil, request.entity)
    	val response = (IO(Http) ? requestObj).mapTo[HttpResponse]
    	response onComplete{
    	  case Success(json) => 
    	    origin ! ResponseContainer(json)
    	  case Failure(failure) => 
    	    println(failure)
    	}
      }
      case "GET" =>{
    	var requestObj = new HttpRequest(HttpMethods.GET, request.uri.withAuthority("v036.winstonapi.com", 0))
    	val response = (IO(Http) ? requestObj).mapTo[HttpResponse]
    	response onComplete{
    	  case Success(json) => 
    	    origin ! ResponseContainer(json)
    	  case Failure(failure) => 
    	    println(failure) 
    	}
      }
    }
  }
}