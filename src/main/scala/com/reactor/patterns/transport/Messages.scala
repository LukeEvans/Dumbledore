package com.reactor.patterns.transport

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.JsonNode
import spray.routing.RequestContext
import com.reactor.dumbledore.messaging.requests.APIRequest

abstract class RESTRequest(data:Any) extends Serializable
class RESTResponse(data:Any) extends Serializable {
	
  //var finalData = data
	
  def finish(startTime:Long, mapper:ObjectMapper): String = {
    return mapper.writeValueAsString(this)
  }
}

// Error
case class Error(status: String)

// JSON parse Error
case class JsonParseError(msg:String)

// Accio request and responses
case class RequestContainer(req:RESTRequest)
case class RequestContainer2(req:APIRequest)
case class StringRequestContainer(req:String)
case class ResponseContainer(resp:RESTResponse)

// Dispatch messages
case class DispatchRequest(request:StringRequestContainer, ctx:RequestContext, mapper:ObjectMapper)
case class OverloadedDispatchRequest(message:Any)
  
// HTTP Request
case class HttpObject(uri: String, obj: JsonNode = null, response: JsonNode = null, method: String = "GET") 
case class JsonResponse(node: JsonNode)