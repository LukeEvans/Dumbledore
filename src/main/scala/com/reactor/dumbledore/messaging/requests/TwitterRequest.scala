package com.reactor.dumbledore.messaging.requests

import com.fasterxml.jackson.databind.ObjectMapper
import spray.http.HttpRequest

class TwitterRequest extends APIRequest {
  
  final var twitterToken:String = null
  final var twitterSecret:String = null
  final var numberStories:Int = 0
  final var clearCache:Boolean = false
  
  def this(obj:Object){
    this()
    obj match{
      case s:String => create(s)
      case r:HttpRequest => create(r)
    }
  }
  
  def create(request:String){
    val reqJson = getJson(request)
    
    twitterToken = getString(reqJson, "twitter_token")
    twitterSecret = getString(reqJson, "twitter_token_secret")
    numberStories = getInt(reqJson, "number_stories", 8)
  }
  
  def create(request:HttpRequest){
    twitterToken = getStringR(request, "twitter_token")
    twitterSecret = getStringR(request, "twitter_token_secret")
    numberStories = getIntR(request, "number_stories", 8)
  }
  
}