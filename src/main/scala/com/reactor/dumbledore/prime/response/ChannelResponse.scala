package com.reactor.dumbledore.prime.response

import scala.collection.mutable.ListBuffer
import com.reactor.dumbledore.prime.data.story.KCStory

class ChannelResponse {
  var status = "OK"
  var log:String = null
  //var count:Int = 0
  //var data
  var kcData:ListBuffer[KCStory] = null
}