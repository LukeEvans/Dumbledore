package com.reactor.dumbledore.prime.services.donations

import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._
import com.reactor.dumbledore.utilities.Tools
import com.fasterxml.jackson.databind.JsonNode
import com.reactor.dumbledore.prime.services.geolocation.GeoLocation

object DonorsChoose {
  val baseURL = "http://api.donorschoose.org/common/json_feed.html"
  val apiKey = "DONORSCHOOSE"
    
  def findProjects(lat:Double, long:Double, limit:Int):ListBuffer[Object] = {
    
    val stateString = GeoLocation.reverseStateLookup(lat, long)
    
    val response = Tools.fetchURL(baseURL + "?" +
    			"APIKey=" + apiKey +
    			"&state=" + stateString +
    			"&max=" + limit)
    			
    if(response.has("proposals")){
      
      val projects = createProjects(response.get("proposals"))
      
      return projects
    }
    else{
      
      return null
    }
  }
  
  def createProjects(nodeList:JsonNode):ListBuffer[Object] = {
    
    val projectList = ListBuffer[Object]()
    
    nodeList.foreach( node => projectList += new DonationProject(node))
    
    return projectList
  }

}