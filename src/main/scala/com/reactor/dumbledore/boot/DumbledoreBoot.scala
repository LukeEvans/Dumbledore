package com.reactor.dumbledore.boot

import com.reactor.dumbledore.listener.Listener
import com.reactor.nlp.utilities.IPTools
import com.typesafe.config.ConfigFactory
import com.reactor.dumbledore.api.ApiActor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.cluster.Cluster
import akka.cluster.routing.AdaptiveLoadBalancingRouter
import akka.cluster.routing.ClusterRouterConfig
import akka.cluster.routing.ClusterRouterSettings
import akka.io.IO
import akka.kernel.Bootable
import akka.cluster.ClusterEvent.ClusterDomainEvent
import akka.pattern.ask
import spray.can.Http
import spray.http.HttpRequest
import spray.http._
import HttpMethods._
import com.reactor.dumbledore.legacy.WinstonAPIActor
import akka.routing.RoundRobinRouter
import com.reactor.patterns.pull.FlowControlConfig
import com.reactor.patterns.pull.FlowControlFactory
import com.reactor.dumbledore.notifications.NotificationArgs
import com.reactor.dumbledore.prime.channels.ChannelArgs
import com.reactor.dumbledore.prime.twitter.TwitterArgs
import com.reactor.dumbledore.prime.twitter.TwitterBuilderArgs

class DumbledoreBoot extends Bootable {
  val ip = IPTools.getPrivateIp(); 
  println("IP: " + ip)
  
  val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=2551") 
    .withFallback(ConfigFactory.parseString("akka.cluster.roles = [dumbledore-frontend]\nakka.remote.netty.tcp.hostname=\""+ip+"\""))
    .withFallback(ConfigFactory.load("dumbledore"))

  implicit val system = ActorSystem("DumbledoreClusterSystem-0-1", config)
  
  Cluster(system) registerOnMemberUp{
    
    val winstonAPIFlowConfig = FlowControlConfig(name="winstonAPIActor", actorType="com.reactor.dumbledore.legacy.WinstonAPIActor")    
    val winstonAPIActor = FlowControlFactory.flowControlledActorForSystem(system, winstonAPIFlowConfig)
    
    val primeFlowConfig = FlowControlConfig(name="primeActor", actorType="com.reactor.dumbledore.prime.PrimeActor")
    val primeActor = FlowControlFactory.flowControlledActorForSystem(system, primeFlowConfig)
    
    val extractorFlowConfig = FlowControlConfig(name="extractorActor", actorType="com.reactor.dumbledore.prime.abstraction.ExtractionActor", parallel = 1)
    val extractorActor = FlowControlFactory.flowControlledActorForSystem(system, extractorFlowConfig)
    
    val twitterStoryBuilderFlowConfig = FlowControlConfig(name="twitterStoryBuilderActor", actorType="com.reactor.dumbledore.prime.twitter.TwitterStoryBuilderActor", parallel = 30)    
    val twitterStoryActor = FlowControlFactory.flowControlledActorForSystem(system, twitterStoryBuilderFlowConfig, TwitterBuilderArgs(extractorActor))
    
    val twitterServiceFlowConfig = FlowControlConfig(name="twitterServiceActor", actorType="com.reactor.dumbledore.prime.twitter.TwitterServiceActor", parallel = 8)    
    val twitterServiceActor = FlowControlFactory.flowControlledActorForSystem(system, twitterServiceFlowConfig, TwitterArgs(twitterStoryActor))
    
    val serviceFlowConfig = FlowControlConfig(name="serviceActor", actorType="com.reactor.dumbledore.services.ServiceActor", parallel=6)    
    val serviceActor = FlowControlFactory.flowControlledActorForSystem(system, serviceFlowConfig)
    
    val notificationFlowConfig = FlowControlConfig(name="notificationActor", actorType="com.reactor.dumbledore.notifications.NotificationActor", parallel =3)    
    val notificationActor = FlowControlFactory.flowControlledActorForSystem(system, notificationFlowConfig, NotificationArgs(serviceActor))
    
    val feedFlowConfig = FlowControlConfig(name="feedActor", actorType="com.reactor.dumbledore.prime.channels.SingleFeedActor", parallel=9)    
    val feedActor = FlowControlFactory.flowControlledActorForSystem(system, feedFlowConfig)
    
    val sourceFlowConfig = FlowControlConfig(name="sourceActor", actorType="com.reactor.dumbledore.prime.channels.sources.SourceActor", parallel=5)    
    val sourceActor = FlowControlFactory.flowControlledActorForSystem(system, sourceFlowConfig)
    
    val channelsFlowConfig = FlowControlConfig(name="channelsActor", actorType="com.reactor.dumbledore.prime.channels.ChannelsActor")    
    val channelsActor = FlowControlFactory.flowControlledActorForSystem(system, channelsFlowConfig, ChannelArgs(feedActor, sourceActor))
    
	val service = system.actorOf(Props(classOf[ApiActor], winstonAPIActor, notificationActor, channelsActor, twitterServiceActor, primeActor).withRouter(	
	  ClusterRouterConfig(AdaptiveLoadBalancingRouter(akka.cluster.routing.MixMetricsSelector), 
	  ClusterRouterSettings(
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