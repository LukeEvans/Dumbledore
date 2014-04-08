package com.reactor.dumbledore.prime.services.twitter

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Failure
import scala.util.Success
import com.reactor.dumbledore.messaging.TwitterStoryData
import com.reactor.dumbledore.messaging.requests.TwitterRequest
import com.reactor.patterns.pull.FlowControlActor
import com.reactor.patterns.pull.FlowControlArgs
import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.reactor.store.MongoDB
import com.reactor.dumbledore.utilities.Tools
import com.reactor.dumbledore.utilities.Conversions


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
   
    
    TwitterMongoCache.checkCache(request.twitterToken) match{
      case Some(cachedSet) =>
        reply(origin, cachedSet)
        complete()
        return
      case None => // Move on to twitter logic
    }

    
    val statuses = twitterAPI.getHomeTimeLine(20, request.twitterToken, request.twitterSecret)
    
    val requests = ListBuffer[Future[TwitterStory]]()
    
    statuses.foreach(status => requests += (twitterStoryBuilder ? TwitterStoryData(status, 0)).mapTo[TwitterStory])
    
    val stories = ListBuffer[TwitterStory]()
    
    Future.sequence(requests).onComplete{
      case Success(results) => 

        results.foreach( result => stories += result )
        
        val set = shorten(8, stories)
        
        TwitterMongoCache.cache(request.twitterToken, set)
        
        reply(origin, set)
        complete()
      
      case Failure(e) => e.printStackTrace()
        reply(origin, ListBuffer[Object]())
        complete()
    }
  }
  
  private def shorten(size:Int, list:ListBuffer[TwitterStory]):ListBuffer[TwitterStory] = {
    
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