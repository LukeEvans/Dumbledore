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

class ChannelsActor(args:ChannelArgs) extends FlowControlActor(args){
  private val NEWS_DB = "reactor-news"
  private val mongo = new MongoDB
  val singleActor = args.singleActor//new SingleChannelActor(new FlowControlArgs())
  ready
  override def preStart() = println("channels actor starting")
  
  override def receive = {
    case Feeds() => getChannelFeeds(sender)
    case FeedData(data) => getChannelData(data, singleActor, sender)
    case a:Any => println("Unknown request - " + a)
  }
  
  /** Get list of channel feeds available from mongo */
  def getChannelFeeds(origin:ActorRef){
    val list = mongo.findAll("reactor-news-feeds")
    val jsonList = ListBuffer[JsonNode]()
    
    list.map{
      channelObj =>
        jsonList += Tools.objectToJsonNode(channelObj)
    }
    
    reply(origin, jsonList)
    complete()
  }
  
  /** Get Nested array story sets */
  def getChannelData(channelData:ListBuffer[ChannelRequestData], singleActor:ActorRef, origin:ActorRef){
    implicit val timeout = Timeout(30 seconds)
    val dataArray = ListBuffer[ListBuffer[Object]]()
    
    val dataList = ListBuffer[Future[ListBuffer[JsonNode]]]()
    channelData.map{
      data => dataList += (singleActor ? data).mapTo[ListBuffer[JsonNode]]
    }
    
    val list = ListBuffer[Object]()
    val data = Await.result(Future.sequence(dataList), atMost = 3 seconds)
    data map{
      d => 
        list.clear     
        d map{
          node =>
            val story = new KCStory(node)
            list += story
      }
      dataArray += list
    }

    reply(origin, dataArray)
    complete()
  }
}