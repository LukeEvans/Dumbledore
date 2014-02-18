package com.reactor.facebook

import scala.collection.JavaConversions._
import java.util.ArrayList
import com.fasterxml.jackson.databind.JsonNode

class TaggedStory extends FacebookStory{
  override def buildStory(node:JsonNode){
    super.buildStory(node)
    header = "Tagged"
      
    FacebookType match{
      case "status" => taggedStatus(node)
      case "photo" => taggedPhoto(node)
      case "link" => taggedLink(node)
    }  
    id = null
  }
  
  override def buildScore():Unit = score = 1
  
  def taggedStatus(node:JsonNode){
    id = node.path("id").asText();
	description = user_name + " tagged you in a status";

	val status_type = node.path("status_type").asText();

	if (status_type != null && to_user_name != null) {
	  if (status_type.equalsIgnoreCase("wall_post")) 
		description = user_name + " posted a status on " + to_user_name + "'s timeline.";
	  else 
		description = user_name + " tagged " + to_user_name + " in a status.";
	}
  }
  
  def taggedPhoto(node:JsonNode){
	id = node.path("id").asText();
    description = user_name + " tagged you in a photo";
    images.add(node.path("picture").asText());
    
    val edited = new ArrayList[String]()
    for (s <- images) 
    	edited.add(s.replaceAll("_s.", "_n."))
  }
    
  def taggedLink(node:JsonNode){
	id = node.path("id").asText();
	description = user_name + " shared a link on your timeline";

	val statusType = node.path("status_type").asText();

		// Get rid of approved friends
	if (statusType != null && statusType.equalsIgnoreCase("approved_friend"))
	  id = null;
  }
}