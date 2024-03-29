package com.reactor.dumbledore.api

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.RepointableActorRef
import akka.io.IO
import spray.can.Http
import akka.pattern.AskTimeoutException
import akka.pattern.ask
import akka.util.Timeout
import spray.http._
import spray.http.HttpMethods._
import spray.http.HttpRequest
import spray.http.StatusCodes.BadRequest
import spray.http.StatusCodes.InternalServerError
import spray.http.StatusCodes.RequestTimeout
import spray.routing.ExceptionHandler
import spray.routing.HttpService
import spray.util.LoggingContext
import spray.http.Uri.Host
import com.reactor.dumbledore.messaging._
import scala.util.Success
import scala.util.Failure
import com.reactor.dumbledore.messaging.request

trait ApiService extends HttpService{

  val winstonAPIActor:ActorRef
  
  private implicit val timeout = Timeout(5 seconds);
  private implicit val system = ActorSystem("DumbledoreClusterSystem-0-1")
  
  val mapper = new ObjectMapper() with ScalaObjectMapper
    mapper.registerModule(DefaultScalaModule)
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  
  val apiRoute =
        path(""){
          get{
            complete{
              "Dumbledore API 1.0"
            }
          }
        }~
        path(Rest ){ restPath =>
          entity(as[HttpRequest]){
            obj =>{
            	complete{
                  winstonAPIActor.ask(RequestContainer(obj))(10.seconds).mapTo[ResponseContainer] map{
                    container =>
                      container.response
                  }
                } 
            }
          }
        }~
        path("health"){
        	complete{"OK."}
        }~
        pathPrefix("css" / Segment) { file =>
          get {
            getFromResource("web/css/" + file)
          }
        }~
        path(RestPath) { path =>
          val resourcePath = "/usr/local/reducto-dist" + "/config/loader/" + path
          getFromFile(resourcePath)
        }
}

class ApiActor(winston:ActorRef) extends Actor with ApiService {
	def actorRefFactory = context
	val winstonAPIActor = winston
	println("Starting API Service actor...")
  
implicit def ReductoExceptionHandler(implicit log: LoggingContext) =
  ExceptionHandler {
    case e: NoSuchElementException => ctx =>
      println("no element")
      val err = "\n--No Such Element Exception--"
      log.error("{}\n encountered while handling request:\n {}\n\n{}", err, ctx.request,e)
      ctx.complete(BadRequest, "Ensure all required fields are present.")
    
    case e: JsonParseException => ctx =>
      println("json parse")
      val err = "\n--Exception parsing input--"
      log.error("{}\nencountered while handling request:\n {}\n\n{}", err, ctx.request,e)
      ctx.complete(InternalServerError, "Ensure all required fields are present with all Illegal characters properly escaped")
      
    case e: AskTimeoutException => ctx =>
      println("Ask Timeout")
      val err = "\n--Timeout Exception--"
      log.error("{}\nencountered while handling request:\n {}\n\n{}", err, ctx.request,e)
      ctx.complete(RequestTimeout, "Server Timeout")
    
    case e: NullPointerException => ctx => 
      println("Null Pointer")
      val err = "\n--Exception parsing input--"
      log.error("{}\nencountered while handling request:\n {}\n\n{}", err, ctx.request,e)
      ctx.complete(InternalServerError, "Ensure all required fields are present with all Illegal characters properly escaped")
    
    case e: Exception => ctx => 
      e.printStackTrace()
      println("Unknown")
      val err = "\n--Unknon Exception--"
      log.error("{}\nencountered while handling request:\n {}\n\n{}", err, ctx.request,e)
      ctx.complete(InternalServerError, "Internal Server Error")
  }   
  // Route requests to our HttpService
  def receive = runRoute(apiRoute)
}