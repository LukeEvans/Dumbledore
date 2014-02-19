package com.reactor.dumbledore.notifications

import com.reactor.patterns.pull.FlowControlActor
import com.reactor.patterns.pull.FlowControlArgs

class NotificationManager(args:FlowControlArgs) extends FlowControlActor(args) {
	ready
	override def preStart() = println("Creating NotificationManagerActor")
	override def postStop() = println("Stopping NotificationManagerActor")
	
	override def receive ={
	  case a:Any => println("Unknown Message: " + a.toString)
	}
}