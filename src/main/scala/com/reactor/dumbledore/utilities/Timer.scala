package com.reactor.dumbledore.utilities

class Timer {
	val start = System.currentTimeMillis()
	
	def stopAndPrint(name:String) = {
	  println(name+ ": " + (System.currentTimeMillis() - start))
	}
}