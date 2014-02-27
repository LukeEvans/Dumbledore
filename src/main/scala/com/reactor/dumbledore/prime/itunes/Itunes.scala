package com.reactor.dumbledore.prime.itunes

import scala.collection.mutable.ListBuffer
import com.reactor.dumbledore.prime.cards.Card
import com.fasterxml.jackson.databind.JsonNode
import scala.collection.JavaConversions._
import com.reactor.dumbledore.utilities.Tools

class ItunesMovie extends Card{
  def this(node:JsonNode){
    this()
  }
}

object Itunes {
  val baseRSSURL = "https://itunes.apple.com/us/rss/"
    
  def getTopMovies():ListBuffer[Card] = {
    
    val r = funcOf(stringFunc)
    //extractData(Tools.fetchURL(""), stringFunc)
    
    extractData(Tools.fetchURL(""), (new ItunesMovie(_)))
    
    null
  }
  
//  def extractData(dataNode:JsonNode, constr:(String) => Card){
//    
//  }
  
  def extractData(dataNode:JsonNode, constructor:(JsonNode) => Card):ListBuffer[Card] = {
    val cards = ListBuffer[Card]()
    
    for(node <- dataNode){
    	cards.add(constructor(node))
    }
    
    null
  }
  
  def constructor(constr:(JsonNode) => Card, node:JsonNode):Card = constr(node)
  
  def stringFunc(s:String):Card ={
    null
  }
  
  def funcOf(params:(String) => Card) = () => params
  
}