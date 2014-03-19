package com.reactor.dumbledore.utilities

import scala.collection.mutable.ListBuffer
import com.fasterxml.jackson.databind.JsonNode
import scala.collection.JavaConversions._
import com.reactor.dumbledore.prime.services.comics.ComicCard

object Conversions {

  def nodeToStringList(node:JsonNode):ListBuffer[String] = {
    
    val listBuffer = new ListBuffer[String]
    node.map( iNode => listBuffer += iNode.asText)
    
    return listBuffer
  }
  
  def nodeToListBuffer[T](node:JsonNode):ListBuffer[T] = {
    val listBuffer = new ListBuffer[T]
    
    node.map{
      iNode => listBuffer += iNode.asInstanceOf[T]
    }
    
    listBuffer
  }
  
  def listToObjectList(list:ListBuffer[ComicCard]){
    val objectList = ListBuffer[Object]()
    
    list.map(t => objectList += t)
    
  }
}