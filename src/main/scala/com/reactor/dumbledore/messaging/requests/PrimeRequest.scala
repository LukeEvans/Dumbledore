package com.reactor.dumbledore.messaging.requests

import scala.collection.mutable.ListBuffer
import com.reactor.dumbledore.prime.notifications.request.Request
import spray.http.HttpRequest
import com.reactor.prime.user.UserCredentials
import com.fasterxml.jackson.databind.JsonNode
import scala.collection.JavaConversions._
import com.reactor.dumbledore.utilities.Conversions

class PrimeRequest(obj:Object) extends APIRequest(obj) {
  
  var udid:String = _
  var timezone_offset:Int = _
  var lat:Double = _
  var long:Double = _
  var time:RequestTime = _
  var dev:Boolean = _
  var all:Boolean = _
  var notificationsRequests:ListBuffer[Request] = _
  var feedRequests:ListBuffer[FeedRequestData] = _
  var entertainmentRequests:ListBuffer[Request] = _
  var socialRequests:ListBuffer[Request] = _
  
  def create(request:HttpRequest){
    
    udid = getStringR(request, "udid")
    timezone_offset = getIntR(request, "timezone_offset")
    lat = getDoubleR(request, "lat")
    long = getDoubleR(request, "long")
    time = new RequestTime(getStringR(request, "timezone_offset"))
    dev = getBoolR(request, "dev")
    all = getBoolR(request, "all")
  }
  
  def create(string:String){

    val json = getJson(string)
    
    udid = getString(json, "udid")
    timezone_offset = getInt(json, "timezone_offset")
    lat = getDouble(json, "lat")
    long = getDouble(json, "long")
    time = new RequestTime(getString(json, "timezone_offset"))
    dev = getBool(json, "dev")
    all = getBool(json, "all")
    
    if(json.has("notifications"))
      notificationsRequests = getRequests(json.get("notifications"))
    if(json.has("feeds"))
      feedRequests = getFeeds(json.get("feeds"))
    if(json.has("entertainment"))
      entertainmentRequests = getRequests(json.get("entertainment"))
    if(json.has("social"))
      socialRequests = getRequests(json.get("social"))
  }
  
  /** Create UserCredentials from request parameters
   */
  def getUserCredentials():UserCredentials = {
        return new UserCredentials(udid)
    			.setLocation(lat, long)
  }
  
  /** Grab List of Services in json and ids of stories to ignore
  */
  private def getRequests(nodeList:JsonNode):ListBuffer[Request] = {
    
    val serviceRequests = ListBuffer[Request]()
    nodeList.foreach(node => serviceRequests.add(new Request(node)))
    
    return serviceRequests
  }
  
  private def getFeeds(dataNode:JsonNode):ListBuffer[FeedRequestData] = {
    if(dataNode == null)
      return null
    
    val dataList = ListBuffer[FeedRequestData]()
    dataNode.foreach{
      data => 
        if(data.has("feed_id") && data.has("sources"))
          dataList += FeedRequestData(data.get("feed_id").asText(), Conversions.nodeToStringList(data.get("sources")))
    }
    return dataList
  }

}