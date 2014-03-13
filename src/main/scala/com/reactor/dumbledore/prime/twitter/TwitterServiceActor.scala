package com.reactor.dumbledore.prime.twitter

import com.reactor.patterns.pull.FlowControlActor
import com.reactor.patterns.pull.FlowControlArgs
import akka.actor.ActorRef
import com.reactor.dumbledore.messaging.requests.TwitterRequest
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import akka.util.Timeout
import akka.pattern.ask
import com.reactor.dumbledore.messaging.TwitterStoryData
import scala.util.Success
import scala.util.Failure

case class TwitterArgs(twitterStoryBuilderActor:ActorRef) extends FlowControlArgs{
  override def workerArgs(): FlowControlArgs ={
    val newArgs = new TwitterArgs(twitterStoryBuilderActor)
    newArgs.addMaster(master)
    return newArgs
  }
}

class TwitterServiceActor(args:TwitterArgs) extends FlowControlActor(args) {
  val twitterStoryBuilder = args.twitterStoryBuilderActor
  val twitterAPI = new TwitterAPI
  ready()
  
  override def receive = {
    case request:TwitterRequest => handleTwitterRequest(request, sender)
    case a:Any => println("TwitterServiceActor: Unknown Message Received - " + a)
  }
  
  
  def handleTwitterRequest(request:TwitterRequest, origin:ActorRef){
    implicit val timeout = Timeout(20 seconds)
    val statuses = twitterAPI.getHomeTimeLine(request.twitterToken, request.twitterSecret)
    
    val requests = ListBuffer[Future[TwitterStory]]()
    
    statuses.map(status => requests += (twitterStoryBuilder ? TwitterStoryData(status, 0)).mapTo[TwitterStory])
    
    val stories = ListBuffer[TwitterStory]()
    
    Future.sequence(requests).onComplete{
      case Success(results) => 
        println("tweets received")
        results.map{
          result => stories += result
      	}
        
        val set = shorten(8, stories)
        reply(origin, set)
        complete()
      
      case Failure(e) => e.printStackTrace()
        reply(origin, ListBuffer[Object]())
        complete()
    }
  }
  
  def shorten(size:Int, list:ListBuffer[TwitterStory]):ListBuffer[TwitterStory] = {
    
    val newList = ListBuffer[TwitterStory]()
    
    var count = 0
    var index = 0
    
    val sorted = list.sortWith((story1, story2) => story1.score > story2.score)
    
    while(count < size){
      newList += sorted(index)
      count += 1
      index += 1
    }
    
    return newList
  }
}