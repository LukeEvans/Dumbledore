package com.reactor.word2vec

import java.io.PrintWriter
import scala.collection.JavaConversions._
import com.reactor.store.MongoDB
import com.reactor.dumbledore.utilities.Tools

object TrainingSet {

  val mongo = new MongoDB
  
  def main(args:Array[String]){
   
    val printWriter = new PrintWriter("training-set", "UTF-8")
    
    val numStories = 50000
    
    val newsIt = mongo.findFirst("reactor-news")
    
    var count = 0
    
    while(newsIt.hasNext()){
      try{
        val json = Tools.objectToJsonNode(newsIt.next())
      
        if(json.has("full_text")){
        
          println("Writing Story #" + count + "  " + json.get("headline").asText())
        
          var storyText = json.get("full_text").asText
        
          if(json.has("entities")){
            val entityList = json.get("entities").map{
              entityNode =>
                entityNode.get("entity_name").asText().toLowerCase()
            } 
          
            storyText = storyText.toLowerCase()
          
            entityList.foreach{ 
              entity => 
                try{
                  
                val entityString = entity.replaceAll("\\*", "").replaceAll("\\(", "").replaceAll("\\)", "")
                
                storyText = storyText.replaceAll( entityString, entityString.replaceAll(" ", "_").replaceAll("-", "_"))
                
                } catch{
                  case e:Exception => 
                    println("Entity Error - " + entity)
                    e.printStackTrace()
                }
            }
          
          }
       
        
        
          val editedText = storyText.replaceAll("'", " ")
                           .replaceAll("\\.", "")
                           .replaceAll("\\?", "")
                           .replaceAll(",", "")
                           .replaceAll("-", " ")
                           .replaceAll("\\(","")
                           .replaceAll("\\)","")
                           .replaceAll("\"","")
        
          printWriter.write(editedText)
          printWriter.write(" ")
        
          count += 1
          
        }
      } catch{
        case e:Exception => e.printStackTrace()
      }
    }
    
  }
}