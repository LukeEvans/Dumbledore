package com.reactor.dumbledore.prime.twitter

import twitter4j.conf.ConfigurationBuilder
import net.sf.ehcache.search.expression.Criteria
import com.reactor.dumbledore.utilities.Tools
import com.reactor.store.MongoDB
import twitter4j.Status
import twitter4j.TwitterFactory
import twitter4j.Paging
import java.util.List
import com.fasterxml.jackson.databind.JsonNode


class TwitterAPI {
    val CONSUMER_KEY = "cpUcO0OmLkj0Newu2w7KA";
    val CONSUMER_SECRET = "cr5Bjd2rAMOlTTXZgSN7Z8qqKfAILEhV5fuPJL5bpg";
	  	  
	val mongo = new MongoDB
	
	
	def getHomeTimeLine(number:Int, userToken:String, userSecret:String):List[Status] = {
	  val builder = authBuilder(CONSUMER_KEY, CONSUMER_SECRET, token = userToken, tokenSecret = userSecret)
	  
	  getHomeStatuses(number:Int, builder, 1l)
	}
	
	/** Get Home feed of Statuses with ConfBuilder and last tweet ID */
	private def getHomeStatuses(number:Int, builder:ConfigurationBuilder, lastId:Long):List[Status] = {
	  val configuration = builder.build()
	  val factory = new TwitterFactory(configuration)
	  val twitter = factory.getInstance()
	  
	  if(lastId != 1l)
	    twitter.getHomeTimeline(new Paging(1, number, lastId))
	  else
	    twitter.getHomeTimeline(new Paging(1, number))
	}
	
	// Get List of Statuses with handle, ConfBuilder, and last tweet ID
	private def getStatuses(handle:String, builder:ConfigurationBuilder, lastId:Long):List[Status] = {
	  
	  val configuration = builder.build()
	  val factory = new TwitterFactory(configuration);
	  val twitter = factory.getInstance();
				
				//	Get a maximum of 10 tweets for the account since the last ID
	  if(lastId != 1l)
		twitter.getUserTimeline(handle, new Paging(1,10,lastId));
					
	  else
		twitter.getUserTimeline(handle, new Paging(1,10));
	}
	
	// Grab the last tweetID from mongo with handle:String
	private def getLastTweetID(handle:String):Long = {
	  var placeHolder:JsonNode = null
	  
	  if(handle != null && !handle.equalsIgnoreCase(""))
	    placeHolder = Tools.objectToJsonNode(mongo.findOneSimple("handle", handle, "twitter_placeholder"))
	  else
	    placeHolder = Tools.objectToJsonNode(mongo.findOne("twitter_placeholder"))
	  
	  if(placeHolder != null){
	    if(placeHolder.has("tID"))
	      return placeHolder.get("tID").asLong()
	  }
	  
	  return 1l
	}
	
	// Create authentication with default to company account
    private def authBuilder(key:String = CONSUMER_KEY,
        	secret:String = CONSUMER_SECRET, 
        	token:String, 
        	tokenSecret:String): ConfigurationBuilder ={
	  
      val builder = new ConfigurationBuilder();
      builder.setOAuthConsumerKey(key);
      builder.setOAuthConsumerSecret(secret);
      builder.setOAuthAccessToken(token);
      builder.setOAuthAccessTokenSecret(tokenSecret);
      builder.setUseSSL(true);
	}
}