package com.reactor.facebook.messages

import java.util.ArrayList
import com.reactor.facebook.FacebookStory
import com.fasterxml.jackson.databind.JsonNode

class Message extends Serializable

trait request
trait response

case class FBRequestContainer(fbRequest:FacebookRequest) extends request

case class FetchContainer(token:String, timezone:String) extends request

case class StoryData(node:JsonNode, id:String, token:String) extends request

case class StoryContainer(story:FacebookStory) extends request

case class ListContainer(list:ArrayList[FacebookStory]) extends response