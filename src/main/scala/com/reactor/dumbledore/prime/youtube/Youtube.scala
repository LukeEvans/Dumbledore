package com.reactor.dumbledore.prime.youtube

import scala.collection.mutable.ListBuffer
import com.reactor.dumbledore.utilities.Tools
import com.fasterxml.jackson.databind.JsonNode
import scala.collection.JavaConversions._

object Youtube {
  private val baseDataUrl = "https://www.googleapis.com/youtube/v3/";
  private val apiKey = "AIzaSyDEv9_zQNRGpw789wTE5NbnUn4IywHUR5U";
  
  def getYoutube(channelID:Option[String], number:Int):ListBuffer[Object] = {
    channelID match{
      case Some(id) => getFeedFromChannel(id, number)
      case None => mostPopularFeed(number)
    }
  }
  
  /** Get a list of videos from channel with channelID */
  private def getFeedFromChannel(id:String, number:Int):ListBuffer[Object] = {
    val response = Tools.fetchURL( baseDataUrl +
        "search?part=snippet" + 
        "&maxResults=" + number +
        "&channelId="+ id +
        "&key=" + apiKey)
    
    response.has("items") match{
      case true => return createVideoSet(response.get("items"))
      case false => ListBuffer[Object]()
    }
  }
  
  /** Get most popular video set from youtube */
  private def mostPopularFeed(number:Int):ListBuffer[Object] = {
    val response = Tools.fetchURL(baseDataUrl +
        "videos?part=statistics,snippet&chart=mostPopular&regionCode=us" +
        "&maxResults=" + number +
        "&key=" + apiKey)
    
    response.has("items") match{
      case true => return createVideoSet(response.get("items"))
      case false => ListBuffer[Object]()
    }
  }
  
  /** Create ListBuffer[Object] of videos from JsonNode */
  private def createVideoSet(node:JsonNode):ListBuffer[Object] ={
    
    val videoSet = new ListBuffer[Object]()
    
    node.foreach( videoNode => videoSet += new YoutubeVideo(videoNode))
    
    return videoSet
  }
}