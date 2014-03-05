package com.reactor.dumbledore.messaging

import spray.http.HttpRequest
import spray.http.HttpResponse
import com.reactor.dumbledore.utilities.Location
import java.util.ArrayList
import scala.collection.mutable.Map
import scala.collection.mutable.ListBuffer
import com.reactor.dumbledore.data.ListSet

class Message extends Serializable

trait request
trait response

case class RequestContainer(request:HttpRequest, urlBase:String) extends request

case class ResponseContainer(response:HttpResponse) extends request

case class ForwardRequest(request:HttpRequest, path:String) extends request

case class ServiceRequest(service_id:String, endpoint:String, ids:ListBuffer[String], params:Option[Map[String, String]]) extends request

case class DataContainer(data:ArrayList[ArrayList[Object]]) extends response

case class DataSetContainer(data:ListBuffer[ListSet]) extends response

case class SingleDataContainer(data:ArrayList[Object]) extends response

case class ArraySetContainer(data:ListSet) extends response

case class NotificationRequestContainer(request:NotificationRequest) extends request

case class FeedData(data:ListBuffer[ChannelRequestData]) extends request

case class Feeds() extends request