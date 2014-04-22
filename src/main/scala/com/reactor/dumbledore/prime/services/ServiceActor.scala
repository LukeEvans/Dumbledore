package com.reactor.dumbledore.prime.services

import com.reactor.patterns.pull.FlowControlArgs
import com.reactor.patterns.pull.FlowControlActor
import akka.actor.ActorRef
import com.reactor.dumbledore.utilities.Location
import scala.util.Random
import scala.collection.mutable.ListBuffer
import com.reactor.dumbledore.messaging.ServiceRequest
import com.reactor.dumbledore.messaging.ListSetContainer
import com.reactor.dumbledore.prime.services.traffic.Traffic
import com.reactor.dumbledore.prime.constants.Prime
import com.reactor.dumbledore.prime.services.yelp.Yelp
import com.reactor.dumbledore.prime.data.ListSet
import com.reactor.dumbledore.prime.services.stocks.YahooStocksAPI
import com.reactor.dumbledore.prime.services.donations.DonorsChoose
import com.reactor.dumbledore.prime.services.donations.DonorsChoose
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent._
import scala.concurrent.duration._


class ServiceActor(args:FlowControlArgs) extends FlowControlActor(args){

  ready()
  override def preStart() = println("Service Actor Starting")  
  
  def receive = {
    case request:ServiceRequest => request.requestData.requestType match{
      case "v036" =>       
        handleSpringRequest(request, sender)
        complete()
        
      case "dumbledore" =>
        handleDumbledoreRequest(request, sender)
        complete()
    }
    case a:Any => 
      println("ServiceActor:Unknown message received - " + a)
      complete()
  }
  
  def handleSpringRequest(request:ServiceRequest, origin:ActorRef){   
    try{
    request.params match {
      case Some(params) => 
        
        val serviceFuture = future(WebService.request(request.service_id, request.requestData, Some(params)))
        
        reply(origin, ListSetContainer(Await.result(serviceFuture, atMost = 3 seconds)))

      case None => 
        
        val serviceFuture = future(WebService.request(request.service_id, request.requestData, None))
        
        reply(origin, ListSetContainer(Await.result(serviceFuture, atMost = 3 seconds)))

    }   
    } catch{
      case e:Exception =>
        e.printStackTrace()
        reply(origin, ListSetContainer(ListSet(request.service_id, ListBuffer[Object]())))
    }
  }
  
  def handleDumbledoreRequest(request:ServiceRequest, origin:ActorRef){
    try{
    request.service_id match{
      
      case Prime.TRAFFIC => 
        
        val trafficFuture = future{ Traffic.getTraffic }
        
        reply(origin, ListSetContainer(Await.result(trafficFuture, atMost = 3 seconds)))
        
        
      case "nearby_places" => 
        request.params match{
          case Some(param) =>

            val setFuture = future{Yelp.searchNearby(param.getOrElse("type", "lunch"), 
                param.getOrElse("lat", "40.6700").toDouble, 
                param.getOrElse("long", "-73.940").toDouble,
                3)}
                  
            reply(origin, ListSetContainer(ListSet(Prime.NEARBY_PLACES, Await.result(setFuture, atMost = 3 seconds))))
           
            
          case None => reply(origin, null)
        }
        
     
      case Prime.STOCKS =>{
        val data = request.requestData
        
        val stockTickers = if(data.ids != null && data.ids.nonEmpty) data.ids else ListBuffer("TSLA", "AAPL", "FB")
        
        val stockSetFuture = future{YahooStocksAPI.getStockCards(stockTickers) }
        
        reply(origin, ListSetContainer(ListSet(Prime.STOCKS, Await.result(stockSetFuture, atMost = 3 seconds))))
        
      }
      
      case Prime.DONATIONS =>{
        
        val data = request.requestData
        
        data.params match{
          case Some(params) =>
            val lat = params.getOrElse("lat", "40.0176")
            val long = params.getOrElse("long", "-105.2797")
            
            val donationsFuture = future{DonorsChoose.findProjects(lat.toDouble, long.toDouble, 5)}
            
            reply(origin, ListSetContainer(ListSet(Prime.DONATIONS, Await.result(donationsFuture, atMost = 3 seconds))))
            
          case None =>
            
            val donationsFuture = future{DonorsChoose.findProjects(40.0176, -105.2797, 5)}
            
            reply(origin, ListSetContainer(ListSet(Prime.DONATIONS, Await.result(donationsFuture, atMost = 3 seconds))))
        }
        
      }
    }
    } catch{
      case e:Exception => 
        e.printStackTrace()
        reply(origin, ListSetContainer(ListSet("", ListBuffer[Object]())))
    }
  }
}