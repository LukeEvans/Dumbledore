package com.reactor.dumbledore.prime.twitter

import com.reactor.patterns.pull.FlowControlActor
import com.reactor.patterns.pull.FlowControlArgs
import com.reactor.dumbledore.messaging.TwitterStoryData
import akka.actor.ActorRef
import twitter4j.Status
import com.reactor.dumbledore.prime.abstraction.Extractor
import com.reactor.dumbledore.utilities.Timer
import com.reactor.dumbledore.prime.abstraction.Abstraction
import akka.pattern.AskTimeoutException
import akka.pattern.ask
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import akka.util.Timeout
import scala.util.Success
import scala.util.Failure

case class TwitterBuilderArgs(extractorActor:ActorRef) extends FlowControlArgs{
  override def workerArgs(): FlowControlArgs ={
    val newArgs = new TwitterBuilderArgs(extractorActor)
    newArgs.addMaster(master)
    return newArgs
  }
}

class TwitterStoryBuilderActor(args:TwitterBuilderArgs) extends FlowControlActor(args) {

  val extrActor = args.extractorActor
  val extractor = new Extractor
  ready()
  
  override def preStart() = println("Starting TwitterStoryBuilder")
  override def postStop() = println("Terminated TwitterStoryBuilder")
  
  override def receive = {
    case TwitterStoryData(status, meID) => handleStoryRequest(status, meID, sender)
  }
  
  def handleStoryRequest(status:Status, meID:Long, origin:ActorRef){
    implicit val timeout = Timeout(2 seconds)
    
    val story = new TwitterStory(status, meID, extractor)
    
    if(story.header.equalsIgnoreCase("link")){
      val linkAbstract = (extrActor ? story.url ).mapTo[Abstraction]
      linkAbstract .onComplete{
        case Success(abs) => 
          println("success")
          story.setLinkData(abs)
          story.entityAnalysis
          story.calcScore
  
          reply(origin, story)
          complete()
        case Failure(e) => 
          e.printStackTrace()
          story.entityAnalysis
          story.calcScore
          reply(origin, story)
      }
    }
    else{
    
      story.entityAnalysis
      story.calcScore
  
      reply(origin, story)
      complete()
    }
  }
  
}