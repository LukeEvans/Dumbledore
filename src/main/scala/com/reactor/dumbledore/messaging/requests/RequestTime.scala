package com.reactor.dumbledore.messaging.requests

import com.github.nscala_time.time.Imports._

class RequestTime {
  var dateTime = new DateTime(DateTimeZone.forOffsetHours(0))
  var offsetTime:DateTime = null
  
  def this(timezone:String){
    this()
    var offset = 0
    
    if(timezone != null && !timezone.equalsIgnoreCase(""))
      offset = timezone.toInt / 3600
      
    val dtz = DateTimeZone.forOffsetHours(offset);
    offsetTime = new DateTime(dtz);
  }
}