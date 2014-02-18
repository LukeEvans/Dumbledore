package com.reactor.facebook

import com.reactor.prime.data.StorySet
import java.util.ArrayList
import java.util.Collections
import scala.collection.JavaConversions._
import com.reactor.dumbledore.utilities.Tools

class FacebookStorySet extends StorySet {
  	var maxFacebook:Int = 0

	var maxTagged:Int = 0
	var maxPhotos:Int = 0
	var maxStatus:Int = 0
	var maxLinks:Int = 0
	var maxCheckins:Int = 0

	var taggedCount:Int = 0
	var photoCount:Int = 0
	var statusCount:Int = 0
	var linkCount:Int = 0
	var checkinCount:Int = 0
  	
  	def prune(requestedSize:Int){
  	  	maxFacebook = requestedSize;

		val numberOfItemsInFacebook = 5.0;
		val distributed = (Math.ceil(maxFacebook / numberOfItemsInFacebook)).toInt;

		maxTagged = distributed;
		maxPhotos = distributed;
		maxStatus = distributed;
		maxLinks = distributed;
		maxCheckins = distributed;

		photoCount = 0;
		statusCount = 0;
		linkCount = 0;
		checkinCount = 0;
		
		// Get the different types
		//ArrayList<FacebookStory> birthday = getStoryType("birthday");
		//ArrayList<FacebookStory> tagged = getStoryType("tagged");
		val status = getStoryType("status");
		val photos = getStoryType("photo");
		val links = getStoryType("link");
		val checkins = getStoryType("checkin");

		// Find ideal number for each
		findIdealCounts();

		// Clear what we have so far
		resetData();

		//pack(birthday, 1);
		//pack(tagged, maxTagged);
		pack(status, maxStatus);
		pack(photos, maxPhotos);
		pack(links, maxLinks);
		pack(checkins, maxCheckins);
  	}
  	
  	def pack(setToAdd:ArrayList[FacebookStory], maxToTake:Int){ 
  	  for(i <- 0 to maxToTake)
  	    addStory(setToAdd.get(i))    
  	}
  	
  	def buildSpeeches(){
  	  for(s <- data){
  	    val fbStory = s.asInstanceOf[FacebookStory]
  	  }
  	}
  	
  	def getStoryType(story_type:String): ArrayList[FacebookStory] = {
  	  val list = new ArrayList[FacebookStory]()
  	  
  	  for(s <- data){
  	    s match {
  	      case fbStory:FacebookStory =>{
  	        if(s.header.equalsIgnoreCase(story_type)){
  	          list.add(fbStory)
  	          incrementType(story_type)
  	        }
  	      }
  	    }
  	  }
  	  //Collections.sort(list)  
  	  list
  	}
  	
  	def incrementType(story_type:String){
  	  story_type match{
  	    case "status" => statusCount += 1
  	    case "photo" => photoCount += 1
  	    case "link" => photoCount += 1
  	    case "checkin" => checkinCount += 1
  	    case "tagged" => taggedCount += 1
  	  }
  	}
  	
  	def findIdealCounts(){
  	  if(data.size <= maxFacebook)
  	    return
  	    
  	  if(linkCount < maxLinks){
  	    maxPhotos += (maxLinks - linkCount)
  	    maxLinks = linkCount
  	  }
  	  if(checkinCount < maxCheckins){
  	    maxStatus += (maxCheckins - checkinCount)
  	    maxCheckins = checkinCount
  	  }
  	}
  	
  	def ensurePhotoPresent(){
  	  for(s <- data){
  	    s match{
  	      case fbStory:FacebookStory =>{
  	        if(fbStory.images == null || fbStory.images.size() == 0){
  	          fbStory.images = new ArrayList[String]
  	          
  	          val background = "REPLACE"//Tools.randomStringFromList((Constants.stockBackground))
  	          if(background != null)
  	            fbStory.images.add(background)
  	        }
  	      }
  	    }
  	  }
  	}
  	
  	def postProcess(token:String){
  	  for(s <- data){
  	    s match{
  	      case fbStory:FacebookStory => fbStory.postProcess(token)
  	    }
  	  }
  	}
}