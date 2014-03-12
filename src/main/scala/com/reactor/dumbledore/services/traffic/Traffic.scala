package com.reactor.dumbledore.services.traffic

import com.reactor.dumbledore.data.ListSet
import scala.collection.mutable.ListBuffer

object Traffic {

  def getTraffic(rank:Int):ListSet[Object] = {
    return ListSet("traffic", rank, ListBuffer[Object](new TrafficCard))
  }
}