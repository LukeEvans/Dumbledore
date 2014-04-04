package com.reactor.dumbledore.prime.services.twitter

import scala.collection.mutable.ListBuffer
import org.joda.time.DateTime
import com.reactor.dumbledore.utilities.Tools
import com.reactor.store.MongoDB
import com.reactor.dumbledore.utilities.Conversions
import org.elasticsearch.common.joda.time.format.DateTimeFormat
import com.mongodb.casbah.commons.MongoDBObject

object TwitterMongoCache {
  
  val cacheCollection = "reactor-twitter_cache"
  val mongo = new MongoDB
  
  
  def cache(token:String, data:ListBuffer[TwitterStory]) = {
    
    val cacheSet = new TwitterCacheSet(token, data)
    
    val cache = cacheSet.asDBObject
    
    //mongo.insert(cache, cacheCollection) 
  }
   
  
  def checkCache(token:String):Option[ListBuffer[Object]] = {
    
    val cachedSet = mongo.findOneSimple("token", token, "cache-twitter_story_sets")
    
    if(cachedSet == null)
      return None
    
    val cacheJson = Tools.objectToJsonNode(cachedSet)
    
    val dateString = if(cacheJson.has("cacheDate")) cacheJson.get("cacheDate").get("$date").asText else null   
    val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    
    val dateTime = formatter.parseDateTime(dateString)
    
    val nowDateTime = new DateTime
    
    if(nowDateTime.getMillis() - dateTime.getMillis() < 7200000){
      
      if(cacheJson.has("cachedStorySet")){
        if(cacheJson.get("cachedStorySet").has("data")){
        	
          val objectList = Conversions.nodeToObjectList(cacheJson.get("cachedStorySet").get("data"))
          
          return Some(objectList)
        }
      }
      
      return None //Return the ListBuffer of cached Twitter Stories
    }
    
    return None
  }
  
}


class TwitterCacheSet{
  var token:String = null
  var date:DateTime = new DateTime
  var data:ListBuffer[Object] = null
  
  def this(token:String, dat:ListBuffer[TwitterStory]){
    this()
    this.token = token
    this.data = data
  }
  
  def asDBObject() ={
    val dbObj = MongoDBObject

    
    dbObj
  }
}