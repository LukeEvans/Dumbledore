package com.reactor.dumbledore.prime.itunes

import scala.collection.mutable.ListBuffer
import com.reactor.dumbledore.prime.cards.Card
import com.fasterxml.jackson.databind.JsonNode
import scala.collection.JavaConversions._
import com.reactor.dumbledore.utilities.Tools

class ItunesMovie{
  
  var id:String = null
  var category:String = null
  var description:String = null
  var name:String = null
  var link:String = null
  var image:String = null
  var artist:String = null
  var rating:String = null
  var date:String = null
  var story_type:String = "itunes_movie"
  
  def this(node:JsonNode){
    this()
    try{
      
      if(node.has("id"))
        id = node.get("id").get("attributes").get("im:id").asText()
      if(node.has("category"))
    	category = node.get("category").get("attributes").get("label").asText()
      if(node.has("summary"))
    	description = node.get("summary").get("label").asText()
      if(node.has("im:name"))
    	name = node.get("im:name").get("label").asText()
      if(node.has("link"))
    	link = node.get("link").get(0).get("attributes").get("href").asText()
      if(node.has("im:image") && node.get("im:image").size() > 0)
    	image = node.get("im:image").get(node.get("im:image").size() - 1).get("label").asText()
      if(node.has("im:artist"))
    	artist = node.get("im:artist").get("label").asText()
      if(node.has("im:releaseDate"))
    	date = node.get("im:releaseDate").get("label").asText()
    
    } catch{
      
      case e:Exception => e.printStackTrace()
    }
  }
}

object Itunes {
  val baseRSSURL = "https://itunes.apple.com/us/rss/"  
    
  def getTopMovies(limit: Int):ListBuffer[Object] = {
    
    val response = Tools.fetchURL(baseRSSURL + "topmovies/limit=" + limit + "/json")
    
    if(response.get("feed").has("entry"))
      return extractData(response.get("feed").get("entry"), (new ItunesMovie(_)))
    else
      return ListBuffer[Object]()
  }
  
  
  def extractData(dataNode:JsonNode, constructor:(JsonNode) => Object):ListBuffer[Object] = {
    val cards = ListBuffer[Object]()
    
    for(node <- dataNode){
    	cards.add(constructor(node))
    }
    
    cards
  }
  
}