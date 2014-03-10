package com.reactor.dumbledore.data

import java.util.ArrayList
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Map
import com.fasterxml.jackson.databind.JsonNode
import scala.collection.JavaConversions._
import com.reactor.prime.user.UserCredentials

/** String mapped to ListBuffer */
case class ListSet[T](card_id:String, rank:Int, set_data:ListBuffer[T])


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
    }
  }
}
