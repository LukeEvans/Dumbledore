package com.reactor.dumbledore.prime.requests

import com.reactor.dumbledore.messaging.Message
import org.codehaus.jackson.map.ObjectMapper
import spray.http.HttpRequest

case class ChannelRequest() extends Message {
  @transient
  val mapper = new ObjectMapper()
  
  var q:String = null
  var topic:String = null
  var source:String = null
  var UDID:String = null
  var entities:String = null
  
  def this(request:HttpRequest){
    this()
    if(request.uri.query.get("q")!= None) 
      q = request.uri.query.get("q").get
    if(request.uri.query.get("topic") != None)
      topic = request.uri.query.get("topic").get
    if(request.uri.query.get("source") != None)
      source = request.uri.query.get("source").get
    if(request.uri.query.get("UDID") != None)
      UDID = request.uri.query.get("UDID").get
    if(request.uri.query.get("entities") != None)
      entities = request.uri.query.get("entities").get
  }
  
  def this(request:String){
    this()
    var cleanRequest = request.replaceAll("\\r", " ").replaceAll("\\n", " ").trim
    val reqJson = mapper.readTree(cleanRequest);
    
    if(reqJson.has("q"))
      q = reqJson.get("q").asText 
    if(reqJson.has("topic"))
      topic = reqJson.get("topic").asText
    if(reqJson.has("source"))
      source = reqJson.get("source").asText 
    if(reqJson.has("UDID"))
      UDID = reqJson.get("UDID").asText
    if(reqJson.has("entities"))
      entities = reqJson.get("entities").asText 
  }
}