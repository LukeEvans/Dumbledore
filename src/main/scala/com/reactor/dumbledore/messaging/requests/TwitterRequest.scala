package com.reactor.dumbledore.messaging.requests

import com.fasterxml.jackson.databind.ObjectMapper
import spray.http.HttpRequest

class TwitterRequest(obj:Object) extends APIRequest(obj) {
  
  final var twitterToken:String = _
  final var twitterSecret:String = _
  final var numberStories:Int = _
  
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