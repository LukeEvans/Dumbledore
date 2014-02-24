package com.reactor.dumbledore.messaging

import com.fasterxml.jackson.databind.JsonNode
import spray.http.HttpRequest
import com.fasterxml.jackson.databind.ObjectMapper

class NotificationRequest extends Message {
  @transient
  val mapper = new ObjectMapper()
  
  var udid:String = ""
  var lat:Double = 0.0
  var long:Double = 0.0
  var time:RequestTime = null
  
  def this(request:HttpRequest){
    this()
    udid = if(request.uri.query.get("udid") != None) request.uri.query.get("udid").get else null
    if(request.uri.query.get("lat") != None) 
      lat = request.uri.query.get("lat").get.toDouble
    if(request.uri.query.get("long") != None) 
      long = request.uri.query.get("long").get.toDouble
    if(request.uri.query.get("timezone_offset") != None)
      time = new RequestTime(request.uri.query.get("timezone_offset").get)
  }
  
  def this(request:String){
    this()
    var cleanRequest = request.replaceAll("\\r", " ").replaceAll("\\n", " ").trim
    val reqJson = mapper.readTree(cleanRequest);	
    
    if(reqJson.has("udid"))
      udid = reqJson.get("udid").asText
    if(reqJson.has("lat"))
      lat = reqJson.get("lat").asDouble()
    if(reqJson.has("long"))
      long = reqJson.get("long").asDouble()
    if(reqJson.has("timezone_offset"))
      time = new RequestTime(reqJson.get("timezone_offset").asText())
  }
}