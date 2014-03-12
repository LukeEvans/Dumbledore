package com.reactor.dumbledore.prime.entities

import java.util.ArrayList
import com.fasterxml.jackson.databind.JsonNode
import scala.collection.JavaConversions._

class EntityList {
  var text:String = null
  var entities = new ArrayList[Entity]
  
  def this(node:JsonNode){
    this()
    if(node.path("status").asText().equalsIgnoreCase("ok")) {
      for (eNode <- node.path("entities"))
    	entities.add(new Entity(eNode))
    }	
    else {
      System.out.println("Alchemy error: " + node.path("status").asText())
    }
  }
  
  def size() = entities.size()
  
  def fetch(i:Int) = entities.get(i)
  
  def fetchTopEntity():Entity = {
    if(entities == null || entities.isEmpty())
      return null
    
    return entities.get(0)
  }
  
  def addEntity(e:Entity) = entities.add(e)
  
}