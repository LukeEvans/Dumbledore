package com.reactor.dumbledore.prime.abstraction

import com.reactor.dumbledore.prime.entities.SentimentService
import com.fasterxml.jackson.databind.JsonNode
import com.reactor.dumbledore.utilities.Tools
import com.gravity.goose._

class Extractor {
  var configuration = new Configuration
  
  configuration.setImagemagickConvertPath("/usr/bin/convert") // Remote Settings
  //configuration.setImagemagickConvertPath("/usr/local/Cellar/imagemagick/6.8.7-0/bin/convert") // Local Settings
  
  configuration.setImagemagickIdentifyPath("/usr/bin/identify") // Remote Settings
  //configuration.setImagemagickIdentifyPath("/usr/local/Cellar/imagemagick/6.8.7-0/bin/identify") // Local Settings

  val goose = new Goose(configuration)
  
  def getAbstraction(url:String):Abstraction = {

	try{
	  val abs = new Abstraction(goose.extractContent(url))
	  
//	  val response = Tools.fetchURL(getURL(url));
//	  val abs = new Abstraction(response);
	
	  return abs;  
	  
	} catch{
	  case e:Exception => 
	    e.printStackTrace()
	    return null
	}

  }
  
  private def getURL(s:String):String = "http://www.diffbot.com/api/article?token=2a418fe6ffbba74cd24d03a0b2825ea5&url=" + s;

}