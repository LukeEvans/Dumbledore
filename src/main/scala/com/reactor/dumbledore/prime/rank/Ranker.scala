package com.reactor.dumbledore.prime.rank

import com.reactor.patterns.pull.FlowControlActor
import com.reactor.patterns.pull.FlowControlArgs
import com.reactor.dumbledore.prime.PrimeSet
import akka.actor.ActorRef
import com.reactor.dumbledore.prime.data.ListSet
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Map
import com.reactor.dumbledore.prime.notifications.time.Time
import org.joda.time.DateTime
import com.reactor.dumbledore.prime.notifications.time.Date
import com.reactor.dumbledore.messaging.PrimeRankContainer
import com.reactor.store.MongoDB
import com.reactor.dumbledore.utilities.Tools
import com.reactor.dumbledore.prime.constants._

class SetRankerActor(args:FlowControlArgs) extends FlowControlActor(args) {

  private val mongo = new MongoDB
  val ranks = initRanks()

  ready()
  
  override def preStart = println("SetRankerActor starting...")
  override def postStop = println("SetRankerActor terminated")
  
  
  def receive = {
    
    case PrimeRankContainer(primeSet, time) => rankSet(primeSet, time,  sender)
    
    case a:Any => println("SetRankerActor- Unknown message received: " + a)
  }
  
  
  /** Rank each ListSet in PrimeSet and Send back to PrimeActor
   */
  private def rankSet(primeSet:PrimeSet, time:DateTime, origin:ActorRef){
    
    val rankedSet = new PrimeSet
    
    primeSet.getSet().foreach{ 
      set =>
        val ranked = rankListSet(set, time)
        
        if(ranked.score > 0)
        	rankedSet += ranked
    }
    
    reply(origin, rankedSet)
    complete()
  }
  
  
  /** Rank ListSet base on DateTime and RankConfig
   */
  private def rankListSet(set:ListSet[Object], now:DateTime):ListSet[Object] = {
    
    val date = new Date(now)
    
    ranks.get(set.card_id) match{
   
      case Some(rank) =>

        val newSet = ListSet(set.card_id, set.set_data)
        
        newSet.setScore(date, rank)
        
        return newSet
        
      case None => 
        return set
    }
  }
  
  
  /** Initialize Ranking Configs
   */
  private def initRanks():Map[String, Rank] = {
    
    var rankConfigs = Map[String, Rank]()
    
    val weatherRank = Rank(Prime.WEATHER)
    					.addRange(40, Time(0, 0), Time(11, 59), Day.MONDAY, Day.SUNDAY)
    					.addRange(30, Time(12, 0), Time(14, 59), Day.MONDAY, Day.SUNDAY)
    					.addRange(10, Time(15, 0), Time(17, 59), Day.MONDAY, Day.SUNDAY)    					
    					.addRange(30, Time(18, 0), Time(19, 59), Day.MONDAY, Day.SUNDAY)    					
    					.addRange(40, Time(20, 0), Time(23, 59), Day.MONDAY, Day.SUNDAY)    					
        
    rankConfigs.put(Prime.WEATHER, weatherRank)
    
    val stocksRank = new StaticRank(Prime.STOCKS)
     					   .addRange(30, Time(0, 0), Time(1, 59), Day.MONDAY, Day.SUNDAY)
    					   .addRange(0, Time(2, 0), Time(4, 59), Day.MONDAY, Day.SUNDAY)
    					   .addRange(0, Time(5, 0), Time(14, 29), Day.MONDAY, Day.SUNDAY)
    					   .addRange(30, Time(14, 30), Time(16, 59), Day.MONDAY, Day.SUNDAY)
    					   .addRange(25, Time(17, 0), Time(21, 59), Day.MONDAY, Day.SUNDAY)
    					   .addRange(40, Time(22, 0), Time(23, 59), Day.MONDAY, Day.SUNDAY)
    
    rankConfigs.put(Prime.STOCKS, stocksRank)
    
    val nearbyPlacesRank = Rank(Prime.NEARBY_PLACES)
    				.addRange(0, Time(0, 0), Time(5, 59), Day.MONDAY, Day.SUNDAY)
    				.addRange(30, Time(6, 0), Time(9, 59), Day.MONDAY, Day.SUNDAY)
    				.addRange(15, Time(10, 0), Time(10, 59), Day.MONDAY, Day.SUNDAY)
    				.addRange(45, Time(11, 0), Time(13, 59), Day.MONDAY, Day.SUNDAY)
    				.addRange(20, Time(14, 0), Time(16, 59), Day.MONDAY, Day.SUNDAY)
    				.addRange(35, Time(17, 0), Time(17, 59), Day.MONDAY, Day.SUNDAY)
    				.addRange(45, Time(18, 0), Time(19, 59), Day.MONDAY, Day.SUNDAY)
    				.addRange(20, Time(20, 0), Time(23, 59), Day.MONDAY, Day.SUNDAY)
    				
    rankConfigs.put(Prime.NEARBY_PLACES, nearbyPlacesRank)
    
    val nearbyPhotosRank = Rank(Prime.NEARBY_PHOTOS)
    						.addRange(0, Time(0, 0), Time(12, 59), Day.MONDAY, Day.SUNDAY)
    						.addRange(10, Time(13, 0), Time(14, 59), Day.MONDAY, Day.SUNDAY)
    						.addRange(15, Time(15, 0), Time(17, 59), Day.MONDAY, Day.SUNDAY)
    						.addRange(0, Time(18, 0), Time(23, 59), Day.MONDAY, Day.SUNDAY)
    
    rankConfigs.put(Prime.NEARBY_PHOTOS, nearbyPhotosRank)
    
    val facebookRank = new DumbRank(Prime.FACEBOOK)
    						.addRange(30, Time(0, 0), Time(5, 59), Day.MONDAY, Day.SUNDAY)
    						.addRange(25, Time(6, 0), Time(9, 59), Day.MONDAY, Day.SUNDAY)
    						.addRange(20, Time(10, 0), Time(11, 59), Day.MONDAY, Day.SUNDAY)
    						.addRange(40, Time(12, 0), Time(14, 59), Day.MONDAY, Day.SUNDAY)
    						.addRange(30, Time(15, 0), Time(17, 59), Day.MONDAY, Day.SUNDAY)
    						.addRange(15, Time(18, 0), Time(20, 59), Day.MONDAY, Day.SUNDAY)
    						.addRange(30, Time(21, 0), Time(23, 59), Day.MONDAY, Day.SUNDAY)
    
    rankConfigs.put(Prime.FACEBOOK, facebookRank)
    
    val twitterRank = new DumbRank(Prime.TWITTER)
    						.addRange(30, Time(0, 0), Time(5, 59), Day.MONDAY, Day.SUNDAY)
    						.addRange(25, Time(6, 0), Time(9, 59), Day.MONDAY, Day.SUNDAY)
    						.addRange(20, Time(10, 0), Time(11, 59), Day.MONDAY, Day.SUNDAY)
    						.addRange(40, Time(12, 0), Time(14, 59), Day.MONDAY, Day.SUNDAY)
    						.addRange(30, Time(15, 0), Time(17, 59), Day.MONDAY, Day.SUNDAY)
    						.addRange(15, Time(18, 0), Time(20, 59), Day.MONDAY, Day.SUNDAY)
    						.addRange(30, Time(21, 0), Time(23, 59), Day.MONDAY, Day.SUNDAY)
    						
    rankConfigs.put(Prime.TWITTER, twitterRank)
    
    val videosRank = Rank(Prime.POPULAR_VIDEOS)
    						.addRange(15, Time(0, 0), Time(5, 59), Day.MONDAY, Day.SUNDAY)
    						.addRange(5, Time(6, 0), Time(19, 59), Day.MONDAY, Day.SUNDAY)
    						.addRange(15, Time(20, 0), Time(23, 59), Day.MONDAY, Day.SUNDAY)
    					   
    rankConfigs.put(Prime.POPULAR_VIDEOS, videosRank)
    
    val comicsRank = Rank(Prime.COMICS)
    						.addRange(5, Time(0, 0), Time(5, 59), Day.MONDAY, Day.SUNDAY)
    						.addRange(10, Time(6, 0), Time(11, 59), Day.MONDAY, Day.SUNDAY)
    						.addRange(0, Time(12, 0), Time(20, 59), Day.MONDAY, Day.SUNDAY)
    						.addRange(5, Time(21, 0), Time(23, 59), Day.MONDAY, Day.SUNDAY)
    	
    rankConfigs.put(Prime.COMICS, comicsRank)
    
    val rentalsRank = Rank(Prime.TOP_RENTALS)
    						.add247(5)
    												
    rankConfigs.put(Prime.TOP_RENTALS, rentalsRank)
    
    val songsRank = Rank(Prime.TOP_SONGS)
    						.add247(5)
    						
    rankConfigs.put(Prime.TOP_SONGS, songsRank)
    
    
    val donationsRank = Rank(Prime.DONATIONS)
    						.add247(5)
    						
    rankConfigs.put(Prime.DONATIONS, donationsRank)
    
    
    rankConfigs = setNewsConfigs(rankConfigs)
    
    return rankConfigs
  }
  
  
  /** Set News Ranking configurations */
  private def setNewsConfigs(configs:Map[String, Rank]):Map[String, Rank] = {
    
    val list = mongo.findAll("reactor-news-feeds")
    val sources = ListBuffer[String]()
    val MONDAY = Day.MONDAY
    val SUNDAY = Day.SUNDAY

    
    list.foreach{
      obj =>  
        var json  = Tools.objectToJsonNode(obj)
        
        if(json.has("feed_id"))
          sources += json.get("feed_id").asText()
    }       
    
    sources.foreach{
      sourceId =>
        val sourceRank = new TimedRank(sourceId)
        				   .addRange(25, Time(0,0), Time(5, 59), MONDAY, SUNDAY)
        				   .addRange(20, Time(6,0), Time(9, 59), MONDAY, SUNDAY)
        				   .addRange(25, Time(10,0), Time(10, 59), MONDAY, SUNDAY)
        				   .addRange(35, Time(11,0), Time(14, 59), MONDAY, SUNDAY)
        				   .addRange(40, Time(15,0), Time(16, 59), MONDAY, SUNDAY)
        				   .addRange(20, Time(17,0), Time(19, 59), MONDAY, SUNDAY)
        				   .addRange(25, Time(20,0), Time(23, 59), MONDAY, SUNDAY)
        				   
        configs.put(sourceId, sourceRank)
    }
    
    return configs
  }
  
}