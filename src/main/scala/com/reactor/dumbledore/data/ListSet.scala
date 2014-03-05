package com.reactor.dumbledore.data

import java.util.ArrayList
import scala.collection.mutable.ListBuffer
import com.fasterxml.jackson.databind.JsonNode

case class ListSet(card_id:String, set_data:ListBuffer[Object]) 

case class ListSetNode(card_id:String, list:ListBuffer[JsonNode])