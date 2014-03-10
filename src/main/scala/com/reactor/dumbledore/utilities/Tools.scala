package com.reactor.dumbledore.utilities

import java.math.BigInteger
import java.security.MessageDigest
import java.util.HashMap
import java.net.URL
import java.net.URI
import java.io.InputStreamReader
import scala.collection.JavaConversions._
import scala.concurrent.Future
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import edu.stanford.nlp.trees.Tree
import java.util.ArrayList
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.HttpResponse
import java.net.URL
import java.net.URI
import java.io.BufferedReader
import java.io.InputStreamReader
import org.apache.commons.lang3.StringEscapeUtils
import org.joda.time.DateTimeZone
import org.joda.time.DateTime
import com.mongodb.casbah.commons.MongoDBObject
import org.apache.http.entity.StringEntity
import org.apache.http.client.methods.HttpPost

object Tools {
  @transient
  private val mapper = new ObjectMapper();
  
  def objectToJsonNode(mongoObj:Object):JsonNode = {
    val cleanObject = mongoObj.toString().replaceAll("\\r", " ").replaceAll("\\n", " ").trim
    mapper.readTree(cleanObject)	
  }
  
  def randomInt(min:Int, max:Int):Int = 
    (min + (Math.random()*((max-min) + 1)).toInt)
  
  def getDateForTZ(timezone:String):DateTime = {
    var offset = 0
    if(timezone != null && timezone.length() > 0)
      offset = Integer.parseInt(timezone) / 3600
      
    val dtz = DateTimeZone.forOffsetHours(offset)
    val date = new DateTime(dtz)
    date
  }
    
  def nodeFromMap(map:HashMap[String, Object]):ObjectNode = {
    try{
      
      var mapper = new ObjectMapper()
      var node = mapper.valueToTree(map)
      
      return node
      
    } catch{
      case e:Exception =>{
        e.printStackTrace()
        return null
      }
    }
  }
  
  def addObjectToJson(field:String, obj:Object, json:JsonNode):ObjectNode = {
    try{
      
      var mapper = new ObjectMapper()
      var node = json.asInstanceOf[ObjectNode]
      
      var jsonNode:JsonNode = mapper.valueToTree(obj)
      node.put(field, jsonNode)
      
      return node
      
    } catch{
      case e:Exception => {
        e.printStackTrace()
        return null
      }
    }
  }
  
  def jsonFromString(input:String):ObjectNode = {
    if(input != null && input.length() > 0){
      var objectMapper = new ObjectMapper()
      try{
        return objectMapper.readValue(input, classOf[ObjectNode])
      }catch{
        case e:Exception =>{
          e.printStackTrace()
          return null
        }
      }
    }
    else{
      return null
    }
  }
  
  def mergeBodyAndURL(params:java.util.Map[String, String], input:String):JsonNode = {
    var tempInput = escapeInput(input)
    
    var node:ObjectNode = jsonFromString(tempInput)
    
    if(node == null){
      var mapper = new ObjectMapper()
      node = mapper.createObjectNode()
    }
    
    
    for(key <- params.keySet()){
      node.put(key, params.get(key))
    }
    
    return node
  }
  
  def escapeInput(input:String):String = {
    return input.replace("\n", "").replace("\r", "")
  }
  
  def generateHash(s:String):String = {
    return md5(s.toLowerCase())
  }
  
  def md5(input:String):String = {
    var md5:String = null
    
    if(null == input) return null
    
    try{
      var digest = MessageDigest.getInstance("MD5")
      
      digest.update(input.getBytes(), 0, input.length())
      
      md5 = new BigInteger(1, digest.digest()).toString(16)
      
    } catch{
      case e:Exception => {
        e.printStackTrace()
        return null
      }
    }
    return md5
  }

    def fetchURL(url:String):JsonNode = {
        try {
                var httpClient = new DefaultHttpClient();
                httpClient.getParams().setParameter("http.socket.timeout", new Integer(5000));
                        var getRequest = new HttpGet(parseUrl(url).toString());
                        getRequest.addHeader("accept", "application/json");

                        var response = httpClient.execute(getRequest);

                        // Return JSON
                        var mapper = new ObjectMapper();
                        var reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                        return mapper.readTree(reader);

                } catch{
                  case e:Exception =>{
                        System.out.println("Failure: " + url);
                        e.printStackTrace();
                        return null;
                  }
                }
     }
    
             //================================================================================
        // URL encoding Methods
        //================================================================================
        def parseUrl(s:String):URL = {
                var u:URL = null;
                try {
                        u = new URL(s);
                        try {
                                return new URI(
                                                u.getProtocol(), 
                                                u.getAuthority(), 
                                                u.getPath(),
                                                u.getQuery(), 
                                                u.getRef()).toURL();
                        } catch {                  
                                  case e:Exception=> {
                                          e.printStackTrace();
                        
                                  }
                        }
                } 
                return null;
        }
        
  	def decodeCharacters(input:String):String = {
		var output = StringEscapeUtils.escapeHtml4(input);
		output = output.replaceAll("&rdquo;", "&quot;");
		output = output.replaceAll("&ldquo;", "&quot;");
		output = output.replaceAll("&lsquo;", "&apos;");
		output = output.replaceAll("&rsquo;", "&apos;");
		output = output.replaceAll("&mdash;", "-");
		output = StringEscapeUtils.unescapeHtml4(output);
		
		return output;
	}
  	
  	def postJsonString(url:String, jsonString:String):String = {
	  try {
		val httpClient = new DefaultHttpClient();
		val postRequest = new HttpPost(url);
                        
		val input = new StringEntity(jsonString);
                        
		input.setContentType("application/json");
		postRequest.setEntity(input);
		val response = httpClient.execute(postRequest);
                                
                        // Testing
		val reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
        val json = reader.readLine();
        return json                       
	  } catch{
	    case e:Exception =>{
	      e.printStackTrace()
	      null
	    }
	  }
  	}
}