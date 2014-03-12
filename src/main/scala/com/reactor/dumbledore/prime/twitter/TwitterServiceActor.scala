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
    
    val stories = ListBuffer[Object]()
    
    Future.sequence(requests).onComplete{
      case Success(results) => results.map{
          result => stories += result
      	}
        reply(origin, stories)
        complete()
      
      case Failure(e) => e.printStackTrace()
        reply(origin, ListBuffer[Object]())
        complete()
    }
  }
}