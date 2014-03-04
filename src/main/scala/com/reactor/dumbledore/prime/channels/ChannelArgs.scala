package com.reactor.dumbledore.prime.channels

import com.reactor.dumbledore.notifications.NotificationArgs
import com.reactor.patterns.pull.FlowControlArgs
import akka.actor.ActorRef

case class ChannelArgs(singleActor:ActorRef) extends FlowControlArgs {
  
  override def workerArgs(): FlowControlArgs ={
    val newArgs = new ChannelArgs(singleActor)
    newArgs.addMaster(master)
    return newArgs
  }
}