package com.reactor.facebook

import java.util.ArrayList
import com.reactor.dumbledore.utilities.Tools
import com.googlecode.batchfb.FacebookBatcher
import org.joda.time.DateTime
import scala.collection.JavaConversions._
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import com.fasterxml.jackson.databind.JsonNode
import akka.actor.ActorLogging
import akka.actor.Actor
import akka.actor.ActorRef
import akka.util.Timeout
import akka.pattern.ask
import com.reactor.facebook.messages.StoryData
import com.reactor.facebook.messages.StoryContainer
import com.reactor.facebook.messages.FetchContainer
import com.reactor.facebook.messages.ListContainer

class FacebookFetcher(storyBuilder:ActorRef) extends Actor with ActorLogging  {
  
  def receive ={
    case fetch:FetchContainer => fetchFacebookData(fetch.token, fetch.timezone, sender)
    case _:Any => println("FacebookFetcher")
  }
  
  def fetchFacebookData(fbToken:String, timezone:String, origin:ActorRef){
    
    val date = Tools.getDateForTZ(timezone)
    
    val month = date.getMonthOfYear()
    val day = date.getDayOfMonth()
    
    val batcher = new FacebookBatcher(fbToken)
    
    val me = batcher.graph("me")
    val home = batcher.graph("me/home?limit=30")
    
    buildFacebook(me.get().path("data"), home.get().path("data"), date, fbToken, origin)
  }
  
  def buildFacebook(me:JsonNode, home:JsonNode, date:DateTime, token:String, origin:ActorRef) {
    implicit val timeout = Timeout(5 second);

    val fetchedStories = new ArrayList[FacebookStory]
    
    val id = me.path("id").asText
    
    val futureStories = home map{
      node => 
        (storyBuilder ? StoryData(node, id, token)).mapTo[StoryContainer]
    }
    
    val sequencedStories = Future.sequence(futureStories)
    
    val wait = for{
      storyContainers <- sequencedStories
      
    } yield storyContainers

    wait map{
      storyContainers =>
        storyContainers map{
          storyContainer =>
            if(storyContainer.story.id != null)
              fetchedStories.add(storyContainer.story)
        }
        origin ! ListContainer(fetchedStories)
    }
  }
  
  def buildScoreAndCover(story:FacebookStory):FacebookStory = {
    try{
      story.buildScore
    } catch{
      case e:Exception => e.printStackTrace()
    }
    story
  }
}