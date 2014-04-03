package com.reactor.dumbledore.prime.services.twitter

import scala.collection.mutable.ListBuffer
import java.util.Date
import com.reactor.dumbledore.prime.entities.Entity
import twitter4j.Status
import twitter4j.UserMentionEntity
import java.util.HashMap
import scala.collection.JavaConversions._
import java.util.regex.Pattern
import java.util.regex.Matcher
import com.reactor.dumbledore.prime.entities.SentimentService
import com.reactor.dumbledore.prime.entities.EntityList
import com.reactor.dumbledore.prime.abstraction.Abstraction
import com.reactor.dumbledore.prime.abstraction.Extractor
import com.reactor.dumbledore.utilities.Timer

class TwitterStory {
  var db:String = null
  var id:String = null
  var `type`:String = null
  var story_type:String = null
  var header:String = null
  var description:String = null
  var entities = ListBuffer[Entity]()
  var speech:String = null
  var valid:Boolean = false
  
  var tweet:String = null;
  var timestamp:String = null;
  var name:String = null
  var prof_pic:String = null
  var cover_pic:String = null
  var images = ListBuffer[String]();
  var url:String = null;

  var favorite_count:Int = 0
  var retweet_count:Int = 0;
  var user_retweeted:Boolean = false;
  var user_favorited:Boolean = false;
  var handle:String = null;
  var timeScore:Double = 0;
  
  var score:Int = 0
        
  var date:Date = null;
  
  private var linkScore = 0
  
  def this(status:Status, me:Long){
    this()
    
    if(!storyValid(status, me)){
      id = null
    }
    else{
      db = "Twitter"
      id = status.getId().toString()
      header = "Status"
      `type` = "Twitter"
      story_type = `type`
      name = status.getUser().getName()
      handle = status.getUser().getScreenName()
      description = "Tweet from " + name
      timestamp = status.getCreatedAt().toString()
      date = status.getCreatedAt()
      
      tweet = clean(status.getText());
      
      var expandedTweet = expandUsers(status.getText(), status.getUserMentionEntities());
      var cleanString = clean(expandedTweet);
      cleanString = cleanFurther(cleanString);
      
      prof_pic = status.getUser().getOriginalProfileImageURLHttps();
      cover_pic = status.getUser().getProfileBannerRetinaURL();
      
      retweet_count = status.getRetweetCount();
      favorite_count = status.getFavoriteCount()
      user_retweeted = status.isRetweetedByMe();
      user_favorited = status.isFavorited();
      
      images(status);
      
      val urls = status.getURLEntities()
      
      if (urls != null && urls.length > 0) {
        url = urls(0).getExpandedURL();
        header = "Link";
      }
    }
  }
  
  def entityAnalysis(){
    val service = new SentimentService
    entities = addEntities(service.getEntities(tweet), entities)
  }
  
  /** add entities to existing entity arraylist and return concatenation */
  private def addEntities(list:EntityList, entities:ListBuffer[Entity]):ListBuffer[Entity] = {
    if(list == null || list.size < 1)
      return null
    val newEntities = ListBuffer[Entity]() 
    if(entities != null)
      newEntities.addAll(entities)
    
    for(entity <- list.entities){
      newEntities.add(entity)
    }
      
    newEntities
  }
  
  private def images(status:Status) {
	images = ListBuffer[String]();

                // Shared image
	for (entity <- status.getMediaEntities()) {
	  if (entity.getType().equalsIgnoreCase("photo")) {
	    images.add(entity.getExpandedURL());
	    header = "Photo";
	  }
	}

	// Bannar
	if (images.size() == 0) {
	  if (cover_pic != null) {
		images.add(cover_pic);
	  }
	}

                // Background Picture
	if (images.size() == 0) {
	  if (status.getUser().getProfileBackgroundImageUrlHttps() != null) {
		images.add(status.getUser().getProfileBackgroundImageUrlHttps());
	  }
	}
  }
  
  private def expandUsers(s:String, mentions:Array[UserMentionEntity]):String = {
                
    var itemsToReplace = new HashMap[String, String]();
                
    for (user <- mentions) {
                        
      var screenName = s.substring(user.getStart(), user.getEnd());
      var name = user.getName();
                        
      if (user.getStart() <= 2) {
        name = "";
      }
      
      itemsToReplace.put(screenName, name);
    }
                
    var newS:String = s
    
    for (entry <- itemsToReplace.entrySet()) {
                        
    	newS = s.replaceAll(Pattern.quote(entry.getKey()), Matcher.quoteReplacement(entry.getValue()));
    }
                
    return newS;
  }
  
  private def clean(s:String):String = {
    var clean = s.replaceAll("\\<.*?\\>", " ");
    clean = clean.replaceAll("\"", " ");
    clean = clean.replaceAll("-", " ");
    clean = clean.replaceAll("\\p{Pd}", " ");
    clean = clean.replaceAll("\\[UPDATE:.*\\]", " ");
    clean = clean.replaceAll("http:\\/\\/\\S*", " ");
    clean = clean.replaceAll("https:\\/\\/\\S*", " ");
    clean = clean.replaceAll("RT.*: ", "");
    clean = clean.replaceAll("^@\\S* ", "");
    clean = clean.replaceAll("  ", " ");
    
    return clean;
  }
  
  private def cleanFurther(s:String):String = {
	var clean = s.replaceAll("#", "");
                
	return clean;
  }
  
  private def storyValid(status:Status, me:Long):Boolean = {
    
    if(status.getUser().getId == me)
      return false
    else if(status.getInReplyToScreenName() != null)
      return false
    else
      return true
  }
  
  
  def setLinkData(abs:Abstraction){
    if (abs.title != null && abs.images != null) {
      if (abs.images.size() > 0) {
        images.clear();
        images.add(abs.images.get(0));
        linkScore += 4000
      }
      if(!abs.title.equalsIgnoreCase("")){
        description = abs.title;
        tweet = name + " shared a link titled, " + description + ".";
      }
    }
  }
  
  def calcScore():Int = {
    val now = new Date()
    val dateScore = Math.abs(date.getTime() - now.getTime())/60000
    
    val rtScore = retweet_count
    val favScore = favorite_count
    
    score = (rtScore + favScore + dateScore.toInt)
    score
  }
}