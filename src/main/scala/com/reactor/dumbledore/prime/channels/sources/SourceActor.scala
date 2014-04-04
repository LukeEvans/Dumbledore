package com.reactor.dumbledore.prime.channels.sources

import com.mongodb.DBObject
import com.mongodb.casbah.Imports._
import com.reactor.patterns.pull.FlowControlActor
import com.reactor.patterns.pull.FlowControlArgs
import com.reactor.store.MongoDB
import akka.actor.ActorRef
import scala.collection.mutable.ListBuffer
import scala.concurrent.Future
import com.fasterxml.jackson.databind.JsonNode
import scala.concurrent._
import com.reactor.dumbledore.utilities.Tools
import com.reactor.dumbledore.prime.data.ListSet
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class SourceActor(args:FlowControlArgs) extends FlowControlActor(args) {
  private val NEWS_DB = "reactor-news"
  private val mongo = new MongoDB
  ready()
  
  override def preStart() = println("SourceActor starting up...")
  override def postStop() = println("SourceActor terminated.")
  
  // Handle messages
  def receive = {
    case source_id:String => getNews(source_id, sender)
    case a:Any => println("Unknown Message - " + a)
  }

  def getNews(id:String, origin:ActorRef){
    val query = buildQuery(id)
    val dataObjects = mongo.find(query, NEWS_DB, 10)
    val futureNodes = ListBuffer[Future[JsonNode]]()
    
    dataObjects map{
      data => futureNodes += future(Tools.objectToJsonNode(data))
    }
    
    reply(origin, ListSet(id, Await.result(Future.sequence(futureNodes), atMost = 3 seconds)))
    complete()
  }
  
  def buildQuery(sourceID:String):DBObject = {
    ("source_id" $eq sourceID)
  }
}