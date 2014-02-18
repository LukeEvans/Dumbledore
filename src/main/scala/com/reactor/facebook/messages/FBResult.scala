package com.reactor.facebook.messages

import com.fasterxml.jackson.databind.ObjectMapper

class FBResult {
  var status = "OK"
  var response_type:String = null
  var response_time:Double = 0
  var data:Object = null
  private var start = System.currentTimeMillis()
  
  def finish(data:Object, mapper:ObjectMapper):String = {
    this.data = data
    response_time = System.currentTimeMillis() - start
    mapper.writeValueAsString(this)
  }
}