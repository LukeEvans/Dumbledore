package com.reactor.dumbledore.prime.services.sports

import scala.collection.mutable.ListBuffer
import org.joda.time.DateTime
import com.reactor.dumbledore.utilities.Tools
import java.security.MessageDigest

object StatsAPI {
  
  val apiKey = "72w4c7bu5vsrqh5gqjhe7uqs"
  val apiSecret = "eVWy7hPdqj"
    
  val avsID = "4970"
  val nhlUri = "http://api.stats.com/v1/stats/hockey/nhl/events/teams/"
    
    
  def getEvents(days:Int):ListBuffer[ScoreCard] = {
    
    val now = new DateTime
    println((now.getMillis() / 1000 ).toString)
    val sigHash = md5(apiKey + apiSecret + now.getMillis().toString)
    
    for(i <- 0 to days -1){
      
      var day = now.plusDays(i)
      
      var dateString = day.year().get() + "-" + day.monthOfYear().get() + "-" + day.dayOfMonth().get()
      
      var response = Tools.fetchURL(nhlUri + avsID + 
          "?date=" + dateString +
          "&accept=json" +
          "&api_key=" + apiKey +
          "&sig=" + sigHash)
      
    }
    
    null
  }
  
  def md5(s:String):String = MessageDigest.getInstance("MD5").digest(s.getBytes).map("%02X".format(_)).mkString
  
}