package com.reactor.dumbledore.messaging.requests

import spray.http.HttpRequest

class YoutubeRequest extends APIRequest {

  var channelID:Option[String] = null
  var number:Int = 10
  
  def this(obj:Object){
    this()
    obj match{
      case s:String => create(s)
      case r:HttpRequest => create(r)
    }
  }
  
  def create(request:String){
    
    val json = getJson(request)
    
    channelID = getStringOpt(json, "channel_id")
    number = getInt(json, "number", number)
  }
  
  def create(request:HttpRequest){
    
    channelID = getStringOptR(request, "channel_id")
    number = getIntR(request, "number", number)
  }
  
}