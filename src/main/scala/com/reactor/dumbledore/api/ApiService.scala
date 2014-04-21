package com.reactor.dumbledore.api

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.reactor.dumbledore.messaging._
import com.reactor.dumbledore.messaging.requests._
import com.redis._
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
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
import scala.collection.mutable.ListBuffer
import com.reactor.dumbledore.prime.data.ListSet
import spray.routing.RequestContext
import spray.routing.StandardRoute
import com.reactor.dumbledore.messaging.requests.ChannelFeedRequest
import com.gravity.goose._
import com.fasterxml.jackson.databind.SerializationFeature
import com.reactor.dumbledore.prime.services.sports.StatsAPI
import com.reactor.patterns.throttle.TimerBasedThrottler
import com.reactor.patterns.throttle.Throttler.Rate
import akka.actor.Props
import com.reactor.patterns.throttle.Dispatcher
import com.reactor.patterns.throttle.Throttler.SetTarget
import com.reactor.patterns.transport.RESTRequest
import com.reactor.patterns.transport.DispatchRequest
import com.reactor.patterns.throttle.Throttler.Queue


trait ApiService extends HttpService{

  val winstonAPIActor:ActorRef
  val notificationActor:ActorRef
  val channelsActor:ActorRef
  val twitterActor:ActorRef
  val primeActor:ActorRef
  
  implicit val throttler:ActorRef
  private implicit val timeout = Timeout(5 seconds);
  private implicit val actorSystem = ActorSystem("DumbledoreClusterSystem-0-1")
  
  val mapper = new ObjectMapper() with ScalaObjectMapper
    mapper.registerModule(DefaultScalaModule)
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
     
  val apiRoute =
    path(""){
      getOrPost{
        obj =>
          complete{
            "Dumbledore API 1.2!!!"
          }
      }
    }~
    path("primetime"){
      getOrPost{
        obj =>
          val response = new Response()
          val request = new PrimeRequest(obj)
          complete{
            primeActor.ask(request)(30.seconds).mapTo[ListBuffer[ListSet[Object]]] map{
    	      data => response.finish(data, mapper)
    	    }
          }
      }
    }~
    path("notifications"){
      getOrPost{
        obj =>
          val response = new Response()
          val request = new NotificationRequest(obj)
          complete{
        	notificationActor.ask(NotificationRequestContainer(request))(15.seconds).mapTo[ListBuffer[ListSet[Object]]] map{
              data => response.finish(data, mapper)
        	}
          }
      }
    }~
    path("channel"/"feeds"){
      getOrPost{
        obj =>
          val response = new Response()
          val request = new ChannelFeedRequest(obj)
          complete{
        	channelsActor.ask(Feeds(request.clearCache))(10.seconds).mapTo[ListBuffer[JsonNode]] map{
        	  data => response.finish(data, mapper)
        	}
          }
      }
    }~
    path("channel"/"feed"){
      getOrPost{
    	obj =>
    	  val response = new Response()
    	  val request = new FeedRequest(obj)
    	  complete{
    	    channelsActor.ask(FeedData(request.channelList))(10.seconds).mapTo[ListBuffer[ListSet[Object]]] map{
    	      data => response.finish(data, mapper)
    	    }
    	  }
      }
    }~
    path("channel"/"source"){
      getOrPost{
        obj =>
          val response = new Response()
          val request = new ChannelRequest(obj)
          complete{
        	channelsActor.ask(SourceData(request.channelIDs))(10.seconds).mapTo[ListBuffer[ListSet[Object]]] map{
        	  data => response.finish(data, mapper)
        	}
          }
      }
    }~
    path("social"/"twitter2"){
      getOrPost{
        obj =>
          val response = new Response()
          val request = new TwitterRequest(obj)
          complete{
            twitterActor.ask(request)(30.seconds).mapTo[ListBuffer[Object]] map{
              data => response.finish(data, mapper)
            }
          }
      }
    }~
    path("youtube"){
      getOrPost{
    	obj =>
    	  val response = new Response()
    	  val request = new YoutubeRequest(obj)
    	  complete{
    		primeActor.ask(request)(30.seconds).mapTo[ListBuffer[Object]] map{
    		  data => response.finish(data, mapper)
    		}
    	  }
      }
    }~
    path("channel"){
      entity(as[HttpRequest]){
        obj =>{
          complete{
            winstonAPIActor.ask(RequestContainer(obj, "dev.winstonapi.com"))(30.seconds).mapTo[ResponseContainer] map{
              container => container.response
            }
          }
        }
      }  
    }~
    path("channel"/"sources"){
      entity(as[HttpRequest]){
    	obj =>{
          complete{
        	winstonAPIActor.ask(RequestContainer(obj, "dev.winstonapi.com"))(30.seconds).mapTo[ResponseContainer] map{
           	  container => container.response
            }
          }
    	}
      }      
    }~
    path("loaderio-31d8c2cac95d79a88fc7a1db9668583c"){
      val localLoadPath = "/Users/kevincolin/Development/Winston/projects/Dumbledore/loader"
      val remoteLoadPath = "/home/ubuntu/loader"
      getFromFile(remoteLoadPath)
    }~
    path(Rest ){ restPath =>
      entity(as[HttpRequest]){
        obj =>{
          complete{
            winstonAPIActor.ask(RequestContainer(obj, "v036.winstonapi.com"))(30.seconds).mapTo[ResponseContainer] map{
              container => container.response
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
 
    
    /** Handle Get or Post Requests
     */
    def getOrPost(comp:Object => StandardRoute):RequestContext => Unit = {

      get{
        respondWithMediaType(MediaTypes.`application/json`){
          entity(as[HttpRequest]){
            obj =>{
              comp(obj)
            }
          }
        }
      }~
      post{
        respondWithMediaType(MediaTypes.`application/json`){
          entity(as[String]){
            obj =>{
              comp(obj)
            }
          }
        }	
      }
    }
    
    
    /** Initiate requests for throttler
     */
    def initiateRequest(request:RESTRequest, ctx: RequestContext){
      
      val dispatchReq = DispatchRequest(com.reactor.patterns.transport.RequestContainer(request), ctx, mapper)
      throttler.tell(Queue(dispatchReq), Actor.noSender)
    }
}

class ApiActor(winston:ActorRef, notifications:ActorRef, channels:ActorRef, twitter:ActorRef, prime:ActorRef) extends Actor with ApiService {
	def actorRefFactory = context
	val winstonAPIActor = winston
	val notificationActor = notifications
	val channelsActor = channels
	val twitterActor = twitter
	val primeActor = prime
	
	val dispatcher = actorRefFactory.actorOf(Props(classOf[Dispatcher], primeActor), "dispatcher")
	val throttler = actorRefFactory.actorOf(Props(new TimerBasedThrottler(new Rate(40, 1 seconds))))
	
	throttler ! SetTarget(Some(dispatcher))
	
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