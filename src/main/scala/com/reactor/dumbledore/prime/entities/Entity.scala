package com.reactor.dumbledore.prime.entities

import com.fasterxml.jackson.databind.JsonNode
import com.mongodb.casbah.commons.MongoDBObject

class Entity {
  var entity_name:String = null
  var entity_type:String = null
  var entity_score:Int = 0
  var sentiment:String = null

  def this(node:JsonNode){
    this()
    this.entity_type = node.path("type").asText()
    this.sentiment = node.path("sentiment").path("type").asText()
    this.entity_name = node.path("text").asText()
    
    if(node.has("entity_score"))
      this.entity_score = node.path("entity_score").asInt
    
    sanitizeEntityText()
  }
  
  def sanitizeEntityText(){
    if(this.entity_type == null)
      return;

    if(this.entity_type.equalsIgnoreCase("Organization"))
      this.entity_name = "the " + this.entity_name;
  }
  
  override def toString():String = {
    var s = "";

    s += "Text: " + entity_name + " Type: " + entity_type + " Sentiment: " + sentiment + "\n";
    System.out.println("entityString : " + s);
    return s;
  }
  
  def toDBObject() = {
    
    val obj = MongoDBObject("entity_name" -> entity_name, "entity_type" -> entity_type,
        "entity_score" -> entity_score, "sentiment" -> sentiment)
    
    obj
  }
}