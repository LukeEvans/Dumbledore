package com.reactor.dumbledore.prime.services.yelp

import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._
import com.fasterxml.jackson.databind.JsonNode

object Yelp {

  
  def searchNearby(query:String, lat:Double, lng:Double, numStories:Int):ListBuffer[Object] = {
    
	return createSet(YelpAPI.search(query, lat, lng).get("businesses"), numStories, query);
  }
  
  
  def createSet(entryNodes:JsonNode, limit:Int, entity:String):ListBuffer[Object] = {
		
    val entrySet = ListBuffer[Object]()
        
    var count = 0;
     
    entryNodes.foreach{
      node => {
        if(count >= limit)
          return entrySet
        
        val story = new YelpStory(node)
        story.entity = entity
        entrySet += story
        count += 1
      }
    } 
    
    return entrySet
  }
}