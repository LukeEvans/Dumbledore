package com.reactor.dumbledore.prime.abstraction

import com.reactor.patterns.pull.FlowControlActor
import com.reactor.patterns.pull.FlowControlArgs
import akka.actor.ActorRef

class ExtractionActor(args:FlowControlArgs) extends FlowControlActor(args) {
  val extractor = new Extractor
  ready()
  
  def receive = {
    case url:String => handleUrl(url, sender)
  }
  
  def handleUrl(url:String, origin:ActorRef){
    try{
      val abs = extractor.getAbstraction(url)
    
      reply(origin, abs)
      complete()      
    } catch {
      case e:Exception => e.printStackTrace()
      reply(origin, new Abstraction)
      complete()
    }
  }

}