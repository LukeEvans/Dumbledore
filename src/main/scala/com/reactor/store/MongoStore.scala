package com.reactor.store

import com.mongodb.casbah.MongoURI
import com.mongodb.casbah.Imports._
import com.fasterxml.jackson.databind.ObjectMapper
import com.mongodb.util.JSON
import java.util.ArrayList
import com.fasterxml.jackson.databind.JsonNode

class MongoStore(collectString:String) {
	val uri = MongoURI("mongodb://levans002:dakota1@ds031887.mongolab.com:31887/winston-db")
	val db = uri.connectDB

	val collection = db.right.get.getCollection(collectString)
	
	def findOne(queryObject:BasicDBObject):Object = {
		collection.findOne(queryObject)
	}
	
	def findOneSimple(field:String, value:String):Object = {
	  var queryObject = new BasicDBObject(field, value)
	  findOne(queryObject)
	}
	
	def findAll():ArrayList[MongoDBObject] = {
	  var list = new ArrayList[MongoDBObject]
	  var cursor = collection.find()
	  while(cursor.hasNext()){
	    list.add(cursor.next())
	  }
	  list
	}	
}