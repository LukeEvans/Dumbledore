package com.reactor.dumbledore.engine

import akka.actor.Actor
import akka.actor.ActorLogging

class EngineActor extends Actor with ActorLogging{

  def receive = {
    case a:Any => println(a.toString)
  }
}