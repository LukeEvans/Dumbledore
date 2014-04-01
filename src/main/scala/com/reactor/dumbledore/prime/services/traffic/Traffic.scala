package com.reactor.dumbledore.prime.services.traffic

import com.reactor.dumbledore.prime.data.ListSet
import scala.collection.mutable.ListBuffer


object Traffic {

  def getTraffic(rank:Int):ListSet[Object] = {
    return ListSet("traffic", rank, ListBuffer[Object](new TrafficCard))
  }
}