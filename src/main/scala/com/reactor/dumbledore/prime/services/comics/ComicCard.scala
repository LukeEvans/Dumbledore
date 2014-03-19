package com.reactor.dumbledore.prime.services.comics

import com.reactor.dumbledore.utilities.Tools
import java.util.Date

class ComicCard {
  val `type` = "comic_strip"
  val story_type = "comic_strip"
  var id:String = null;
  var image:String = null;
  var title:String = null;
  var date:Date = null;
  var author:String = null;
  var url:String = null;
  var source_icon:String = "https://s3.amazonaws.com/Twitter_Source_Images/Winston-Twitter-Images/gocomics_icon.png";
	
  def this(imageUrl:String, title:String){
    this()
    this.image = imageUrl;
    this.id = Tools.generateHash(imageUrl);
    this.title = title;
  }
	
  def setAuthor(author:String):ComicCard = {
	this.author = author;
	return this;
  }
  
  def setUrl(url:String):ComicCard = {
	this.url = url;
	return this;
  }
	
  def setDate(date:Date):ComicCard = {
	if(date == null){
	  this.date = new Date();
	  return this;
	}
	else{
	  this.date = date;
	  return this;
	}
  }
}