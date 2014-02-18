package com.reactor.facebook

import java.util.ArrayList
import scala.collection.JavaConversions._
import com.fasterxml.jackson.databind.JsonNode

class HomeStory extends FacebookStory {
  
  def this(node:JsonNode, myID:String, token:String){
    this()
    if(node.path("from").path("name").asText().equalsIgnoreCase(myID))
      id = null
    
    else if(node.has("story")){
      if(node.get("story").asText().contains("commented") || node.get("story").asText().contains("likes")){
        id = null
      }
    }
    else
      buildStory(node)	
  }
	
  override def buildStory(node:JsonNode){
    try{
    super.buildStory(node)
    
    FacebookType match{
      case "status" => homeStatus(node)
      case "photo" => homePhoto(node)
      case "link" => homeLink(node)
      case other:Any => println("Unsupported FacebookType: " + other) 
    }
    } catch{
      case e:Exception => e.printStackTrace()
    }
  }
  
  override def buildScore():Unit = {
	if(header == null)
	  return
	header match{
	  case "Status" =>{
	    statusScore();
	  }
	  case "Photo" =>{
	    photoScore();
	  }
	  case "Link" =>{
	    linkScore();
	  }
	}
  }
  
  def statusScore(){
//	  if (sentiment == null)
//			return;
//
//		if (sentiment.entities != null && sentiment.entities.size() > 0) 
//			score += 3;
//
//		int emotionCount = sentiment.emotionCount(content);
//
//		if (emotionCount > 0)
//			score++;
//		setEntities(sentiment.topEntityList());
    println("statusScore")
  }
  
  def photoScore() = score = Math.max(like_count, comment_count)
  
  def linkScore(){
//    		if (abstraction != null && abstraction.entities != null && abstraction.entities.size() > 0) {
//			score++;
//			setEntities(abstraction.entities.entities);
//		}
    println("linkScore")
  }
  
  override def buildSpeech():Unit = {
    println("buildSpeech")
  }
  
  def homeStatus(node:JsonNode){
    try{
    id = node.path("id").asText();
    header = "Status";
    description = user_name + " updated their status";

    val status_type = node.path("status_type").asText();

    if (status_type != null && to_user_name != null) {
      if (status_type.equalsIgnoreCase("wall_post"))
    	description = user_name + " posted a status on " + to_user_name + "'s timeline.";
      else
    	description = user_name + " tagged " + to_user_name + " in a status.";
			
    }
    } catch{
      case e:Exception => e.printStackTrace()
    }
  }
  
  def homePhoto(node:JsonNode){
    try{
	id = node.path("id").asText();
	header = "Photo";
	description = user_name + " posted a photo";

	var tempImages = new ArrayList[String]();
	tempImages.add(node.path("picture").asText());

	for (s <- tempImages) {
      val r = s.replaceAll("_s.", "_n.");
      images.add(r);
	}

	val story = node.path("story").asText();

	if (story != null) 
		description = story;
    } catch{
      case e:Exception => e.printStackTrace()
    }
  }
  
  def homeLink(node:JsonNode){
    try{
	id = node.path("id").asText();
	header = "Link";
	description = user_name + " shared a link";
	url = node.path("link").asText();

	if (url == null || url.length() <= 0)
      id = null;

	val statusType = node.path("status_type").asText();

	// Get rid of approved friends
	if (statusType != null && statusType.equalsIgnoreCase("approved_friend")) {
	  id = null;
	  return;
	}
    } catch {
      case e:Exception => e.printStackTrace()
    }
  }
}