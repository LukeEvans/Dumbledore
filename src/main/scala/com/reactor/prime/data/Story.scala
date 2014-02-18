package com.reactor.prime.data

class Story {
	var db:String = null
	var id:String = null
	var order:Int = 0
	var story_type:String = null
	var header:String = null
	var description:String = null
	var speech:String = null
	var score:Int = 0
	var valid:Boolean = false
	//private var entities:ArrayList[Entity] = null
	
	def updateSpeech(speech:String) = this.speech = speech
	
}