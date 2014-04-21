package com.reactor.dumbledore.prime.services.quote

import com.sun.syndication.io.XmlReader
import com.sun.syndication.io.SyndFeedInput
import java.net.URL
import com.sun.syndication.feed.synd.SyndEntry
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._
import com.reactor.dumbledore.utilities.Tools


object QuoteOfTheDay {
  
  val rssURLString = "http://feeds.feedburner.com/brainyquote/QUOTEBR"
    
    
  
  def getToday():ListBuffer[Object] = {
    
    try{
      
      // Build rss feed
      val rssURL = new URL(rssURLString)     
      val reader = new XmlReader(rssURL)
      val feed = new SyndFeedInput().build(reader)
      
      // Check for content in rss
      if(feed.getEntries().isEmpty())
        return null
        
      val quotes = ListBuffer[Object]()
      
      feed.getEntries().foreach{
        rssObject =>
          if(rssObject.isInstanceOf[SyndEntry]){
                	
            val entry = rssObject.asInstanceOf[SyndEntry]
        
            val author = entry.getTitleEx().getValue()
        
            val text = entry.getDescription().getValue()
        
            val id = Tools.generateHash(text)
        
            val quote = new Quote(id, author, text)
        
            quotes += quote
            
          }
      }
      
      return quotes
      
    } catch{
      
      case e:Exception => e.printStackTrace()
      return null
    }
  }

}