package com.reactor.dumbledore.api

import java.net.InetSocketAddress
import java.util.HashSet
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.reflect.ClassTag
import scala.collection.mutable.ListBuffer
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.reactor.dumbledore.messaging._
import com.reactor.dumbledore.messaging.ChannelRequest
import com.reactor.dumbledore.messaging.NotificationRequest
import com.reactor.dumbledore.messaging.request
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.pattern.AskTimeoutException
import akka.pattern.ask
import akka.util.Timeout
import net.spy.memcached.MemcachedClient
import redis.clients.jedis.JedisSentinelPool
import redis.clients.jedis.HostAndPort
import spray.http._
import spray.http.HttpMethods._
import spray.http.HttpRequest
import spray.http.StatusCodes.BadRequest
import spray.http.StatusCodes.InternalServerError
import spray.http.StatusCodes.RequestTimeout
import spray.routing.ExceptionHandler
import spray.routing.HttpService
import spray.util.LoggingContext
import redis.clients.jedis.JedisCluster
import com.fasterxml.jackson.databind.JsonNode
import scala.collection.immutable.ListSet
import com.redis._

trait ApiService extends HttpService{

  val winstonAPIActor:ActorRef
  val notificationActor:ActorRef
  val channelsActor:ActorRef
  
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
        path("cacheTest"){
//          val c = new MemcachedClient(
//                new InetSocketAddress("freebasecachecluster.1hm814.0001.use1.cache.amazonaws.com", 11211));
          
//          val jedisClusterNodes = new HashSet[HostAndPort]()
//          jedisClusterNodes.add(new HostAndPort("freebase-redis-cache.1hm814.0001.use1.cache.amazonaws.com", 6379))
//          val jc = new JedisCluster(jedisClusterNodes);
          
          val r = new RedisClient("freebase-redis-cache.1hm814.0001.use1.cache.amazonaws.com", 6379)
          complete{
//            c.set("test_key", 3600, "test_value")
//            c.get("test_key").toString()
//            jc.set("test_key", "redis value")
//            jc.get("test_key")
            r.set("test_key", "Scala-Redis value")
            r.get("test_key") match{
              case Some(value) => value
              case None => "NO VALUE"
            }
          }
        }~
        path("notifications"){
         get{
           respondWithMediaType(MediaTypes.`application/json`){
             entity(as[HttpRequest]){
               obj =>{
                 val response = new Result()
                 val request = new NotificationRequest(obj)
                 complete{
                   notificationActor.ask(NotificationRequestContainer(request))(15.seconds).mapTo[DataSetContainer] map{
                     container =>
                       response.finish(container.data, mapper)
                   }
                 }
               }
             }
           }
         }~
         post{
           respondWithMediaType(MediaTypes.`application/json`){
             entity(as[String]){
               obj =>{
                 val response = new Result()
                 val request = new NotificationRequest(obj)
                 complete{
                   notificationActor.ask(NotificationRequestContainer(request))(15.seconds).mapTo[DataSetContainer] map{
                     container =>
                       response.finish(container.data, mapper)
                   }
                 }
               }
             }
           }
         }
        }~
        path("channel"){
          entity(as[HttpRequest]){
            obj =>{
              complete{
            	winstonAPIActor.ask(RequestContainer(obj, "dev.winstonapi.com"))(30.seconds).mapTo[ResponseContainer] map{
            	  container =>
                    container.response
                }
              }
            }
          }  
        }~
        path("channel"/"feeds"){
           respondWithMediaType(MediaTypes.`application/json`){
             val response = new Result()
             
             complete{
            	channelsActor.ask(Feeds())(10.seconds).mapTo[ListBuffer[JsonNode]] map{
            	  data =>
            	    response.finish(data, mapper)
            	}
             }
           }
        }~
        path("channel"/"feed"){
           respondWithMediaType(MediaTypes.`application/json`){
             entity(as[String]){
               obj =>
                 val response = new Result()
                 val request = new FeedRequest(obj)
                 
                 complete{
                   channelsActor.ask(FeedData(request.channelList))(10.seconds).mapTo[ListBuffer[ListSet[Object]]] map{
            	     data =>
            	       response.finish(data, mapper)
            	   }
                 }
             }
           }
        }~
        path("channel"/"source"){
          respondWithMediaType(MediaTypes.`application/json`){
             entity(as[String]){
               obj =>
                 val response = new Result()
                 val request = new ChannelRequest(obj)
                 
                 complete{
                   channelsActor.ask(SourceData(request.channelIDs))(10.seconds).mapTo[ListBuffer[ListSet[Object]]] map{
            	     data =>
            	       response.finish(data, mapper)
            	   }
                 }
             }
          }
        }
        path("channel"/"sources"){
          entity(as[HttpRequest]){
            obj =>{
              complete{
            	winstonAPIActor.ask(RequestContainer(obj, "dev.winstonapi.com"))(30.seconds).mapTo[ResponseContainer] map{
            	  container =>
                    container.response
                }
              }
            }
          }      
        }~
        path(Rest ){ restPath =>
          entity(as[HttpRequest]){
            obj =>{
            	complete{
                  winstonAPIActor.ask(RequestContainer(obj, "v036.winstonapi.com"))(30.seconds).mapTo[ResponseContainer] map{
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

class ApiActor(winston:ActorRef, notifications:ActorRef, channels:ActorRef) extends Actor with ApiService {
	def actorRefFactory = context
	val winstonAPIActor = winston
	val notificationActor = notifications
	val channelsActor = channels
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