package com.reactor.dumbledore.messaging.requests

import spray.http.HttpRequest
import scala.collection.mutable.ListBuffer
import org.codehaus.jackson.map.ObjectMapper
import scala.collection.JavaConversions._
import com.reactor.dumbledore.notifications.request.Request
import com.reactor.prime.user.UserCredentials
import com.fasterxml.jackson.databind.JsonNode

class NotificationRequest(obj:Object) extends APIRequest(obj) {
  
  var udid:String = _
  var timezone_offset:Int = _
  var lat:Double = _
  var long:Double = _
  var time:RequestTime = _
  var dev:Boolean = _
  var serviceRequest:ListBuffer[Request] = _
  
  def this(primeReq:PrimeRequest){
    this(None)
    udid = primeReq.udid
    timezone_offset = primeReq.timezone_offset
    lat = primeReq.lat
    long = primeReq.long
    time = primeReq.time
    dev = primeReq.dev
    serviceRequest = primeReq.notificationsRequests
  }
  
  def create(request:HttpRequest){
    
    udid = getStringR(request, "udid")
    timezone_offset = getIntR(request, "timezone_offset")
    lat = getDoubleR(request, "lat")
    long = getDoubleR(request, "long")
    time = new RequestTime(getStringR(request, "timezone_offset"))
    dev = getBoolR(request, "dev")
  }
  
  def create(string:String){

    val json = getJson(string)
    
    udid = getString(json, "udid")
    timezone_offset = getInt(json, "timezone_offset")
    lat = getDouble(json, "lat")
    long = getDouble(json, "long")
    time = new RequestTime(getString(json, "timezone_offset"))
    dev = getBool(json, "dev")
    
    if(json.has("service_request"))
      serviceRequest = getServiceRequest(json.get("service_request"))
  }
  
  /** Create UserCredentials from request parameters
   */
  def getUserCredentials():UserCredentials = {
    return new UserCredentials(udid)
    	.setLocation(lat, long)
    	.setTzOffset(timezone_offset)
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