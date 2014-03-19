package com.reactor.dumbledore.prime

import scala.collection.mutable.ListBuffer
import com.reactor.dumbledore.data.ListSet

class PrimeSet {
  private val dataList = new ListBuffer[ListSet[Object]]
  
  def +=(set:ListSet[Object]):Unit = {
    dataList += set
  }
  
  def ++=(list:ListBuffer[ListSet[Object]]){
    dataList ++= list
  }
  
  def sort():ListBuffer[ListSet[Object]] = {
    
    val sortedList = dataList.sortWith((a,b) => a.rank < b.rank)
    
	return sortedList
  }
}