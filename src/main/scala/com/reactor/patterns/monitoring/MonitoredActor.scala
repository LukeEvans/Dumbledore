package com.reactor.patterns.monitoring

import akka.actor.Actor
import com.timgroup.statsd.NonBlockingStatsDClient
import akka.actor.ActorLogging

abstract class MonitoredActor(tag:String) extends Actor with ActorLogging {

  val tags:Array[String] = Array("tag:" + tag)
  
    // Datadog client
  val statsd = new NonBlockingStatsDClient(
    "dumbledore-dev",                                                               /* prefix to any stats; may be null or empty string */
    "localhost",                                                                    /* common case: localhost */
    8125,                                                   /* port */
    tags                                                                            /* DataDog extension: Constant tags, always applied */
  )   
  
  override def postStop() {
    statsd.stop();
  }
  

}