package com.reactor.dumbledore.messaging

import com.github.nscala_time.time.Imports._

class RequestTime {
  var dateTime = DateTime.now
  var offsetTime:DateTime = null
  
  def this(timezone:String){
    this()
    println(timezone)
    var offset = 0
    if(timezone != null && !timezone.equalsIgnoreCase(""))
      offset = timezone.toInt / 3600
    println(offset)
    println(DateTime.now)
    val dtz = DateTimeZone.forOffsetHours(offset);
    println
    offsetTime = new DateTime(dtz);
    println(offsetTime)
  }
}