package com.reactor.dumbledore.prime.services.yelp

import com.fasterxml.jackson.databind.JsonNode
import com.reactor.dumbledore.utilities.Tools
import scala.collection.JavaConversions._
import com.reactor.dumbledore.utilities.Conversions
import scala.collection.mutable.ListBuffer

class YelpStory {
  
  val `type` = "yelp";
  val story_type = "yelp"
  val image_url = "https://s3.amazonaws.com/Channel_Icons/yelp_icon.png";
  	
  var id:String = null
  var business_name:String = null
  var business_type:String = null
  var business_img_url:String = null
  var rating_img_url:String = null
  var entity:String = null
	
  var categories:ListBuffer[String] = null
  var review_count:Int = 0
  var address:JsonNode = null

  def this(yelpNode:JsonNode){
    this()

    if(yelpNode.has("name")){
      business_name = yelpNode.get("name").asText();
      id = Tools.generateHash(business_name);
    }
    if(yelpNode.has("image_url"))
      business_img_url = yelpNode.get("image_url").asText();
    if(yelpNode.has("rating_img_url_large"))
      rating_img_url = yelpNode.get("rating_img_url_large").asText();
    else
      rating_img_url = yelpNode.get("rating_img_url").asText();
    if(yelpNode.has("categories"))
      categories = getCategories(yelpNode.get("categories"))
	if(yelpNode.has("review_count"))
	  review_count = yelpNode.get("review_count").asInt();
	if(yelpNode.has("location")){
	  if(yelpNode.get("location").has("display_address"))
		address = yelpNode.get("location").get("display_address");
	}
  }
  
  private def getCategories(nodeList:JsonNode):ListBuffer[String] = {
    
    val categories = ListBuffer[String]();
		
    nodeList.map( node => categories += node.get(0).asText() )

    return categories; 
  }
}