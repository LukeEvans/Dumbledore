package com.reactor.prime.user

import com.reactor.dumbledore.utilities.Location

class UserCredentials {
  	var loc:String = null
	var location:Location = null
	var udid:String = null
	var timezone_offset:Int = 0
	
	def this(udid:String){
	  this()
	  this.udid = udid
  	}

    def setLoc(loc:String):UserCredentials = {
	  if(loc != null && !loc.equalsIgnoreCase("")){
		  this.loc = loc
	  }
	  this
	}
    
    def setLocation(lat:Double, long:Double):UserCredentials = {
      this.location = Location(lat, long)
      this
    }
    
    def setTzOffset(timezone_offset:Int):UserCredentials = {
	  this.timezone_offset = timezone_offset
	  this
	}
}