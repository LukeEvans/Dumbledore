package com.reactor.dumbledore.data

import java.util.ArrayList
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Map
import com.fasterxml.jackson.databind.JsonNode
import scala.collection.JavaConversions._
import com.reactor.prime.user.UserCredentials
import com.reactor.dumbledore.prime.rank.Rank
import com.reactor.dumbledore.notifications.time.Date
import com.reactor.dumbledore.prime.data.story.KCStory

/** String mapped to ListBuffer */
case class ListSet[T](card_id:String, rank:Int, set_data:ListBuffer[T]){
  
  var score = 0
  
  def setScore(date:Date, rank:Rank){
    score = rank.getScore(date, set_data.asInstanceOf[ListBuffer[Object]], card_id)
  }
  
}


/** Parameters Map[String,String] */
case class Parameters(map:Map[String, String]){
  
  def this(creds:UserCredentials){
    this(Map[String,String]())
    if(creds.udid != null)
      map.put("udid", creds.udid)
    if(creds.location != null){
      map.put("lat", creds.location.lat.toString)
      map.put("long", creds.location.long.toString)     
      map.put("loc", creds.location.lat.toString+","+creds.location.long.toString)
      map.put("timezone_offset", creds.timezone_offset.toString)
    }
  }
}
