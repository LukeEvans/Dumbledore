package com.reactor.dumbledore.prime.channels

import com.reactor.patterns.pull.FlowControlActor
import com.reactor.patterns.pull.FlowControlArgs
import com.reactor.dumbledore.messaging.Feeds
import akka.actor.ActorRef
import com.reactor.store.MongoDB
import scala.collection.mutable.ListBuffer
import com.fasterxml.jackson.databind.JsonNode
import com.reactor.dumbledore.utilities.Tools
import com.reactor.dumbledore.messaging.FeedData
import com.reactor.dumbledore.messaging.ChannelRequestData
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.DBObject
import com.mongodb.casbah.Imports._
import com.reactor.dumbledore.prime.data.story.KCStory

class ChannelsActor(args:FlowControlArgs) extends FlowControlActor(args){
  val NEWS_DB = "reactor-news"
  val mongo = new MongoDB
  ready
  
  
  override def preStart() = println("channels actor starting")
  
  override def receive = {
    case Feeds() => getChannelFeeds(sender)
    case FeedData(data) => getChannelData(data, sender)
    case a:Any => println("Unknown request - " + a)
  }
  
  
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
  
  def getChannelData(channelData:ListBuffer[ChannelRequestData], origin:ActorRef){
    val dataArray = ListBuffer[ListBuffer[Object]]()
    
    channelData.map{
      data => 
        val dataList = ListBuffer[Object]()
        // build query
        queryMongo(data) map{
          obj =>{
            // get stories and populate set
            val story = new KCStory(obj)
            // add story set to List
            dataList += story
          }
        }
        dataArray += dataList
    }
    
    reply(origin, dataArray)
  }

  def queryMongo(data:ChannelRequestData):ListBuffer[JsonNode] = {
    val query = buildQuery(data.feed_id, data.sources)
    val dataObjects = mongo.find(query, NEWS_DB, 10)
    
    val dataNodes = ListBuffer[JsonNode]()
    dataObjects map{
      data => dataNodes += Tools.objectToJsonNode(data)
    }
    dataNodes
  }
  
  def buildQuery(feedID:String, excluded:ListBuffer[String]):DBObject = {
	("source_category" $eq feedID) ++ ("source_id" $ne excluded) ++ ("valid" $eq true)
  }
}