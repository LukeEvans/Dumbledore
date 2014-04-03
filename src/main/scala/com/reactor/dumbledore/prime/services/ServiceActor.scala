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
    request.params match {
      case Some(params) => 
        reply(origin, ListSetContainer(WebService.request(request.service_id, request.requestData, Some(params))))

      case None => 
        reply(origin, ListSetContainer(WebService.request(request.service_id, request.requestData, None)))

    }   
  }
  
  def handleDumbledoreRequest(request:ServiceRequest, origin:ActorRef){
    
    request.service_id match{
      
      case Prime.TRAFFIC => 
        reply(origin, ListSetContainer(Traffic.getTraffic()))
        
        
      case "nearby_places" => 
        request.params match{
          case Some(param) =>

            val set = Yelp.searchNearby(param.getOrElse("type", "lunch"), 
                param.getOrElse("lat", "40.6700").toDouble, 
                param.getOrElse("long", "-73.940").toDouble,
                3)
                  
            reply(origin, ListSetContainer(ListSet(Prime.NEARBY_PLACES, set)))
           
            
          case None => reply(origin, null)
        }
        
     
      case Prime.STOCKS =>{
        val data = request.requestData
        
        val stockTickers = if(data.ids != null && data.ids.nonEmpty) data.ids else ListBuffer("TSLA", "AAPL", "FB")
        
        val stockSet = YahooStocksAPI.getStockCards(stockTickers) 
        
        reply(origin, ListSetContainer(ListSet(Prime.STOCKS, stockSet)))
        
      }
    }
  }
}