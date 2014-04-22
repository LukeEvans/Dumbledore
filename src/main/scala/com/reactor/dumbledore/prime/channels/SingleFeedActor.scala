package com.reactor.dumbledore.prime.channels

import scala.collection.mutable.ListBuffer
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import com.fasterxml.jackson.databind.JsonNode
import com.mongodb.DBObject
import com.mongodb.casbah.Imports._
import com.reactor.dumbledore.messaging.requests.FeedRequestData
import com.reactor.dumbledore.utilities.Tools
import com.reactor.patterns.pull.FlowControlActor
import com.reactor.patterns.pull.FlowControlArgs
import com.reactor.store.MongoDB
import akka.actor.ActorRef
import scala.util.Success
import scala.util.Failure
import com.reactor.dumbledore.prime.data.ListSet

class SingleFeedActor(args:FlowControlArgs) extends FlowControlActor(args) {
  private val NEWS_DB = "reactor-news"
  private val mongo = new MongoDB
  ready
  
  def receive() = {
    
    case data:FeedRequestData =>  
      processChannel(data, sender)
      
    case a:Any => println("Unknown message - " + a)
  }
  
  def processChannel(data:FeedRequestData, origin:ActorRef){
    reply(origin, queryMongo(data))
    complete()
  }
  
  /** Query mongo for list of stories */
  private def queryMongo(data:FeedRequestData):ListSet[JsonNode] = {
    val query = buildQuery(data.feed_id, data.sources)
    val dataObjects = mongo.find(query, NEWS_DB, 10)
    val futureNodes = ListBuffer[Future[JsonNode]]()
    
    dataObjects map{
      data => futureNodes += future{ Tools.objectToJsonNode(data)}//Tools.objectToJsonNode(data)
    }
    
    ListSet(data.feed_id, Await.result(Future.sequence(futureNodes), atMost = 1 seconds))
  }
  
  /** Build News Set mongo query */  
  private def buildQuery(feedID:String, excludedSources:ListBuffer[String]):DBObject = {
    ("source_category" $eq feedID) ++ ("valid" $eq true) ++ exclusionQuery(excludedSources)
  }
  
  private def exclusionQuery(sources:ListBuffer[String]):DBObject  = {
    if(sources == null) return new BasicDBObject
    
    sources.size match{
      case 0 => return new BasicDBObject
      case 1 => return "source_id" $ne sources(0) 
      case s:Int => return $and("source_id" $ne sources(0), exclusionQuery(sources.tail) )
    }
  }
}
