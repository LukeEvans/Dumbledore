package com.reactor.dumbledore.api

import scala.compat.Platform
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import akka.actor.Actor
import akka.util.Timeout
import spray.routing.ExceptionHandler
import spray.routing.HttpService
import spray.util.LoggingContext
import spray.http.StatusCodes.{BadRequest, InternalServerError, RequestTimeout}
import akka.pattern.AskTimeoutException
import akka.actor.ActorRef
import akka.actor.RepointableActorRef

trait ApiService extends HttpService{
  
  val engineActor:ActorRef
  
  private implicit val timeout = Timeout(5 seconds);
  
  val mapper = new ObjectMapper() with ScalaObjectMapper
    mapper.registerModule(DefaultScalaModule)
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  
  val apiRoute =
        path(""){
          get{
        	  //getFromResource("web/index.html") // html index
            complete{
              "Dumbledore API 1.0"
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

class ApiActor(engine:ActorRef) extends Actor with ApiService {
	def actorRefFactory = context
	val engineActor = engine
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