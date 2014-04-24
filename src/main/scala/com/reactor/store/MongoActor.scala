package com.reactor.store

import com.reactor.patterns.pull.FlowControlActor
import com.reactor.patterns.pull.FlowControlArgs
import com.reactor.dumbledore.messaging.MongoQuery
import akka.actor.ActorRef


class MongoActor(args:FlowControlArgs) extends FlowControlActor(args) {

  private val mongo = new MongoDB2("reactor-news")
  
  ready
  
  override def preStart() = println("MongoActor Started")
  
  def receive() = {
    
    case m:MongoQuery =>
    	handleMongoQuery(m, sender)
      
    case a:Any => println("MongoActor: Unknown message received - " + a)
  }
  
  def handleMongoQuery(mongoQuery:MongoQuery, origin:ActorRef){
    
    val data = mongo.find(mongoQuery.query, mongoQuery.limit)
    
    reply(origin, data)
    complete()
    
  }
}