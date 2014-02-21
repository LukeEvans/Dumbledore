package com.reactor.dumbledore.notifications

import com.reactor.patterns.pull.FlowControlArgs
import akka.actor.ActorRef

case class NotificationArgs(serviceActor:ActorRef) extends FlowControlArgs