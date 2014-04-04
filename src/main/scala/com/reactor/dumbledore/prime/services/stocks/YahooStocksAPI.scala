package com.reactor.dumbledore.prime.services.stocks

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

import com.fasterxml.jackson.databind.JsonNode
import com.reactor.dumbledore.utilities.Tools


object YahooStocksAPI {
  
  val baseURL = "http://query.yahooapis.com/v1/public/yql?" +
		  			"format=json&env=store://datatables.org/alltableswithkeys";
  
  
  def getStockCards(stocks:ListBuffer[String]):ListBuffer[Object] = {
    
    val response = Tools.fetchURL(formUrl(stocks))
    
    if(response != null){
      if(response.get("query").has("results")){

        if(response.get("query").get("results").get("quote").isArray())
        	return createCards(response.get("query").get("results").get("quote"))
        else
          return createCards(response.get("query").get("results"))
      }
    }
    
    null
  }

  
  private def createCards(nodeData:JsonNode):ListBuffer[Object] = {
    
    val cards = ListBuffer[Object]()
    
    for(node <- nodeData)
      cards.add(new StockCard(node)) 
    
    cards
  }
  
  
  private def formUrl(stocks:ListBuffer[String]):String = {
    if(stocks == null || stocks.isEmpty)
      return null
      
    var queryUrl = baseURL + "&q= select * from yahoo.finance.quote where symbol in ("
    
    for(i <- 0 to stocks.size -1){
      
      queryUrl += "\""+stocks(i)+"\"";
      
   	  if((i+1) != stocks.size)
   		queryUrl +=  ", ";
    }
      
    return queryUrl + ")";
  }
}