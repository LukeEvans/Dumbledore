package com.reactor.dumbledore.notifications.time

import com.github.nscala_time.time.Imports._
import scala.collection.mutable.Map


/** Time Range with start & stop date and optional parameters Map[String, String]
 */
case class TimeRange(start:Date, stop:Date, params:Option[Map[String, String]])