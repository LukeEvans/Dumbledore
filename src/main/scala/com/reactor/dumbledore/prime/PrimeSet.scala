package com.reactor.dumbledore.prime

import scala.collection.mutable.ListBuffer
import com.reactor.dumbledore.prime.data.ListSet

class PrimeSet {
  private val dataList = new ListBuffer[ListSet[Object]]
  
  
  def getSet():ListBuffer[ListSet[Object]] = dataList
  
  
  def +=(set:ListSet[Object]):Unit = {
    dataList += set
  }
  
  
  def ++=(list:ListBuffer[ListSet[Object]]){
    dataList ++= list
  }
  
  
  def sort():ListBuffer[ListSet[Object]] = {
    
    // ToDo: Decay social
    
    val sortedList = dataList.sortWith((a,b) => a.score > b.score)
    
	return sortedList
  }
}