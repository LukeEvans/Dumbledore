package com.reactor.dumbledore.prime.stocks

import com.reactor.patterns.pull.FlowControlActor
import com.reactor.patterns.pull.FlowControlArgs
import akka.actor.ActorRef
import com.reactor.store.MongoStore
import com.reactor.dumbledore.utilities.Tools

class StockMessage extends Serializable

trait serial
case class Read(udid:String) extends serial
case class Update(udid:String, ticker:String) extends serial
case class Create(udid:String) extends serial
case class Delete(udid:String, ticker:String) extends serial
case class Destroy(udid:String) extends serial

class StocksActor(args:FlowControlArgs) extends FlowControlActor(args) {
  val mongo = new MongoStore("winston-users")
  
  def receive = {
    case Read(udid) => read(udid, sender)
      
  }
  
  def read(udid:String, origin:ActorRef){
    val user = Tools.objectToJsonNode(mongo.findOneSimple("UDID", udid))
  }
}