package com.reactor.dumbledore.prime.cards

import scala.collection.mutable.ListBuffer

abstract case class Card()

case class CardSet(data:ListBuffer[Card]){
  val count = data.size
}