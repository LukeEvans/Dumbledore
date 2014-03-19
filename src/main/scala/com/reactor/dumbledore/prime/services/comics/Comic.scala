package com.reactor.dumbledore.prime.services.comics

import java.net.URL
import com.sun.syndication.io.XmlReader
import com.sun.syndication.feed.synd.SyndEntry
import com.sun.syndication.feed.synd.SyndFeed
import com.sun.syndication.io.SyndFeedInput

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

class Comic {
  private var rssURLString = "http://feeds.feedburner.com/uclick/calvinandhobbes?format=xml";
  private var title:String = null;
  private var image_id:String = null;
  private var author:String = null;
  private var rssURL:URL = null;
  
  def this(url:String, title:String, id:String, author:String){
    this()
    rssURLString = url;
    this.title = title;
    this.image_id = id;
    this.author = author;
    try{
    	rssURL = new URL(rssURLString);
    } catch{
      case e:Exception => e.printStackTrace()
    }	
  }
  
  def getToday():ComicCard = {
    try{
      val reader = new XmlReader(rssURL);
	  val feed = new SyndFeedInput().build(reader);
		
		if(feed.getEntries().isEmpty())
			return null;
		val comicObj = feed.getEntries().get(0);
		
		if (comicObj.isInstanceOf[SyndEntry]){
			val entry = comicObj.asInstanceOf[SyndEntry];
			
			val webPage = connectToDoc(entry.getUri());
			val featureDiv = webPage.getElementsByClass("feature_item");
			val firstElement = featureDiv.get(0);
			val images = firstElement.getElementsByAttributeValue("alt", image_id);
			val image = images.first();
			val imageUrl = image.attr("src");
			
			return new ComicCard(imageUrl, title)
						.setAuthor(author)
						.setUrl(entry.getUri())
						.setDate(entry.getPublishedDate());
			
		}
		else{
			return null;
		}
		} catch{
		  case e:Exception =>
			e.printStackTrace();
			return null;
		}
	}
  
  	private def connectToDoc(url:String):Document = {
	  try{
		  val doc = Jsoup.connect(url).get();
		  return doc;
	  }
	  catch{
	    case e:Exception =>
	      e.printStackTrace();
		  return null;
	  }
	}
}