package com.reactor.dumbledore.prime.channels.feeds

import com.fasterxml.jackson.databind.JsonNode
import scala.collection.mutable.ListBuffer
import com.reactor.dumbledore.prime.channels.SourceInfo
import com.reactor.store.MongoDB
import scala.collection.JavaConversions._
import com.reactor.dumbledore.utilities.Tools

class Feed {
  var feed_id:String = null
  var icon:String = null
  var name:String = null
  var quality_rating = 0
  var spoken:String = null
  var sources:ListBuffer[SourceInfo] = null
  var tagline:String = null
  
  def this(node:JsonNode, mongo:MongoDB){
    this()
    feed_id = if(node.has("feed_id")) node.get("feed_id").asText() else null
    icon = if(node.has("icon")) node.get("icon").asText() else null
    name = if(node.has("name")) node.get("name").asText() else null
    quality_rating = if(node.has("quality_rating")) node.get("quality_rating").asInt else 0
    spoken = if(node.has("spoken")) node.get("spoken").asText() else null
    sources = if(node.has("sources")) getSourceInfo(node.get("sources"), mongo) else null
    tagline = if(node.has("tagline")) node.get("tagline").asText() else null
  }
  
  def getSourceInfo(sourceNode:JsonNode, mongo:MongoDB):ListBuffer[SourceInfo] = {
    val infoList = ListBuffer[SourceInfo]()
    
    sourceNode.foreach{
      source =>
        val obj = mongo.findOneSimple("source_id", source.asText(), "reactor-news-sources")
        val json = Tools.objectToJsonNode(obj)
        if(json.has("source_id") && json.has("name") && json.has("icon"))
        	infoList += SourceInfo(json.get("source_id").asText(), json.get("name").asText(), json.get("icon").asText())
    }
    infoList
  }
}