package com.reactor.dumbledore.messaging

import java.util.ArrayList
import com.fasterxml.jackson.databind.ObjectMapper

class Result {
	var status ="OK"
	var response_type:String = null
	var response_time:Double = 0
	var data:ArrayList[ArrayList[Object]] = new ArrayList[ArrayList[Object]]
	private var start = System.currentTimeMillis()
	
	def finish(data: ArrayList[ArrayList[Object]], mapper:ObjectMapper):String = {
	  this.data = data
	  response_time = System.currentTimeMillis() - start
	  mapper.writeValueAsString(this)
	}
}