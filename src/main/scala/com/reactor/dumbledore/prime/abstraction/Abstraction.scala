package com.reactor.dumbledore.prime.abstraction

import scala.collection.mutable.ListBuffer
import com.reactor.dumbledore.prime.entities.EntityList
import com.fasterxml.jackson.databind.JsonNode
import scala.collection.JavaConversions._
import com.reactor.dumbledore.utilities.Tools
import com.reactor.dumbledore.prime.entities.SentimentService
import com.gravity.goose.Article

class Abstraction {
  var title:String = null;
  var text:String = null;
  var images = ListBuffer[String]();
  var entities = new EntityList();
  var url:String = null;
  
  def this(node:JsonNode){
	this()	
    if(node != null) {
      if (node.path("title") != null && node.path("text") != null && node.path("media") != null) {
        title = node.path("title").asText;
        text = node.path("text").asText;

        getImages(node.path("media"));
      }
    }
  }
  
  def this(article:Article){
    this()
    if(article != null){
      title = article.title
      text = article.cleanedArticleText
      images += article.topImage.getImageSrc
    }
  }
  
  def getImages(node:JsonNode):ListBuffer[String] = {
    val primary = ListBuffer[String]();
	val secondary = new ListBuffer[String]();
		
	for (media <- node) { 
	  val media_type = media.path("type").asText;

	  if (media_type != null && media_type.equalsIgnoreCase("image")) {
	    var p = media.path("primary").asText()
	    var link = media.path("link").asText()

	    if (p != null && p.equalsIgnoreCase("true")) {
	    	primary.add(link);
	    }

	    else {
	    	secondary.add(link);
	    }

	  }
	}

	if (primary.size() > 0) {
		return removeSmallImages(primary)
	}
	
	else {
		return removeSmallImages(secondary)
	}
  }
  
  def removeSmallImages(list:ListBuffer[String]):ListBuffer[String] = {
    var finalImages = ListBuffer[String]()
		
    for (s <- images) {
      if (isValid(s)) {
        val i = Tools.getImageFromURL(s);

        val width = i.getWidth(null);
		val height = i.getHeight(null);

		if (height >= 100 && width >= 100) {
					finalImages.add(s);
		}
      }
    }
    finalImages
  }
  
  def isValid(src:String):Boolean = {
    var image = src.toLowerCase();

    if (image.endsWith(".jpeg")) {
    	return true;
    }

    if (image.endsWith(".jpg")) {
    	return true;
    }
    if (image.endsWith(".png")) {
    	return true;
    }
    
    return false;
  }
  
  def getEntities(sentiment:SentimentService) {
	if (text == null || text.length() == 0) {
	  return;
	}
	entities = sentiment.getEntities(text);
  }
}