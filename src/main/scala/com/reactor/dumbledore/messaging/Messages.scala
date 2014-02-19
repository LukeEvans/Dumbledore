package com.reactor.dumbledore.messaging

import spray.http.HttpRequest
import spray.http.HttpResponse

class Message extends Serializable

trait request
trait response

case class RequestContainer(request:HttpRequest) extends request

case class ResponseContainer(response:HttpResponse) extends request

case class ForwardRequest(request:HttpRequest, path:String) extends request