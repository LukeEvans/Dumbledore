package com.reactor.prime.data

import java.util.ArrayList

class StorySet {
  var status:String = null
  var count:Int = 0
  var data:ArrayList[Story] = null
  
  def addStory(story:Story):Unit ={
    if(story == null)
      return
    
    story.order = this.count
    data.add(story)
    count = count + 1
  }
  
  def resetData(){
    count = 0
    data.clear()
  }
}