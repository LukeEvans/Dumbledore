package com.reactor.dumbledore.prime.services.donations

import com.fasterxml.jackson.databind.JsonNode

class DonationProject {
  
  val story_type:String = "donation"
  val source:String = "Donors Choose"
  var id:String = null
  var proposalURL:String = null
  var fundURL:String = null
  var imageURL:String = null
  var title:String = null
  var description:String = null
  var details:String = null
  var percent_funded:Double = 0
  var cost_to_complete:Double = 0
  var total_price:Double = 0
  var speech:String = null
  
  

  def this(node:JsonNode){
    this()
    
    if(node.has("id"))
      id = node.get("id").asText()
    
    if(node.has("proposalURL"))
      proposalURL = node.get("proposalURL").asText()
      
    if(node.has("fundURL"))
      fundURL = node.get("fundURL").asText()
      
    if(node.has("imageURL"))
      imageURL = node.get("imageURL").asText()
      
    if(node.has("title")){
      title = node.get("title").asText()
      speech = "Here is a donation project for " + title + "."
    }
      
    if(node.has("shortDescription"))
      description = node.get("shortDescription").asText()
    
    if(node.has("fulfillment"))
      details = node.get("fulfillment").asText()
      
    if(node.has("percentFunded"))
      percent_funded = node.get("percentFunded").asDouble()
      
    if(node.has("costToComplete"))
      cost_to_complete = node.get("costToComplete").asDouble()
      
    if(node.has("totalPrice"))
      total_price = node.get("totalPrice").asDouble()
    
  }
}