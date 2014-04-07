package com.reactor.dumbledore.utilities

import scala.collection.mutable.ListBuffer
import com.fasterxml.jackson.databind.JsonNode
import scala.collection.JavaConversions._
import com.reactor.dumbledore.prime.services.comics.ComicCard

object Conversions {

  
  def nodeToStringList(node:JsonNode):ListBuffer[String] = {
    
    val listBuffer = ListBuffer[String]()
    
    node.foreach( iNode => listBuffer += iNode.asText)
    
    return listBuffer
  }
  
  
  def nodeToObjectList(node:JsonNode):ListBuffer[Object] = {
    
    val listBuffer = ListBuffer[Object]()
    
    node.foreach( iNode => listBuffer += iNode)
    
    return listBuffer
  }
  
  
  def nodeToListBuffer[T](node:JsonNode):ListBuffer[T] = {
    
    val listBuffer = ListBuffer[T]()
    
    node.foreach{
      iNode => listBuffer += iNode.asInstanceOf[T]
    }
    
    listBuffer
  }
  
  
  def listToObjectList[T](list:ListBuffer[T]):ListBuffer[Object] = {
    
    val objectList = ListBuffer[Object]()
    
    return list.map( t => t.asInstanceOf[Object] )
  }
  
}
