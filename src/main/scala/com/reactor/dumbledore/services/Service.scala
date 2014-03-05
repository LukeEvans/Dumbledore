package com.reactor.dumbledore.services

import java.util.ArrayList
import com.reactor.dumbledore.utilities.Tools
import com.reactor.dumbledore.utilities.Location
import com.fasterxml.jackson.databind.JsonNode
import scala.collection.JavaConversions._
import scala.collection.mutable.Map
import scala.collection.mutable.ListBuffer
import com.reactor.dumbledore.data.ListSet

object Service {
  private val baseUrl = "http://v036.winstonapi.com"  

  def request(serviceId:String, endpoint:String, parameters:Option[Map[String, String]], ids:ListBuffer[String]):ListSet= {
		  
    parameters match{
      case Some(params) => 
        val url = baseUrl + endpoint + "?" + constructParams(params)
        ListSet(serviceId, getData(url, ids))
      case None =>
        ListSet(serviceId, getData(baseUrl + endpoint, ids))
    }
  }
  
  private def getData(url:String, ids:ListBuffer[String]):ListBuffer[Object] = {
    val response = Tools.fetchURL(url)
    
    if(!response.has("data"))
      return null
      
    extractData(response.get("data"), ids)
  }
  
  private def extractData(jsonData:JsonNode, ids:ListBuffer[String]):ListBuffer[Object] = {
    if(jsonData == null)
      return null;
    
    val data = new ListBuffer[Object]  

    for(node <- jsonData){
      if(node.has("id") && ids != null){
        if(!ids.contains(node.get("id").asText())){
          data.add(node)
        }
      }
      else{
    	data.add(node)
      }
    }   
    data
  }
  
  private def constructParams(parameters:Map[String, String]):String = {
    var paramString = ""    
    parameters.foreach(params => paramString += (params._1 + "=" + params._2 + "&"))
    //truncate extra ampersand
    return paramString.substring(0, paramString.size - 1)  
  }
}