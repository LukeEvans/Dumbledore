package com.reactor.dumbledore.boot

import com.reactor.dumbledore.listener.Listener
import com.reactor.nlp.utilities.IPTools
import com.typesafe.config.ConfigFactory
import com.reactor.dumbledore.api.ApiActor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.cluster.Cluster
import akka.cluster.routing.AdaptiveLoadBalancingPool
import akka.cluster.routing.ClusterRouterConfig
import akka.cluster.routing.ClusterRouterPool
import akka.cluster.routing.ClusterRouterSettings
import akka.cluster.routing.ClusterRouterPoolSettings
import akka.io.IO
import akka.kernel.Bootable
import akka.cluster.ClusterEvent.ClusterDomainEvent
import akka.pattern.ask
import spray.can.Http
import spray.http.HttpRequest
import spray.http._
import HttpMethods._
import akka.routing.RoundRobinRouter
import com.reactor.patterns.pull.FlowControlConfig
import com.reactor.patterns.pull.FlowControlFactory
import com.reactor.dumbledore.prime.notifications.NotificationArgs
import com.reactor.dumbledore.prime.channels.ChannelArgs
import com.reactor.dumbledore.prime.services.twitter.TwitterArgs
import com.reactor.dumbledore.prime.services.twitter.TwitterBuilderArgs
import com.reactor.dumbledore.prime.PrimeActorArgs

class DumbledoreBoot extends Bootable{
  val ip = IPTools.getPrivateIp(); 
  println("IP: " + ip)
  
  val config = ConfigFactory.parseString("akka.remote.netty.tcp.port=2551") 
    .withFallback(ConfigFactory.parseString("akka.cluster.roles = [dumbledore-frontend]\nakka.remote.netty.tcp.hostname=\""+ip+"\""))
    .withFallback(ConfigFactory.load("dumbledore"))

  implicit val system = ActorSystem("DumbledoreClusterSystem-0-1", config)
  
  Cluster(system) registerOnMemberUp{
    
    val winstonAPIFlowConfig = FlowControlConfig(name="winstonAPIActor", actorType="com.reactor.dumbledore.prime.legacy.WinstonAPIActor")    
    val winstonAPIActor = FlowControlFactory.flowControlledActorForSystem(system, winstonAPIFlowConfig)
    
    
    val extractorFlowConfig = FlowControlConfig(name="extractorActor", actorType="com.reactor.dumbledore.prime.abstraction.ExtractionActor", parallel = 20)
    val extractorActor = FlowControlFactory.flowControlledActorForSystem(system, extractorFlowConfig)
    
    
    val twitterStoryBuilderFlowConfig = FlowControlConfig(name="twitterStoryBuilderActor", actorType="com.reactor.dumbledore.prime.services.twitter.TwitterStoryBuilderActor", parallel = 30)    
    val twitterStoryActor = FlowControlFactory.flowControlledActorForSystem(system, twitterStoryBuilderFlowConfig, TwitterBuilderArgs(extractorActor))
    
    
    val twitterServiceFlowConfig = FlowControlConfig(name="twitterServiceActor", actorType="com.reactor.dumbledore.prime.services.twitter.TwitterServiceActor", parallel = 8)    
    val twitterServiceActor = FlowControlFactory.flowControlledActorForSystem(system, twitterServiceFlowConfig, TwitterArgs(twitterStoryActor))
    
    
    val serviceFlowConfig = FlowControlConfig(name="serviceActor", actorType="com.reactor.dumbledore.prime.services.ServiceActor", parallel=20)    
    val serviceActor = FlowControlFactory.flowControlledActorForSystem(system, serviceFlowConfig)
    
    
    val notificationFlowConfig = FlowControlConfig(name="notificationActor", actorType="com.reactor.dumbledore.prime.notifications.NotificationActor", parallel =20)    
    val notificationActor = FlowControlFactory.flowControlledActorForSystem(system, notificationFlowConfig, NotificationArgs(serviceActor))
    
    
    val feedFlowConfig = FlowControlConfig(name="feedActor", actorType="com.reactor.dumbledore.prime.channels.SingleFeedActor", parallel=400)    
    val feedActor = FlowControlFactory.flowControlledActorForSystem(system, feedFlowConfig)
    
    
    val sourceFlowConfig = FlowControlConfig(name="sourceActor", actorType="com.reactor.dumbledore.prime.channels.sources.SourceActor", parallel=20)    
    val sourceActor = FlowControlFactory.flowControlledActorForSystem(system, sourceFlowConfig)
    
    
    val channelsFlowConfig = FlowControlConfig(name="channelsActor", actorType="com.reactor.dumbledore.prime.channels.ChannelsActor", parallel=20)    
    val channelsActor = FlowControlFactory.flowControlledActorForSystem(system, channelsFlowConfig, ChannelArgs(feedActor, sourceActor))
    
    
    val entFlowConfig = FlowControlConfig(name="entActor", actorType="com.reactor.dumbledore.prime.entertainment.EntertainmentActor", parallel=50)    
    val entActor = FlowControlFactory.flowControlledActorForSystem(system, entFlowConfig)
    
    
    val rankFlowConfig = FlowControlConfig(name="rankActor", actorType="com.reactor.dumbledore.prime.rank.SetRankerActor", parallel=50)    
    val rankActor = FlowControlFactory.flowControlledActorForSystem(system, rankFlowConfig)
    
    
    val primeFlowConfig = FlowControlConfig(name="primeActor", actorType="com.reactor.dumbledore.prime.PrimeActor", parallel=50)
    val primeActor = FlowControlFactory.flowControlledActorForSystem(system, primeFlowConfig, PrimeActorArgs(channelsActor, notificationActor, entActor, rankActor))   
    
    
	val service = system.actorOf(Props(classOf[ApiActor], winstonAPIActor, notificationActor, channelsActor, twitterServiceActor, primeActor).withRouter(	
	  ClusterRouterPool(AdaptiveLoadBalancingPool(akka.cluster.routing.MixMetricsSelector), 
	  ClusterRouterPoolSettings(
   	  totalInstances = 100, maxInstancesPerNode = 1,
   	  allowLocalRoutees = true, useRole = Some("dumbledore-frontend")))),
   	  name = "serviceRouter")
  
   	IO(Http) ! Http.Bind(service, interface = "0.0.0.0", port = 8080)
  }
  
  def startup = Cluster(system).subscribe(system.actorOf(Props(classOf[Listener], system), name = "clusterListener"), classOf[ClusterDomainEvent])
  
  def shutdown = system.shutdown  
}

object DumbledoreBoot{
  
  def main(args:Array[String]) = {
    val api = new DumbledoreBoot
    api.startup
  }
}