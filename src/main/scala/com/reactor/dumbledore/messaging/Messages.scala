package com.reactor.dumbledore.messaging

import spray.http.HttpRequest
import spray.http.HttpResponse
import com.reactor.dumbledore.utilities.Location
import java.util.ArrayList
import scala.collection.mutable.Map
import scala.collection.mutable.ListBuffer
import com.reactor.dumbledore.prime.data.ListSet
import com.reactor.dumbledore.prime.services.WebRequestData
import twitter4j.Status
import com.reactor.dumbledore.messaging.requests._
import com.reactor.dumbledore.prime.notifications.request.Request
import com.reactor.dumbledore.prime.PrimeSet
import org.joda.time.DateTime
import com.mongodb.DBObject

class Message extends Serializable

trait request

// HTTP Request Forwarding Containers 
case class RequestContainer(request:HttpRequest, urlBase:String) extends request
case class ResponseContainer(response:HttpResponse) extends request


// Service Request Container
//case class ServiceRequest(service_id:String, endpoint:String, ids:ListBuffer[String], params:Option[Map[String, String]]) extends request
case class ServiceRequest(service_id:String, requestData:WebRequestData, params:Option[Map[String, String]]) extends request


// Data Containers
case class DataSetContainer(data:ListBuffer[ListSet[Object]]) extends request
//case class ListSetContainer(data:ListSet[Object]) extends response
case class ListSetContainer[T](data:ListSet[T]) extends request


// Notification Request
case class NotificationRequestContainer(request:NotificationRequest) extends request

// Entertainment Request
case class EntertainmentRequestContainer(request:ListBuffer[Request], all:Boolean) extends request

// Feed Request Containers
case class FeedData(data:ListBuffer[FeedRequestData]) extends request
case class Feeds(clear:Boolean) extends request

// Channel Request Containers
case class Sources() extends request
case class SourceData(data:ListBuffer[String]) extends request

// Twitter Containers
case class TwitterStoryData(status:Status, me:Long) extends request

//==========================
// Prime Request Containers 
//==========================

// Ranking Containers
case class PrimeRankContainer(set:PrimeSet, now:DateTime, dev:Boolean)

// Youtube Containers
case class YoutubeData(request:YoutubeRequest)


// Mongo Containers
case class MongoQuery(collection:String, query:DBObject, limit:Int)
