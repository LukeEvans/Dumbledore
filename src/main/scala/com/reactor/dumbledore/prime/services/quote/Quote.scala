package com.reactor.dumbledore.prime.services.quote

class Quote {
  
  val story_type = "quote"
  var id:String = null
  var author:String = null
  var text:String = null

  def this(id:String, author:String, text:String){
    this()
    
    this.id = id
    this.author = author
    this.text = text
  }
  
}