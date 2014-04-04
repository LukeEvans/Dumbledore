package com.reactor.dumbledore.api

import org.specs2.mutable.Specification
import org.specs2.time._
import com.reactor.dumbledore.prime.notifications.NotificationArgs
import com.reactor.dumbledore.prime.channels.ChannelArgs
import com.reactor.dumbledore.prime.services.twitter.TwitterArgs
import com.reactor.dumbledore.prime.services.twitter.TwitterBuilderArgs
import com.reactor.patterns.pull.FlowControlConfig
import com.reactor.patterns.pull.FlowControlFactory
import spray.http._
import spray.http.StatusCodes._
import spray.testkit.Specs2RouteTest
import akka.actor.ActorSystem
import akka.actor.Props
import com.reactor.dumbledore.prime.PrimeActor
import com.reactor.dumbledore.prime.PrimeActorArgs
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.databind.DeserializationFeature
import com.reactor.dumbledore.prime.notifications.request.Request
import com.reactor.dumbledore.prime.constants.Prime
import scala.collection.mutable.ListBuffer
import com.reactor.dumbledore.messaging.requests.FeedRequestData


class ApiServiceSpec extends Specification with Specs2RouteTest with ApiService {
  def actorRefFactory = system 
  
  // Initialize Actors
  val winstonAPIFlowConfig = FlowControlConfig(name="winstonAPIActor", actorType="com.reactor.dumbledore.prime.legacy.WinstonAPIActor")    
  val winstonAPIActor = FlowControlFactory.flowControlledActorForTests(system, winstonAPIFlowConfig)
    
  val extractorFlowConfig = FlowControlConfig(name="extractorActor", actorType="com.reactor.dumbledore.prime.abstraction.ExtractionActor", parallel = 30)
  val extractorActor = FlowControlFactory.flowControlledActorForTests(system, extractorFlowConfig)
    
  val twitterStoryBuilderFlowConfig = FlowControlConfig(name="twitterStoryBuilderActor", actorType="com.reactor.dumbledore.prime.services.twitter.TwitterStoryBuilderActor", parallel = 30)    
  val twitterStoryActor = FlowControlFactory.flowControlledActorForTests(system, twitterStoryBuilderFlowConfig, TwitterBuilderArgs(extractorActor))
    
  val twitterServiceFlowConfig = FlowControlConfig(name="twitterServiceActor", actorType="com.reactor.dumbledore.prime.services.twitter.TwitterServiceActor", parallel = 8)    
  val twitterActor = FlowControlFactory.flowControlledActorForTests(system, twitterServiceFlowConfig, TwitterArgs(twitterStoryActor))
    
  val serviceFlowConfig = FlowControlConfig(name="serviceActor", actorType="com.reactor.dumbledore.prime.services.ServiceActor", parallel=6)    
  val serviceActor = FlowControlFactory.flowControlledActorForTests(system, serviceFlowConfig)
    
  val notificationFlowConfig = FlowControlConfig(name="notificationActor", actorType="com.reactor.dumbledore.prime.notifications.NotificationActor", parallel =3)    
  val notificationActor = FlowControlFactory.flowControlledActorForTests(system, notificationFlowConfig, NotificationArgs(serviceActor))
    
  val feedFlowConfig = FlowControlConfig(name="feedActor", actorType="com.reactor.dumbledore.prime.channels.SingleFeedActor", parallel=9)    
  val feedActor = FlowControlFactory.flowControlledActorForTests(system, feedFlowConfig)
    
  val sourceFlowConfig = FlowControlConfig(name="sourceActor", actorType="com.reactor.dumbledore.prime.channels.sources.SourceActor", parallel=5)    
  val sourceActor = FlowControlFactory.flowControlledActorForTests(system, sourceFlowConfig)
    
  val channelsFlowConfig = FlowControlConfig(name="channelsActor", actorType="com.reactor.dumbledore.prime.channels.ChannelsActor")    
  val channelsActor = FlowControlFactory.flowControlledActorForTests(system, channelsFlowConfig, ChannelArgs(feedActor, sourceActor))
  
  val entFlowConfig = FlowControlConfig(name="entActor", actorType="com.reactor.dumbledore.prime.entertainment.EntertainmentActor", parallel=5)    
  val entActor = FlowControlFactory.flowControlledActorForSystem(system, entFlowConfig)
  
  val rankFlowConfig = FlowControlConfig(name="rankActor", actorType="com.reactor.dumbledore.prime.rank.SetRankerActor", parallel=5)    
  val rankActor = FlowControlFactory.flowControlledActorForTests(system, rankFlowConfig)
  
  val primeFlowConfig = FlowControlConfig(name="primeActor", actorType="com.reactor.dumbledore.prime.PrimeActor")
  val primeActor = FlowControlFactory.flowControlledActorForTests(system, primeFlowConfig, PrimeActorArgs(channelsActor, notificationActor, entActor, rankActor))
  
  
  // Set Time
  val time = new scala.concurrent.duration.FiniteDuration(10, java.util.concurrent.TimeUnit.SECONDS)  
  implicit val routeTestTimeout = RouteTestTimeout(time) 

  
  "The ApiService" should {
    
    
    "return Dumbledore API 1.0 at root" in {
      Get() ~> apiRoute ~> check{
        responseAs[String] must contain("Dumbledore API")
      }
    }   
    
    basicPostCheck("/primetime", RequestConstants.primeRequest)
    
    basicGetCheck("/channel/feeds")
    
    basicGetCheck("/channel/feeds?clearCache=true")
    
    basicPostCheck("/channel/feed", RequestConstants.feedsRequest)
    
    basicPostCheck("/channel/source", RequestConstants.sourceRequest)

    "pass all tests" in {
      "pass" must contain("pass")
    }
  }
  
  
  /** Check Post request for ok status and non-empty data
   */
  def basicPostCheck(endpoint:String, requestObj:Object){
    
	"handle Post request to " + endpoint in {
      Post(endpoint, mapper.writeValueAsString(requestObj)) ~>
        apiRoute ~> check{
          
          val jsonNode = mapper.readTree(responseAs[String]);
          
          "with data in response" in{
        	jsonNode.has("data") must equalTo(true)
          
            jsonNode.get("data") must not equalTo(null)
        	
        	jsonNode.get("data").size() must not equalTo(0)
          }          
          
          "with 'ok' in status " in{
            jsonNode.get("status").asText() must equalTo("ok")
          }
        }
    }
  }
  
  
  /** Check Get request for ok status and non-empty data
   */
  def basicGetCheck(endpoint:String){
    
	"handle Get request to " + endpoint in {
      Get(endpoint) ~>
        apiRoute ~> check{
          
          val jsonNode = mapper.readTree(responseAs[String]);
          
          "with data in response" in{
        	jsonNode.has("data") must equalTo(true)
          
            jsonNode.get("data") must not equalTo(null)
        	
        	jsonNode.get("data").size() must not equalTo(0)
          }          
          
          "with 'ok' in status " in{
            jsonNode.get("status").asText() must equalTo("ok")
          }
        }
    }
  }
  
}


/** /primetime request object */
case class PrimeTimeTestRequest(udid:String, timezone_offset:Int, lat:Double, long:Double, 
    dev:Boolean, all:Boolean, notifications:List[Request], feeds:List[Request], ent:List[Request], social:List[Request])

    
/** /channel/feed request object */
case class FeedTestRequest(data:ListBuffer[FeedRequestData])


/** /channel/source request object */
case class SourceTestRequest(data:ListBuffer[String])
    

/** Request object constants */
object RequestConstants{
  
  /* Social Requests */
  val fbRequest = new Request(Prime.FACEBOOK, None, null)  
  val twitterRequest = new Request(Prime.TWITTER, None, null)
  
  /* Entertainment Requests */
  val comicRequest = new Request(Prime.COMICS, None, null)
  val popVideosRequest = new Request(Prime.POPULAR_VIDEOS, None, null)
   
  /* Notification Requests */
  val nearbyPlacesRequest = new Request(Prime.NEARBY_PLACES, None, null)   
  val nearbyPhotosRequest = new Request(Prime.NEARBY_PHOTOS, None, null)   
  val stocksRequest = new Request(Prime.STOCKS, None, null)    
  val weatherRequest = new Request(Prime.WEATHER, None, null)
  
  /* Social Request List */
  val social = List(fbRequest, twitterRequest)
  
  /* Entertainment Request List */
  val ent = List(comicRequest, popVideosRequest)
    
  /* Notification Request List */
  val notif = List(nearbyPlacesRequest, nearbyPhotosRequest, stocksRequest, weatherRequest)
  
  /* PrimeTime Requests */
  val primeRequest = PrimeTimeTestRequest("9F6979AD-E0B4-4E7A-B9B7-BA6B8C6A8F60", -21600, 40.0152, -105.2762, 
        false, false, notif, List(), ent, social)
        
  val failRequest = PrimeTimeTestRequest("", 0, 0, 0, false, false, List(), List(), List(), List())
  
  /* Channel Feeds Requests*/
  val feedsRequest = FeedTestRequest(ListBuffer(FeedRequestData("Sports", null), FeedRequestData("Entertainment", null), 
      FeedRequestData("Cuisine", null), FeedRequestData("Business", null), FeedRequestData("World News", null),
      FeedRequestData("Gaming", null), FeedRequestData("Technology", null), FeedRequestData("Politics", null),
      FeedRequestData("Headline News", null)))
      
  /* Channel Sources Requests */
  val sourceRequest = SourceTestRequest(ListBuffer("npr_business", "advblog", "atlantic", "bbc_business"))

}