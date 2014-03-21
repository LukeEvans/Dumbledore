package com.reactor.dumbledore.prime.rank

import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Map
import com.reactor.dumbledore.notifications.time._

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
  
  def getScore(time:Date):Int = {
    rankedTimes.map{
      timeRange =>{
    	if(time.isInRange(timeRange.range)){
    	  return timeRange.score
    	}
      }
    }
    return 0
  }
}

class StaticRank(id:String) extends Rank(id){
  
  override def getScore(time:Date):Int = {
    rankedTimes.map{
      timeRange =>{
        if(time.isInOffsetRange(timeRange.range)){
          return timeRange.score
        }
      }
    }
    return 0
  }
}

case class RankConfig(score:Int, range:TimeRange)