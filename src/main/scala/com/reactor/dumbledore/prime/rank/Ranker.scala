package com.reactor.dumbledore.prime.rank

import com.reactor.patterns.pull.FlowControlActor
import com.reactor.patterns.pull.FlowControlArgs
import com.reactor.dumbledore.prime.PrimeSet
import akka.actor.ActorRef
import com.reactor.dumbledore.data.ListSet
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Map
import com.reactor.dumbledore.notifications.time.Time
import org.joda.time.DateTime
import com.reactor.dumbledore.notifications.time.Date
import com.reactor.dumbledore.messaging.PrimeRankContainer
import com.reactor.store.MongoDB
import com.reactor.dumbledore.utilities.Tools

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
  
  def rankSet(primeSet:PrimeSet, time:DateTime, origin:ActorRef){
    
    val rankedSet = new PrimeSet
    
    primeSet.getSet().map{
      set => rankedSet += rank(set, time)
    }
    
    rankedSet.sort.map{
      set => println("Set ID: " + set.card_id + "  Set Rank: " + set.rank)
    }
    
    reply(origin, rankedSet)
    complete()
  }
  
  def rank(set:ListSet[Object], now:DateTime):ListSet[Object] = {
    
    val date = new Date(now)
    
    ranks.get(set.card_id) match{
      case Some(rank) =>
        val score = rank.getScore(date)
        return ListSet(set.card_id, score, set.set_data)
      case None => 
        return set
    }
  }
  
  
  /** Initialize Ranking Configs
   */
  private def initRanks():Map[String, Rank] = {
    
    var rankConfigs = Map[String, Rank]()
    
    val weatherRank = Rank("weather")
    					.addRange(40, Time(0, 0), Time(11, 59), 0, 6)
    					.addRange(30, Time(12, 0), Time(14, 59), 0, 6)
    					.addRange(10, Time(15, 0), Time(17, 59), 0, 6)    					
    					.addRange(30, Time(18, 0), Time(19, 59), 0, 6)    					
    					.addRange(40, Time(20, 0), Time(23, 59), 0, 6)    					
        
    rankConfigs.put("weather", weatherRank)
    
    val stocksRank = new StaticRank("stocks")
    					   .addRange(0, Time(0, 0), Time(9, 29), 0, 6)
    					   .addRange(30, Time(9, 30), Time(11, 59), 0, 6)
    					   .addRange(25, Time(12, 0), Time(16, 59), 0, 6)
    					   .addRange(40, Time(17, 0), Time(18, 59), 0, 6)
    					   .addRange(30, Time(19, 0), Time(20, 59), 0, 6)
    					   .addRange(0, Time(21, 0), Time(23, 59), 0, 6)
    
    rankConfigs.put("stocks", stocksRank)
    
    val nearbyPlacesRank = Rank("nearby_places")
    				.addRange(0, Time(0, 0), Time(5, 59), 0, 6)
    				.addRange(30, Time(6, 0), Time(9, 59), 0, 6)
    				.addRange(15, Time(10, 0), Time(10, 59), 0, 6)
    				.addRange(45, Time(11, 0), Time(13, 59), 0, 6)
    				.addRange(20, Time(14, 0), Time(16, 59), 0, 6)
    				.addRange(35, Time(17, 0), Time(17, 59), 0, 6)
    				.addRange(45, Time(18, 0), Time(19, 59), 0, 6)
    				.addRange(20, Time(20, 0), Time(23, 59), 0, 6)
    				
    rankConfigs.put("nearby_places", nearbyPlacesRank)
    
    val nearbyPhotosRank = Rank("nearby_photos")
    						.addRange(0, Time(0, 0), Time(12, 59), 0, 6)
    						.addRange(10, Time(13, 0), Time(14, 59), 0, 6)
    						.addRange(15, Time(15, 0), Time(17, 59), 0, 6)
    						.addRange(0, Time(18, 0), Time(23, 59), 0, 6)
    
    rankConfigs.put("nearby_photos", nearbyPhotosRank)
    
    val facebookRank = Rank("facebook")
    						.addRange(30, Time(0, 0), Time(5, 59), 0, 6)
    						.addRange(25, Time(6, 0), Time(9, 59), 0, 6)
    						.addRange(20, Time(10, 0), Time(11, 59), 0, 6)
    						.addRange(40, Time(12, 0), Time(14, 59), 0, 6)
    						.addRange(30, Time(15, 0), Time(17, 59), 0, 6)
    						.addRange(15, Time(18, 0), Time(20, 59), 0, 6)
    						.addRange(30, Time(21, 0), Time(23, 59), 0, 6)
    
    rankConfigs.put("facebook", facebookRank)
    
    val twitterRank = Rank("facebook")
    						.addRange(30, Time(0, 0), Time(5, 59), 0, 6)
    						.addRange(25, Time(6, 0), Time(9, 59), 0, 6)
    						.addRange(20, Time(10, 0), Time(11, 59), 0, 6)
    						.addRange(40, Time(12, 0), Time(14, 59), 0, 6)
    						.addRange(30, Time(15, 0), Time(17, 59), 0, 6)
    						.addRange(15, Time(18, 0), Time(20, 59), 0, 6)
    						.addRange(30, Time(21, 0), Time(23, 59), 0, 6)
    						
    rankConfigs.put("twitter", twitterRank)
    
    val youtubeRank = Rank("youtube")
    						.addRange(15, Time(0, 0), Time(5, 59), 0, 6)
    						.addRange(5, Time(6, 0), Time(19, 59), 0, 6)
    						.addRange(15, Time(20, 0), Time(23, 59), 0, 6)
    					   
    rankConfigs.put("youtube", youtubeRank)
    
    val comicsRank = Rank("comics")
    						.addRange(5, Time(0, 0), Time(5, 59), 0, 6)
    						.addRange(10, Time(6, 0), Time(11, 59), 0, 6)
    						.addRange(0, Time(12, 0), Time(20, 59), 0, 6)
    						.addRange(5, Time(21, 0), Time(23, 59), 0, 6)
    	
    rankConfigs.put("comics", comicsRank)
    
    rankConfigs = setNewsConfigs(rankConfigs)
    
    return rankConfigs
  }
  
  private def setNewsConfigs(configs:Map[String, Rank]):Map[String, Rank] = {
    
    val list = mongo.findAll("reactor-news-feeds")
    val sources = ListBuffer[String]()

    list.map{
      obj =>
        
        var json  = Tools.objectToJsonNode(obj)
        
        if(json.has("feed_id"))
          sources += json.get("feed_id").asText()
    }
    
    sources.map{
      sourceId =>
        val sourceRank = Rank(sourceId)
        				   .addRange(25, Time(0,0), Time(5, 59), 0, 6)
        				   .addRange(20, Time(6,0), Time(9, 59), 0, 6)
        				   .addRange(25, Time(10,0), Time(10, 59), 0, 6)
        				   .addRange(35, Time(11,0), Time(14, 59), 0, 6)
        				   .addRange(40, Time(15,0), Time(16, 59), 0, 6)
        				   .addRange(20, Time(17,0), Time(19, 59), 0, 6)
        				   .addRange(25, Time(20,0), Time(23, 59), 0, 6)
        configs.put(sourceId, sourceRank)
    }
    
    return configs
  }
  
}