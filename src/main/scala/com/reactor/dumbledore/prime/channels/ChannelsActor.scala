package com.reactor.dumbledore.prime.channels

import com.reactor.patterns.pull.FlowControlActor
import com.reactor.patterns.pull.FlowControlArgs
import com.reactor.dumbledore.messaging.Feeds
import akka.actor.ActorRef
import com.reactor.store.MongoDB
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import com.fasterxml.jackson.databind.JsonNode
import com.reactor.dumbledore.utilities.Tools
import com.reactor.dumbledore.messaging.FeedData
import com.reactor.dumbledore.messaging.ChannelRequestData
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.DBObject
import com.mongodb.casbah.Imports._
import com.reactor.dumbledore.prime.data.story.KCStory
import com.reactor.dumbledore.messaging.SingleDataContainer
import com.reactor.patterns.pull.FlowControlArgs
import akka.pattern.ask
import akka.util.Timeout
import scala.util.Failure
import scala.util.Success
import scala.concurrent.Await
import com.reactor.dumbledore.utilities.Timer
import com.reactor.dumbledore.data.ListSet
import com.reactor.dumbledore.data.ListSetNode
import com.reactor.dumbledore.prime.channels.feeds.Feed
import spray.caching.{LruCache, Cache}

class ChannelsActor(args:ChannelArgs) extends FlowControlActor(args){
  private val NEWS_DB = "reactor-news"
  private val mongo = new MongoDB
  val singleActor = args.singleActor
  ready
  
  override def preStart() = println("channels actor starting")
  
  override def receive = {
    case Feeds() => getChannelFeeds(sender)
    case FeedData(data) => getChannelData(data, singleActor, sender)
    case a:Any => println("Unknown request - " + a)
  }
  
  /** Get list of channel feeds available from mongo */
  def getChannelFeeds(origin:ActorRef){

    val cache: Cache[ListBuffer[Feed]] = LruCache()
    def cachedOp():Future[ListBuffer[Feed]] = cache(ListBuffer[Feed]()){
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
    
    list.map{
      channelObj =>
        var json  = Tools.objectToJsonNode(channelObj)
        val feed = new Feed(json, mongo)
        feedList += feed
    }
	feedList
  }
  
  /** Get Nested array story sets */
  def getChannelData(channelData:ListBuffer[ChannelRequestData], singleActor:ActorRef, origin:ActorRef){
    implicit val timeout = Timeout(30 seconds)
    val dataArray = ListBuffer[ListSet]()
    
    val dataList = ListBuffer[Future[ListSetNode]]()
    channelData.map{
      data => dataList += (singleActor ? data).mapTo[ListSetNode]
    }
    
    val list = ListBuffer[Object]()
    val data = Await.result(Future.sequence(dataList), atMost = 3 seconds)
    data map{
      d => 
        list.clear     
        d.list map{
          node =>
            val story = new KCStory(node)
            list += story
      }
      dataArray += ListSet(d.card_id, list)
    }

    reply(origin, dataArray)
    complete()
  }
}