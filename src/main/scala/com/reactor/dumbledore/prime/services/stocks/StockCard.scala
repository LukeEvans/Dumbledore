package com.reactor.dumbledore.prime.services.stocks

import com.reactor.dumbledore.utilities.Tools
import java.math.BigDecimal
import com.reactor.dumbledore.prime.cards.Card
import com.fasterxml.jackson.databind.JsonNode


class StockCard(company:String, symbol:String) extends Card{
	val id = Tools.generateHash(company);
	val `type` = "stocks";
	val icon = "https://s3.amazonaws.com/Twitter_Source_Images/Winston-Twitter-Images/Stocks_icon.png";
	val source = "Yahoo! Finance";
	var value:Double = 0;
	var change:Double = 0;
	var percent_change:String = null;
	
	def this(node:JsonNode){
	  this(node.get("symbol").asText(), node.get("Name").asText())
	  updateValues(node.get("LastTradePriceOnly").asDouble, node.get("Change").asDouble())
	}
	
	def updateValues(value:Double, change:Double){
	  this.value = value;
	  this.change = change;
	  percent_change = calculatePercent(value, change);
	}
	
	private def calculatePercent(value:Double, change:Double):String = {
	  if(value == 0)
	    return "0.0%"
		
	  val changePercent = (change/value)*100
		
	  var dec = new BigDecimal(changePercent)
	  dec = dec.setScale(2, BigDecimal.ROUND_HALF_UP)
		
	  return dec.toString() + "%"
	}
}