package com.reactor.dumbledore.prime.entities

import com.fasterxml.jackson.databind.JsonNode

class Entity {
  var entity_name:String = null
  var entity_type:String = null
  var sentiment:String = null

  def this(node:JsonNode){
    this()
    this.entity_type = node.path("type").asText()
    this.sentiment = node.path("sentiment").path("type").asText()
    this.entity_name = node.path("text").asText()
    
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
}