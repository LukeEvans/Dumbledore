package com.reactor.dumbledore.prime.entities

import com.reactor.dumbledore.utilities.Tools

class SentimentService {

  def getEntities(text:String):EntityList = {
    val url = getAlchemyURI(text)
    fetchEntities(url)
  }
  
  private def fetchEntities(url:String):EntityList = {
    
    val response = Tools.fetchURL(url) 
    if(response != null)
      return new EntityList(response)
    else
      return null
  }
  
  private def getAlchemyURI(text:String):String = {
	val apiKey = "fb5e30fc9358653bee86ffd698ed024aae33325c";
	val outputFormat = "json";
	val sentiment = 1;

	val uri = "http://access.alchemyapi.com/calls/text/TextGetRankedNamedEntities?apikey=" + apiKey + 
				"&outputMode=" + outputFormat + 
				"&sentiment=" + sentiment + 
				"&text=" + text;  

	return uri;
  }
}