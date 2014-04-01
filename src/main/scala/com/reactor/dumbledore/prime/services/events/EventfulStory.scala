package com.reactor.dumbledore.prime.services.events

import com.fasterxml.jackson.databind.JsonNode

class EventfulStory {
  
  val `type` = "event"
  val story_type = "event"
  var title:String = null
  var state:String = null
  var state_abbrev:String = null
  var city:String = null
  var venue_name:String = null
  var venue_url:String = null
  var start_date:String = null
  var end_date:String = null
  var created_date:String = null
  var image:String = null
	
  def this(eventNode:JsonNode){
    this()
    if(eventNode.has("title"))
      title = eventNode.get("title").asText();
    if(eventNode.has("region_name"))
      state = eventNode.get("region_name").asText();
    if(eventNode.has("region_abbr"))
      state_abbrev = eventNode.get("region_abbr").asText();
    if(eventNode.has("city_name"))
      city = eventNode.get("city_name").asText();
    if(eventNode.has("venue_name"))
      venue_name = eventNode.get("venue_name").asText();
    if(eventNode.has("venue_url"))
      venue_url = eventNode.get("venue_url").asText();
    if(eventNode.has("start_time"))
      start_date = eventNode.get("start_time").asText();
    if(eventNode.has("stop_time"))
      end_date = eventNode.get("stop_time").asText();
    if(eventNode.has("created"))
	  created_date = eventNode.get("created").asText();
    if(eventNode.has("image")){
      if(eventNode.get("image").has("medium"))
    	image = eventNode.get("image").get("medium").get("url").asText();
      else if( !eventNode.get("image").isNull())
    	image = eventNode.get("image").get("url").asText();
    }
  }
  
}