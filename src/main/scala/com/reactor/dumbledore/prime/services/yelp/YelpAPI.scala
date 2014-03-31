package com.reactor.dumbledore.prime.services.yelp

import org.scribe.builder.api.DefaultApi10a
import org.scribe.model.Token
import org.scribe.builder.ServiceBuilder
import org.scribe.model.OAuthRequest
import org.scribe.model.Response
import org.scribe.model.Token
import org.scribe.model.Verb
import org.scribe.oauth.OAuthService
import com.reactor.dumbledore.utilities.Tools
import com.fasterxml.jackson.databind.JsonNode


class DefaultAPI extends DefaultApi10a{
  
  override def getAccessTokenEndpoint():String = return "none";

  
  override def getAuthorizationUrl(token:Token):String = return "none"

  
  override def getRequestTokenEndpoint():String = return "none"
  
}


object YelpAPI {
  
  private val consumerKey = "bMdgu98OYqTlSFhgrAbIdw";
  private val consumerSecret = "2Du93QEajyjH9b8B7XlaXX5zv0k";
  private val token = "Zau3xeS8yyyxiXbWkfMW_e2L5eNxmVSm";
  private val tokenSecret = "chcf5SCVOM5NsCOGOmrIuvj093c";
	
  private val service = new ServiceBuilder().provider(classOf[DefaultAPI]).apiKey(consumerKey).apiSecret(consumerSecret).build();
  private val accessToken = new Token(token, tokenSecret)
  
  
  def search(term:String, latitude:Double, longitude:Double):JsonNode = {
    
    val request = new OAuthRequest(Verb.GET, "http://api.yelp.com/v2/search");
    request.addQuerystringParameter("term", term);
    request.addQuerystringParameter("ll", latitude + "," + longitude);
    
    this.service.signRequest(this.accessToken, request);
    
    val response = request.send();
    
    return Tools.jsonFromString(response.getBody());	 
  } 
}