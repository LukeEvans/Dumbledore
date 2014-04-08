package com.reactor.dumbledore.prime.services.geolocation

import com.fasterxml.jackson.databind.JsonNode

import com.reactor.dumbledore.utilities.Tools

object GeoLocation {
  val username = "keco1249"
  val reverseGeoAPIURL = "http://api.geonames.org/findNearestAddressJSON"
  val stateKey = "adminCode1"
    
  
  def reverseStateLookup(lat:Double, long:Double):String = {
    
    val node = reverseLookup(lat, long)
    
    if(node != null && node.has("address")){
      
      if(node.get("address").has(stateKey))
    	return node.get("address").get(stateKey).asText()
    	  
    }
    
    return null
  }
  
  def reverseLookup(lat:Double, long:Double):JsonNode = {
    
    val response = Tools.fetchURL(reverseGeoAPIURL + "?" +
    						"&lat=" + lat +
    						"&lng=" + long +
    						"&username=" + username)
    
    return response
  }

}