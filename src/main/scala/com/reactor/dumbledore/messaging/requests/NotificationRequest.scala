package com.reactor.dumbledore.messaging.requests

import spray.http.HttpRequest
import scala.collection.mutable.ListBuffer
import org.codehaus.jackson.map.ObjectMapper
import scala.collection.JavaConversions._
import com.reactor.dumbledore.notifications.request.Request
import com.reactor.prime.user.UserCredentials
import com.fasterxml.jackson.databind.JsonNode

class NotificationRequest extends APIRequest {
  
  var udid:String = ""
  var lat:Double = 0.0
  var long:Double = 0.0
  var time:RequestTime = null
  var dev:Boolean = false
  var serviceRequest:ListBuffer[Request] = null
  
  def this(obj:Object){
    this()
    obj match{
      case s:String => create(s)
      case r:HttpRequest => create(r)
    }
  }
  
  def create(request:HttpRequest){
    
    udid = if(request.uri.query.get("udid") != None) request.uri.query.get("udid").get else null
    if(request.uri.query.get("lat") != None) 
      lat = request.uri.query.get("lat").get.toDouble
    if(request.uri.query.get("long") != None) 
      long = request.uri.query.get("long").get.toDouble
    if(request.uri.query.get("timezone_offset") != None)
      time = new RequestTime(request.uri.query.get("timezone_offset").get)
    if(request.uri.query.get("dev") != None)
      dev = request.uri.query.get("dev").get.toBoolean
  }
  
  def create(string:String){

    val reqJson = getJson(string)
    
    if(reqJson.has("udid"))
      udid = reqJson.get("udid").asText
    if(reqJson.has("lat"))
      lat = reqJson.get("lat").asDouble()
    if(reqJson.has("long"))
      long = reqJson.get("long").asDouble()
    if(reqJson.has("timezone_offset"))
      time = new RequestTime(reqJson.get("timezone_offset").asText())
    if(reqJson.has("dev"))
      dev = reqJson.get("dev").asBoolean()
    if(reqJson.has("service_request"))
      serviceRequest = getServiceRequest(reqJson.get("service_request"))
  }
  
  /** Create UserCredentials from request parameters
   */
  def getUserCredentials():UserCredentials = {
        return new UserCredentials(udid)
    			.setLocation(lat, long)
  }
  
  /** Grab List of Services in json and ids of stories to ignore
  */
  private def getServiceRequest(nodeList:JsonNode):ListBuffer[Request] = {
    val serviceRequests = ListBuffer[Request]()
    
    for(node <- nodeList)
    	serviceRequests.add(new Request(node))
    serviceRequests
  }
}