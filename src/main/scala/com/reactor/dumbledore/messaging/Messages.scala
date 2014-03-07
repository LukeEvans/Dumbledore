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

// HTTP Request Forwarding Containers 
case class RequestContainer(request:HttpRequest, urlBase:String) extends request
case class ResponseContainer(response:HttpResponse) extends request


// Service Request Container
case class ServiceRequest(service_id:String, endpoint:String, ids:ListBuffer[String], params:Option[Map[String, String]]) extends request


// Data Containers
case class DataSetContainer(data:ListBuffer[ListSet[Object]]) extends response
case class ListSetContainer(data:ListSet[Object]) extends response


// Notification Request
case class NotificationRequestContainer(request:NotificationRequest) extends request


// Feed Request Containers
case class FeedData(data:ListBuffer[FeedRequestData]) extends request
case class Feeds() extends request

// Channel Request Containers
case class Sources() extends request
case class SourceData(data:ListBuffer[String]) extends request

