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

class DumbledoreBoot extends Bootable {
  val ip = IPTools.getPrivateIp(); 
  println("IP: " + ip)
  
  val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=2551") 
    .withFallback(ConfigFactory.parseString("akka.cluster.roles = [dumbledore-frontend]\nakka.remote.netty.tcp.hostname=\""+ip+"\""))
    .withFallback(ConfigFactory.load("dumbledore"))

  implicit val system = ActorSystem("DumbledoreClusterSystem-0-1", config)
  
  Cluster(system) registerOnMemberUp{
    
    val winstonAPIActor = system.actorOf(Props(classOf[WinstonAPIActor]).withRouter(ClusterRouterConfig(RoundRobinRouter(), 
			ClusterRouterSettings(
			totalInstances = 100, maxInstancesPerNode = 1,
			allowLocalRoutees = true, useRole = Some("dumbledore-frontend")))),
			name = "splitMaster")
    
	val service = system.actorOf(Props(classOf[ApiActor], winstonAPIActor).withRouter(	
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