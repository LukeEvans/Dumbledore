package com.reactor.facebook

import akka.actor.ActorLogging
import akka.actor.Actor
import com.reactor.facebook.messages.StoryData
import akka.actor.ActorRef
import com.reactor.facebook.messages.StoryContainer

class StoryBuilder extends Actor with ActorLogging {
  def receive = {
    case data:StoryData => buildStory(data, sender)
    case a:Any => println("Unknown Type: "+ a.toString)
  }
  
  def buildStory(data:StoryData, origin:ActorRef){
    val story = new HomeStory(data.node, data.id, data.token)
    println("story built - " + story.id)
    origin ! StoryContainer(story)
  }
}