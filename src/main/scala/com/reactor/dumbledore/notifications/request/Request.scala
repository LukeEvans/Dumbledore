package com.reactor.dumbledore.notifications.request

import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._
import com.fasterxml.jackson.databind.JsonNode


class Request {
  var id:String = null
  var cards:ListBuffer[String] = null
  def this(node:JsonNode){
    this()
    id = node.get("id").asText()
    cards = getCardIDs(node.get("cards"))
  }
  
  private def getCardIDs(nodeList:JsonNode):ListBuffer[String] = {
    val ids = ListBuffer[String]()
    
    for(node <- nodeList)
      ids.add(node.asText)
    
    ids
  }
}