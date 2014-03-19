package com.reactor.dumbledore.api

import org.specs2.mutable.Specification
import org.specs2.time._
import com.reactor.dumbledore.notifications.NotificationArgs
import com.reactor.dumbledore.prime.channels.ChannelArgs
import com.reactor.dumbledore.prime.twitter.TwitterArgs
import com.reactor.dumbledore.prime.twitter.TwitterBuilderArgs
import com.reactor.patterns.pull.FlowControlConfig
import com.reactor.patterns.pull.FlowControlFactory
import spray.http._
import spray.http.StatusCodes._
import spray.testkit.Specs2RouteTest
import akka.actor.ActorSystem
import akka.actor.Props
import com.reactor.dumbledore.prime.PrimeActor


class ApiServiceSpec extends Specification with Specs2RouteTest with ApiService {
  def actorRefFactory = system 
  
  val winstonAPIFlowConfig = FlowControlConfig(name="winstonAPIActor", actorType="com.reactor.dumbledore.legacy.WinstonAPIActor")    
  val winstonAPIActor = FlowControlFactory.flowControlledActorForTests(system, winstonAPIFlowConfig)
    
  val primeFlowConfig = FlowControlConfig(name="primeActor", actorType="com.reactor.dumbledore.prime.PrimeActor")
  val primeActor = FlowControlFactory.flowControlledActorForTests(system, primeFlowConfig)
    
  val extractorFlowConfig = FlowControlConfig(name="extractorActor", actorType="com.reactor.dumbledore.prime.abstraction.ExtractionActor", parallel = 30)
  val extractorActor = FlowControlFactory.flowControlledActorForTests(system, extractorFlowConfig)
    
  val twitterStoryBuilderFlowConfig = FlowControlConfig(name="twitterStoryBuilderActor", actorType="com.reactor.dumbledore.prime.twitter.TwitterStoryBuilderActor", parallel = 30)    
  val twitterStoryActor = FlowControlFactory.flowControlledActorForTests(system, twitterStoryBuilderFlowConfig, TwitterBuilderArgs(extractorActor))
    
  val twitterServiceFlowConfig = FlowControlConfig(name="twitterServiceActor", actorType="com.reactor.dumbledore.prime.twitter.TwitterServiceActor", parallel = 8)    
  val twitterActor = FlowControlFactory.flowControlledActorForTests(system, twitterServiceFlowConfig, TwitterArgs(twitterStoryActor))
    
  val serviceFlowConfig = FlowControlConfig(name="serviceActor", actorType="com.reactor.dumbledore.services.ServiceActor", parallel=6)    
  val serviceActor = FlowControlFactory.flowControlledActorForTests(system, serviceFlowConfig)
    
  val notificationFlowConfig = FlowControlConfig(name="notificationActor", actorType="com.reactor.dumbledore.notifications.NotificationActor", parallel =3)    
  val notificationActor = FlowControlFactory.flowControlledActorForTests(system, notificationFlowConfig, NotificationArgs(serviceActor))
    
  val feedFlowConfig = FlowControlConfig(name="feedActor", actorType="com.reactor.dumbledore.prime.channels.SingleFeedActor", parallel=9)    
  val feedActor = FlowControlFactory.flowControlledActorForTests(system, feedFlowConfig)
    
  val sourceFlowConfig = FlowControlConfig(name="sourceActor", actorType="com.reactor.dumbledore.prime.channels.sources.SourceActor", parallel=5)    
  val sourceActor = FlowControlFactory.flowControlledActorForTests(system, sourceFlowConfig)
    
  val channelsFlowConfig = FlowControlConfig(name="channelsActor", actorType="com.reactor.dumbledore.prime.channels.ChannelsActor")    
  val channelsActor = FlowControlFactory.flowControlledActorForTests(system, channelsFlowConfig, ChannelArgs(feedActor, sourceActor))
  
  val time = new scala.concurrent.duration.FiniteDuration(10, java.util.concurrent.TimeUnit.SECONDS)  
  implicit val routeTestTimeout = RouteTestTimeout(time) 

  
  "The ApiService" should {
    "return Dumbledore API 1.0 at the root URL" in {
      Get() ~> 
      	apiRoute ~> check{
        responseAs[String] must contain("Dumbledore API 1.0")
      }
    }
    
    "get primetime/test" in {
      Get("/primetime/test") ~>
        apiRoute ~> check{
          responseAs[String] must contain("ok")
        }
    }
    
    "get channel feeds at /channel/feeds" in {
      Get("/channel/feeds") ~>
        apiRoute ~> check{
          responseAs[String] must contain("ok")
        }
    }
  }
  

}

