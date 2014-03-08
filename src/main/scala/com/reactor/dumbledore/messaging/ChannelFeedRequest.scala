package com.reactor.dumbledore.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import spray.http.HttpRequest

class ChannelFeedRequest {
  @transient
  val mapper = new ObjectMapper()
  var clearCache:Boolean = false
  
  def this(request:HttpRequest){
    this()
    clearCache = if(request.uri.query.get("clearCache") != None) request.uri.query.get("clearCache").get.toBoolean else false
  }
  
  def this(request:String){
    this()
    var cleanRequest = request.replaceAll("\\r", " ").replaceAll("\\n", " ").trim
    val reqJson = mapper.readTree(cleanRequest);	
    
    if(reqJson.has("clearCache"))
      clearCache = reqJson.get("clearCache").asBoolean
  }
}