package com.reactor.dumbledore.services

import java.util.ArrayList
import com.reactor.dumbledore.utilities.Tools
import com.reactor.dumbledore.utilities.Location
import com.fasterxml.jackson.databind.JsonNode
import scala.collection.JavaConversions._
import scala.collection.mutable.Map

object Service {
  private val baseUrl = "http://v036.winstonapi.com"  
  
  def getData(endpoint:String):ArrayList[Object] = {
    val response = Tools.fetchURL(baseUrl + endpoint)
    if(!response.has("data"))
      null
    
   extractData(response.get("data"))   
  }
  
  def getData(endpoint:String, parameters:Map[String, String]):ArrayList[Object]= {
    val response = Tools.fetchURL(baseUrl + endpoint + "?" +
    								constructParameters(parameters))
    if(!response.has("data"))
      return null
    println(response.get("responseTime").asText())
    extractData(response.get("data"))
  }
  
  private def extractData(jsonData:JsonNode):ArrayList[Object] = {
    if(jsonData == null)
      return null;
    
    val data = new ArrayList[Object]  
    println(jsonData)
    for(node <- jsonData)
      data.add(node)
    
    data
  }
  
  private def addLocationParameters(loc:Location):String = {
    if(loc == null)
      return ""
    return "lat="+loc.lat+"&long="+loc.long
  }
  
  private def constructParameters(parameters:Map[String, String]):String = {
    var parameterString = ""
    
    parameters.foreach(params => parameterString += (params._1 + "=" + params._2 +"&"))
    //truncate extra ampersand
    val params = parameterString.substring(0, parameterString.size - 1)  
    params
  }
}