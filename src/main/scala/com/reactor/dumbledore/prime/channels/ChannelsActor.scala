package com.reactor.dumbledore.prime.channels

import scala.collection.mutable.ListBuffer
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.util.Failure
import scala.util.Success

import com.fasterxml.jackson.databind.JsonNode
import com.reactor.dumbledore.data.ListSet
import com.reactor.dumbledore.messaging.FeedData
import com.reactor.dumbledore.messaging.Feeds
import com.reactor.dumbledore.messaging.SourceData
import com.reactor.dumbledore.messaging.requests.FeedRequestData
import com.reactor.dumbledore.prime.channels.feeds.Feed
import com.reactor.dumbledore.prime.data.story.KCStory
import com.reactor.dumbledore.utilities.Tools
import com.reactor.patterns.pull.FlowControlActor
import com.reactor.patterns.pull.FlowControlArgs
import com.reactor.store.MongoDB

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import spray.caching.Cache
import spray.caching.LruCache
import spray.caching.ValueMagnet.fromAny

case class ChannelArgs(feedActor:ActorRef, sourceActor:ActorRef) extends FlowControlArgs {
  
  override def workerArgs(): FlowControlArgs ={
    val newArgs = new ChannelArgs(feedActor, sourceActor)
    newArgs.addMaster(master)
    return newArgs
  }
}

class ChannelsActor(args:ChannelArgs) extends FlowControlActor(args){
  private val NEWS_DB = "reactor-news"
  private val mongo = new MongoDB
  private val cache:Cache[ListBuffer[Feed]] = LruCache()
  private val feedActor = args.feedActor
  private val sourceActor = args.sourceActor
  ready
  
  override def preStart() = println("channels actor starting")
  
  
  /** Message Handling
   */
  override def receive = {
    
    case Feeds(clear) => getChannelFeeds(clear, sender)
    
    case FeedData(data) => getChannelData(data, sender)
    
    case SourceData(data) => getSourceData(data, sender)
    
    case a:Any => println("Unknown request - " + a)
  
  }
  
  
  /** Get list of channel feeds available from mongo 
   */
  def getChannelFeeds(clearCache:Boolean, origin:ActorRef){
	if(clearCache)
	  cache.clear
	  
    val key = "some key"
    def cachedOp():Future[ListBuffer[Feed]] = cache(key){
      getFeeds()
    }
    val futureList = cachedOp()
    
    val feedList = Await.result(futureList, atMost = 10.seconds)
  
    reply(origin, feedList)
    complete()
  }
  
  def getFeeds():ListBuffer[Feed] = {
	val list = mongo.findAll("reactor-news-feeds")
    val feedList = ListBuffer[Feed]()
    
    list.foreach{
      channelObj =>
        var json  = Tools.objectToJsonNode(channelObj)
        val feed = new Feed(json, mongo)
        feedList += feed
    }
	feedList
  }
  
  /** Get Nested array story sets */
  def getChannelData(channelData:ListBuffer[FeedRequestData], origin:ActorRef){
    implicit val timeout = Timeout(30 seconds)
    val dataArray = ListBuffer[ListSet[Object]]()
    
    val dataList = ListBuffer[Future[ListSet[JsonNode]]]()
    channelData.foreach{
      data => dataList += (feedActor ? data).mapTo[ListSet[JsonNode]]
    }
    
    val list = ListBuffer[Object]()
    val data = Await.result(Future.sequence(dataList), atMost = 3 seconds)
    data map{
      d => 
        list.clear     
        d.set_data map{
          node =>
            val story = new KCStory(node)
            list += story
      }
        
      dataArray += ListSet(d.card_id, 20, list.clone)
    }

    reply(origin, dataArray)
    complete()
  }
  
  def getSourceData(sources:ListBuffer[String], origin:ActorRef){
    implicit val timeout = Timeout(10 seconds)
    
    val dataArray = ListBuffer[ListSet[Object]]()
    val dataList = ListBuffer[Future[ListSet[Object]]]()
    
    sources.foreach{
      source_id =>
        dataList += (sourceActor ? source_id).mapTo[ListSet[Object]]
    }
    
    Future.sequence(dataList) onComplete{
      case Success(list) =>
        
        list.foreach{
          data => dataArray += data
        }
        reply(origin, dataArray)
        complete()
        
      case Failure(error) => 
        println("Failure - " + error)
        complete()
    }    
  }
}