package com.reactor.dumbledore.prime.services.events

import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._
import com.reactor.dumbledore.utilities.Tools

object Eventful {

  val baseUrl = "http://api.eventful.com/json/events/"
  val apiKey = "t4d5HJTM57Ts66ws"
    
  def searchNearby(lat:Double, lng:Double):ListBuffer[Object] = {
    val queryUrl = baseUrl + "search" + "?where="+lat.toString()+","+lng.toString()+"&within=20&app_key="+apiKey; 
    return createStorySet(queryUrl);
  }	
	
  private def createStorySet(url:String):ListBuffer[Object] = {
    val responseNode = Tools.fetchURL(url);
    val set = new ListBuffer[Object]();
		
    if(!responseNode.get("events").has("event"))
      return set;
		
    val nodeSet = responseNode.get("events").get("event");
        
    nodeSet.foreach( storyNode => set.add(new EventfulStory(storyNode)))
        	             
    return set;
  }
  
}