package com.reactor.dumbledore.prime.abstraction

import com.reactor.dumbledore.prime.entities.SentimentService
import com.fasterxml.jackson.databind.JsonNode
import com.reactor.dumbledore.utilities.Tools

class Extractor {
  var sentiment = new SentimentService
  
  def getAbstraction(url:String):Abstraction = {
	val response = Tools.fetchURL(getURL(url));
	val abs = new Abstraction(response);
	//abs.getEntities(sentiment);
	return abs;
  }
  
  private def getURL(s:String):String = "http://www.diffbot.com/api/article?token=2a418fe6ffbba74cd24d03a0b2825ea5&url=" + s;

}