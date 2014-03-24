package com.reactor.dumbledore.prime.services.comics

import scala.collection.mutable.ListBuffer

object Comics {

  def getRandomToday():ListBuffer[Object] = {
	
    val cards = ListBuffer[Object]();
		
    initComics().foreach(comic => cards += comic.getToday)
		
    return cards;
  }	
  
  def initComics():ListBuffer[Comic] = {
    val list = ListBuffer[Comic]();
		
    list += new Comic("http://feeds.feedburner.com/uclick/calvinandhobbes?format=xml","Calvin and Hobbes","Calvin and Hobbes", "Bill Watterson");
    list += new Comic("http://feeds.feedburner.com/uclick/garfield?format=xml","Garfield","Garfield", "Jim Davis");
    list += new Comic("http://feeds.feedburner.com/uclick/doonesbury?format=xml","Doonesbury","Doonesbury", "Garry Trudeau");
    list += new Comic("http://feeds.feedburner.com/uclick/nonsequitur?format=xml","Nonsequitur","Non Sequitur", "Wiley Miller");
    list += new Comic("http://feeds.feedburner.com/uclick/peanuts?format=xml","Peanuts","Peanuts", "Charles Shulz");
    list += new Comic("http://feeds.feedburner.com/uclick/dilbert-classics?format=xml","Dilbert Classics","Dilbert Classics", "Scott Adams");
		
    return list;
  }
}