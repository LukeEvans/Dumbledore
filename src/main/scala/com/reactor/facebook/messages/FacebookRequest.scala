package com.reactor.facebook.messages

import org.codehaus.jackson.map.ObjectMapper

class FacebookRequest {
	@transient
	val mapper = new ObjectMapper()
	
	var facebook_token:String = null
	var timezone_offset:String = null
	
	def this(request:String){
	  this()
	  var cleanRequest = request.replaceAll("\\r", " ").replaceAll("\\n", " ").trim
	  val reqJson = mapper.readTree(cleanRequest);	  
	  if(reqJson.has("facebook_token"))
	    facebook_token = reqJson.get("facebook_token").asText
	  if(reqJson.has("timezone_offset"))
	    timezone_offset = reqJson.get("timezone_offset").asText	    
	}
}