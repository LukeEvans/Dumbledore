package com.reactor.dumbledore.prime.twitter

import com.reactor.patterns.pull.FlowControlActor
import com.reactor.patterns.pull.FlowControlArgs
import com.reactor.dumbledore.messaging.TwitterStoryData
import akka.actor.ActorRef
import twitter4j.Status
import com.reactor.dumbledore.prime.abstraction.Extractor
import com.reactor.dumbledore.utilities.Timer

class TwitterStoryBuilderActor(args:FlowControlArgs) extends FlowControlActor(args) {

  val extractor = new Extractor
  ready()
  
  override def preStart() = println("Starting TwitterStoryBuilder")
  override def postStop() = println("Terminated TwitterStoryBuilder")
  
  override def receive = {
    case TwitterStoryData(status, meID) => handleStoryRequest(status, meID, sender)
  }
  
  def handleStoryRequest(status:Status, meID:Long, origin:ActorRef){
    
    val story = new TwitterStory(status, meID, extractor)
    story.entityAnalysis
  
    reply(origin, story)
    complete()
  }
  
}