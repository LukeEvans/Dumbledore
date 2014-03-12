package com.reactor.dumbledore.messaging.requests

import com.fasterxml.jackson.databind.ObjectMapper
import scala.collection.mutable.ListBuffer
import com.fasterxml.jackson.databind.JsonNode
import scala.collection.JavaConversions._
import spray.http.HttpRequest

case class FeedRequestData(feed_id:String, sources:ListBuffer[String])

/** Feed Request 
 */
class FeedRequest extends APIRequest {
  
  var channelList:ListBuffer[FeedRequestData] = null
  
 def this(obj:Object){
    this()
    obj match{
      case s:String => create(s)
      case r:HttpRequest => create(r)
    }
  }
  
  def create(request:HttpRequest){
    // Get requests not supported
  }
  
  def create(string:String){
    val reqJson = getJson(string)
    
    channelList = getList(reqJson.get("data"))
  }
  
  private def getList(dataNode:JsonNode):ListBuffer[FeedRequestData] = {
    if(dataNode == null)
      return null
    
    val dataList = ListBuffer[FeedRequestData]()
    dataNode.map{
      data => 
        if(data.has("feed_id") && data.has("sources"))
          dataList += FeedRequestData(data.get("feed_id").asText(), nodeToList(data.get("sources")))
    }
    return dataList
  }
  
  private def nodeToList(nodeList:JsonNode):ListBuffer[String] = {
    if(nodeList == null)
      return null
      
    val list = ListBuffer[String]()
    nodeList.map{
      node => list += node.asText()
    }
    list
  }
}

/** Channel Request
 *  
 */
class ChannelRequest extends APIRequest{

  var channelIDs:ListBuffer[String] = null
  
  def this(obj:Object){
    this()
    obj match{
      case s:String => create(s)
      case r:HttpRequest => create(r)
    }
  }
  
  override def create(request:HttpRequest){
    // Get Requests not supported
  }
  
  override def create(string:String){

    val reqJson = getJson(string)
    
	if(reqJson.has("data"))
	  channelIDs = getChannelIds(reqJson.get("data"))
  }
  
  private def getChannelIds(node:JsonNode):ListBuffer[String] = {
    val idList = ListBuffer[String]()
    
    node map (nodeString => idList += nodeString.asText())

    idList
  }
  
}

