package com.reactor.dumbledore.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import scala.collection.mutable.ListBuffer
import com.fasterxml.jackson.databind.JsonNode
import scala.collection.JavaConversions._

case class ChannelRequestData(feed_id:String, sources:ListBuffer[String])

class ChannelRequest {
  @transient
  val mapper = new ObjectMapper()
  
  var channelList:ListBuffer[ChannelRequestData] = null
  
  def this(request:String){
    this()
    var cleanRequest = request.replaceAll("\\r", " ").replaceAll("\\n", " ").trim
    val reqJson = mapper.readTree(cleanRequest);
    
    channelList = getList(reqJson.get("data"))
  }
  
  def getList(dataNode:JsonNode):ListBuffer[ChannelRequestData] = {
    if(dataNode == null)
      return null
    
    val dataList = ListBuffer[ChannelRequestData]()
    dataNode.map{
      data => 
        if(data.has("feed_id") && data.has("sources"))
          dataList += ChannelRequestData(data.get("feed_id").asText(), nodeToList(data.get("sources")))
    }
    return dataList
  }
  
  def nodeToList(nodeList:JsonNode):ListBuffer[String] = {
    if(nodeList == null)
      return null
      
    val list = ListBuffer[String]()
    nodeList.map{
      node => list += node.asText()
    }
    list
  }
}

