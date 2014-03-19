package com.reactor.dumbledore.messaging.requests

import spray.http.HttpRequest
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.JsonNode


abstract class APIRequest {
  @transient
  val mapper = new ObjectMapper()
  
  def this(obj:Object){
    this()
    obj match{
      case s:String => create(s)
      case r:HttpRequest => create(r)
      case None => 
    }
  }
  
  def create(string:String)
  
  def create(request:HttpRequest)
  
  def getJson(jsonString:String):JsonNode = {
	var cleanRequest = jsonString.replaceAll("\\r", " ").replaceAll("\\n", " ").trim 
	mapper.readTree(cleanRequest);
  }
  
  
  def getStringR(request:HttpRequest, key:String):String = {
	request.uri.query.get(key) match{
	  case Some(value) => return value
	  case None => return null
	}
  }
  
  
  def getString(node:JsonNode, key:String):String = {
    if(node.has(key)) 
      return node.get(key).asText
 	else
      null
  }
  
  
  def getStringOptR(request:HttpRequest, key:String):Option[String] = return request.uri.query.get(key)
  
  
  def getStringOpt(node:JsonNode, key:String):Option[String] = {
    if(node.has(key))
      return Some(node.get(key).asText)
    else
      return None
  }

  
  def getBoolR(request:HttpRequest, key:String, default:Boolean = false):Boolean = {
	request.uri.query.get(key) match{
	  case Some(value) => return value.toBoolean
	  case None => return default
	}
  }
  
  
  def getBool(node:JsonNode, key:String, default:Boolean = false):Boolean = {
    if(node.has(key)) 
      return node.get(key).asBoolean
 	else
      default
  }

  
  def getIntR(request:HttpRequest, key:String, default:Int = 0):Int = {
	request.uri.query.get(key) match{
	  case Some(value) => return value.toInt
	  case None => return default
	}
  }
  
  
  def getInt(node:JsonNode, key:String, default:Int = 0):Int = {
    if(node.has(key)) 
      return node.get(key).asInt()
 	else
      default
  }
	
  
  def getDoubleR(request:HttpRequest, key:String, default:Double = 0):Double = {
	request.uri.query.get(key) match{
	  case Some(value) => return value.toDouble
	  case None => return default
	}
  }
  
  
  def getDouble(node:JsonNode, key:String, default:Double = 0):Double = {
    if(node.has(key)) 
      return node.get(key).asDouble
 	else
      default
  }
}