package com.reactor.dumbledore.prime.channels

import scala.collection.mutable.ListBuffer
import com.reactor.store.MongoDB
import com.fasterxml.jackson.databind.JsonNode
import com.reactor.dumbledore.utilities.Tools
import scala.collection.JavaConversions._
import com.reactor.dumbledore.prime.channels.feeds.Feed

case class SourceInfo(source_id:String, source_name:String, source_icon:String)

object ChannelFeeds {

  def getFeeds(mongo:MongoDB):ListBuffer[Feed] = {
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
}