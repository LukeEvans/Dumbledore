package com.reactor.dumbledore.prime.services.youtube

import java.util.Date
import java.text.SimpleDateFormat
import com.fasterxml.jackson.databind.JsonNode


/** Youtube Video Object
 */
class YoutubeVideo {
  val `type` = "youtube"
  val story_type = "youtube"
  var id:String = null
  var title:String = null
  var channel:String = null
  var embedded_html:String = null
  var description:String = null
  var thumbnail:String = null
  var date:Date = null
  val image_url = "https://s3.amazonaws.com/Channel_Icons/youtube_icon.png"
  var view_count:Int = 0
  var likes:Int = 0
  var dislikes:Int = 0
  
  
  /** JsonNode Constructor */
  def this(videoNode:JsonNode){
    this()
    if(videoNode.has("id"))
      id = videoNode.get("id").asText();
    if(id != null){
      embedded_html = "<iframe width=\"560\" height=\"315\" " +
        "src=\"//www.youtube.com/embed/" + id +
        " frameborder=\"0\" allowfullscreen></iframe>";
    }
    if(videoNode.has("snippet")){
      val snipNode = videoNode.get("snippet");
      if(snipNode.has("title"))
        title = snipNode.get("title").asText();
  	  if(snipNode.has("channelTitle"))
  	    channel = snipNode.get("channelTitle").asText();
  	  if(snipNode.has("description"))
  		description = snipNode.get("description").asText();
  	  if(snipNode.has("thumbnails"))
  		thumbnail = snipNode.get("thumbnails").get("high").get("url").asText();
  	  if(snipNode.has("publishedAt")){
   		try{
   		  val dateString = snipNode.get("publishedAt").asText();
   		  date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(dateString);
   		  
   		} catch{
   		  case e:Exception => e.printStackTrace();
   		}
  	  }
    }
    if(videoNode.has("statistics")){
      val statNode = videoNode.get("statistics");
      if(statNode.has("viewCount"))
 		view_count = statNode.get("viewCount").asInt();
      if(statNode.has("likeCount"))
    	likes = statNode.get("likeCount").asInt();
   	  if(statNode.has("dislikeCount"))
   		dislikes = statNode.get("dislikeCount").asInt();   		
    }	
  }
}