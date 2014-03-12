package com.reactor.dumbledore.services

import scala.collection.mutable.{ListBuffer, Map}

case class WebRequestData(requestType:String, endpoint:String, rank:Int, params:Option[Map[String, String]], ids:ListBuffer[String])