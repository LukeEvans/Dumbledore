package com.reactor.dumbledore.prime

import com.reactor.patterns.pull.FlowControlActor
import com.reactor.patterns.pull.FlowControlArgs
import com.reactor.dumbledore.prime.youtube.Youtube
import com.reactor.dumbledore.messaging.requests.YoutubeRequest

class PrimeActor(args:FlowControlArgs) extends FlowControlActor(args) {

  ready()
  
  override def preStart = println("Prime Actor Starting...")
  override def postStop = println("Prime Actor Terminated...")

  override def receive = {
    case request:YoutubeRequest => 
      reply(sender, Youtube.getYoutube(request.channelID, request.number))
      complete()
    case a:Any => println("Unknown Message - " + a)
  }
  
}