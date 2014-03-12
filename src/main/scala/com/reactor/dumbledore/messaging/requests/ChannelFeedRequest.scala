package com.reactor.dumbledore.messaging.requests

import spray.http.HttpRequest

class ChannelFeedRequest extends APIRequest {
  
  var clearCache:Boolean = false
  
  def this(obj:Object){
    this()
    obj match{
      case s:String => create(s)
      case r:HttpRequest => create(r)
    }
  }
  
  override def create(request:HttpRequest){
    clearCache = getBoolR(request, "clearCache")
  }
  
  override def create(string:String){

    val reqJson = getJson(string);	
    
    clearCache = getBool(reqJson, "clearCache")
  }
}