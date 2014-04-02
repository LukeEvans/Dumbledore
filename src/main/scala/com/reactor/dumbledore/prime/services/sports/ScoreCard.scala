package com.reactor.dumbledore.prime.services.sports

import com.fasterxml.jackson.databind.JsonNode

class ScoreCard {

  var data:JsonNode = null
  
  def this(node:JsonNode){
    this()
    
    data = node
  }
}