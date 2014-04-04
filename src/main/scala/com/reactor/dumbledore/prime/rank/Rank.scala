package com.reactor.dumbledore.prime.rank

import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Map
import com.reactor.dumbledore.prime.notifications.time._
import com.reactor.dumbledore.prime.data.story.KCStory
import com.reactor.dumbledore.prime.data.story.KCStory
import com.reactor.dumbledore.utilities.Tools
import com.reactor.dumbledore.prime.constants.Day


/** Rank Class
 */
case class Rank(id:String) {

  var rankedTimes = ListBuffer[RankConfig]()
  
  
  def addTimeRange(score:Int, start:Date, stop:Date):Rank = {
    rankedTimes += RankConfig(score, new TimeRange(start, stop, None))
    this
  }
  
  
  def addRange(score:Int, startTime:Time, stopTime:Time, start:Int, 
      stop:Int):Rank ={
    
    for(i <- start to stop){
     addTimeRange(score, Date(startTime, i), Date(stopTime, i)) 
    }
    this
  }
  
  
  /** Add 24/7 TimeRange */
  def add247(score:Int):Rank = {
    
    addRange(score, Time(0,0), Time(23,59), Day.MONDAY, Day.SUNDAY)
    this
  }
  
  
  def getScore(time:Date, data:ListBuffer[Object], id:String):Int = {
    rankedTimes.foreach{
      timeRange =>{
    	if(time.isInRange(timeRange.range)){
    	  return timeRange.score
    	}
      }
    }
    return 0
  }
}


/** Static Rank Class
 */
class StaticRank(id:String) extends Rank(id){
  
  override def getScore(time:Date, data:ListBuffer[Object], id:String):Int = {
    rankedTimes.foreach{
      timeRange =>{
        if(time.isInOffsetRange(timeRange.range)){
          return timeRange.score
        }
      }
    }
    return 0
  }
}


/** Timed RankClass
 */
class TimedRank(id:String) extends Rank(id){
  
  override def getScore(time:Date, data:ListBuffer[Object], id:String):Int = {
    
    if(data == null || data.isEmpty)
      return super.getScore(time, data, id)
   
    var mostRecent:KCStory = null
      
    data(0) match{
      case story:KCStory => mostRecent = story
      case _:Any => return 0
    }
    
    rankedTimes.foreach{
      timeRange =>{
        if(time.isInRange(timeRange.range)){
          
          val recentScore = mostRecent.calcRecentScore
          
          if(recentScore < 0){
            
            return timeRange.score
          }
          else if(recentScore > 120){
            
            return 5  
          }
          else{
            
            val fract = ((120 - recentScore).toDouble/120)
            val newScore = (fract * timeRange.score)

            return newScore.toInt
          }

        }
      }
    }
    return 0
  }
}

class DumbRank(id:String) extends Rank(id){
  override def getScore(time:Date, data:ListBuffer[Object], id:String):Int = {
    rankedTimes.foreach{
      timeRange =>{
    	if(time.isInRange(timeRange.range)){
    	  
    	  if(Tools.randomInt(0, 1) == 1 )
    	    return timeRange.score
    	  else
    		return timeRange.score - 10
    	}	
      }
    }
    return 0
  }
}


/** RankConfig
 */
case class RankConfig(score:Int, range:TimeRange)
