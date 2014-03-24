package com.reactor.dumbledore.prime.rank

import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Map
import com.reactor.dumbledore.notifications.time._
import com.reactor.dumbledore.prime.data.story.KCStory
import com.reactor.dumbledore.prime.data.story.KCStory


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
  
  def getScore(time:Date, data:ListBuffer[Object]):Int = {
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
  
  override def getScore(time:Date, data:ListBuffer[Object]):Int = {
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
  
  override def getScore(time:Date, data:ListBuffer[Object]):Int = {
    
    if(data == null || data.isEmpty)
      return 0
   
    var mostRecent:KCStory = null
      
    data(0) match{
      case story:KCStory => mostRecent = story
      case _:Any => return 0
    }
    
    rankedTimes.foreach{
      timeRange =>{
        if(time.isInOffsetRange(timeRange.range)){
          
          println("")
          val recentScore = mostRecent.calcRecentScore
          
          println("range score: " + timeRange.score)
          println("recent:   " + recentScore)
          
          if(recentScore < 0){
            println("neg       " + timeRange.score)
            return timeRange.score
          }
          else if(recentScore > 120){
            println(" 5")
            return 5  
          }
          
          else{
            val fract = ((120 - recentScore).toDouble/120)
            println("fract:     " + fract)
            val newScore = (fract * timeRange.score)
            println("new score: " + newScore.toInt)
            return newScore.toInt
          }

        }
      }
    }
    return 0
  }
}


/** RankConfig
 */
case class RankConfig(score:Int, range:TimeRange)