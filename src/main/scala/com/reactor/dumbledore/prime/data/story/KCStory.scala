package com.reactor.dumbledore.prime.data.story

import com.reactor.dumbledore.prime.entities.Entity
import com.fasterxml.jackson.databind.JsonNode
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._

class KCStory {
  	var id:String = null
	var story_type:String = null
	var entities:ListBuffer[Entity] = null
	var speech:String = null
	var valid:Boolean = false
	var source_category:String = null
	var headline:String = null
	var source_id:String = null
	var source_name:String = null
	var author:String = null
	var summary:String = null
	var pubdate:String = null
	var date:Long = 0
	var link:String = null
	var source_icon_link:String = null
	var source_twitter_handle:String = null
	var image_links:ListBuffer[String] = null
	var reviewed:Boolean = false
	var ceiling_Topic:String = null
	var related_topics:ListBuffer[String] = null
	var main_topics:ListBuffer[String] = null
	
	def this(node:JsonNode){
  	  this()
  	  id = if(node.has("_id")) node.get("_id").get("$oid").asText else null
  	  story_type = if(node.has("story_type")) node.get("story_type").asText else null
  	  speech = if(node.has("speech")) node.get("speech").asText else null
  	  valid = if(node.has("valid")) node.get("valid").asBoolean else false
  	  source_category = if(node.has("source_category")) node.get("source_category").asText else null
  	  headline = if(node.has("headline")) node.get("headline").asText else null
  	  source_id = if(node.has("source_id")) node.get("source_id").asText else null
  	  source_name = if(node.has("source_name")) node.get("source_name").asText else null
  	  author = if(node.has("author")) node.get("author").asText else null
  	  summary = if(node.has("summary")) node.get("summary").asText else null
  	  date = if(node.has("date")) node.get("date").asLong() else 0
  	  pubdate = if(node.has("pubdate")) node.get("pubdate").asText else null
  	  link = if(node.has("link")) node.get("link").asText else null
  	  source_icon_link = if(node.has("source_icon_link")) node.get("source_icon_link").asText else null
  	  source_twitter_handle = if(node.has("source_twitter_handle")) node.get("source_twitter_handle").asText else null
  	  image_links = if(node.has("image_links")) nodeToList(node.get("image_links")) else null
  	  reviewed = if(node.has("reviewed")) node.get("reviewed").asBoolean else false
  	  ceiling_Topic = if(node.has("ceiling_Topic")) node.get("ceiling_Topic").asText else null
  	  related_topics = if(node.has("related_topics")) nodeToList(node.get("related_topics")) else null
  	  main_topics = if(node.has("main_topics")) nodeToList(node.get("main_topics")) else null
  	  entities = if(node.has("entities")) nodeToEntities(node.get("entities")) else null
  	}
  	
  	def nodeToList(node:JsonNode):ListBuffer[String] = {
  	  val list = ListBuffer[String]()
  	  node.map{
  	    obj => list += obj.asText
  	  }
  	  list
  	}
  	
  	def nodeToEntities(node:JsonNode):ListBuffer[Entity] = {
  	  val list = ListBuffer[Entity]()
  	  node.map{
  	    obj => {
  	      val entity = new Entity
  	      entity.entity_name = obj.get("entity_name").asText()
  	      entity.entity_type = obj.get("entity_type").asText()
  	      entity.sentiment = obj.get("sentiment").asText()
  	      list += entity
  	    }
  	  }
  	  list
  	}
  	
  	override def toString() = {
  	  headline + " - " + author 
  	}
}