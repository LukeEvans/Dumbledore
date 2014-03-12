package com.reactor.dumbledore.messaging.requests

import spray.http.HttpRequest

class ChannelFeedRequest(obj:Object) extends APIRequest(obj) {
  
  var clearCache:Boolean = _
  
  override def create(request:HttpRequest){
    clearCache = getBoolR(request, "clearCache")
  }
  
  override def create(string:String){

    val reqJson = getJson(string);	
    
    clearCache = getBool(reqJson, "clearCache")
  }
}