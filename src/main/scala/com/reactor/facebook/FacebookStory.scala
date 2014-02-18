package com.reactor.facebook

import java.util.ArrayList
import java.util.Date
import com.reactor.prime.data.Story
import java.util.Formatter.DateTime
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import com.reactor.dumbledore.utilities.Tools
import com.fasterxml.jackson.databind.JsonNode

abstract class FacebookStory extends Story {
  var user_name:String = null
  var FacebookType:String = null
  var fbID:String = null
  var fromCategory:String = null
  
  var to_user_name:String = null
  var comment_count:Int = 0
  var like_count:Int = 0
  
  var source:String = null
  var timestamp:String = null
  var content:String = null
  var prof_pic:String = null
  var cover_pic:String = null
  var images:ArrayList[String] = null
  var url:String = null
  
  //var comments:Comment = null
  //var likes:Like = null
  
  var date:Date = null
  var text:String = null
  var postID:String = null
  
  //var sentiment:SentimentObject
  //var abstraction:Abstraction
  //var sentimentService:SentimentService
  //var extractor:Extractor
 
  def this(node:JsonNode, myID:String, token:String){
    this()
    if(node.get("from").get("name").asText().equalsIgnoreCase(myID))
      id = null
    
    else if(node.has("story")){
      if(node.get("story").asText().contains("commented") || node.get("story").asText().contains("likes")){
        id = null
      }
    }
    else
      buildStory(node)	
  }
  
  def buildStory(node:JsonNode){
	  db = "Facebook";
	  // Services
	  //sentimentService = s;
      //extractor = ex;
	  
	  fbID = node.path("from").path("id").asText();
	  fromCategory = node.path("from").path("category").asText();
		
		// Data
		FacebookType = node.path("type").asText();
		user_name = node.path("from").path("name").asText();
		story_type = "Facebook";
		
		//comments = new Comment(node.path("comments").path("count").asInt(), node);
		comment_count = node.path("comments").path("count").asInt();
		postID = node.path("id").asText();

		source = user_name;
		timestamp = node.path("created_time").asText();
		content = node.path("message").asText();
		
		prof_pic = "https://graph.facebook.com/" + fbID + "/picture?width=720&height=720";
		to_user_name = node.path("to").path("data").path(0).path("name").asText();
		url = node.path("actions").path(0).path("link").asText();
						
		images = new ArrayList[String]();
		
		val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");
		val dateTime = formatter.parseDateTime(timestamp);
		date = dateTime.toDate()
  }
  
  def postProcess(token:String) = {
	  //likes = grabLikes(postID, token)
	  //like_count = likes.count;
      fetchCoverPicture();
  }
  
//  public grabLikes(id:String, token:String):Like = {
//        try{
//      
//    }catch {
//      case e:Exception => e.printStackTrace()
//    }
//  }
  
  def buildSpeech():Unit = {
    
  }
  
  def buildScore():Unit = {
    
  }
  
  def fetchCoverPicture(){
    try{
      val response = Tools.fetchURL(coverPhotoURL(fbID))
      cover_pic = response.get("cover").path("source").asText();
      
    }catch {
      case e:Exception => e.printStackTrace()
    }
  }
  
  def coverPhotoURL(id:String) = "https://graph.facebook.com/" + id + "?fields=cover";
  
}