package com.reactor.dumbledore.services

import java.util.ArrayList
import com.reactor.dumbledore.utilities.Tools
import com.reactor.dumbledore.utilities.Location
import com.fasterxml.jackson.databind.JsonNode
import scala.collection.JavaConversions._
import scala.collection.mutable.Map

object Service {
  private val baseUrl = "http://v036.winstonapi.com"  

  def request(endpoint:String, parameters:Option[Map[String, String]]):ArrayList[Object]= {

    parameters match{
      case Some(params) => 
        val url = baseUrl + endpoint + "?" + constructParams(params)
        getData(url)
      case None =>
        getData(baseUrl + endpoint)
    }
  }
  
  private def getData(url:String):ArrayList[Object] = {
    val response = Tools.fetchURL(url)
    
    if(!response.has("data"))
      return null
      
    extractData(response.get("data"))
  }
  
  private def extractData(jsonData:JsonNode):ArrayList[Object] = {
    if(jsonData == null)
      return null;
    
    val data = new ArrayList[Object]  

    for(node <- jsonData)
      data.add(node)
    
    data
  }
  
  private def constructParams(parameters:Map[String, String]):String = {
    var paramString = ""    
    parameters.foreach(params => paramString += (params._1 + "=" + params._2 + "&"))
    //truncate extra ampersand
    return paramString.substring(0, paramString.size - 1)  
  }
}