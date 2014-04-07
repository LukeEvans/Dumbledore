package com.reactor.store

import scala.collection.mutable.ListBuffer
import com.mongodb.BasicDBObject
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.MongoURI
import com.novus.salat.grater
import com.novus.salat.global._
import com.reactor.dumbledore.prime.services.twitter.TwitterCacheSet


class MongoDB {
	val uri = MongoURI("mongodb://levans002:dakota1@ds031887.mongolab.com:31887/winston-db")
	val db = uri.connectDB
	
	def findAndDelete(dbObj:DBObject, coll:String){
	  
	  val collect = db.right.get.getCollection(coll)
	  
	  collect.remove(dbObj)
	}
	
	def insert(dbObj:DBObject, coll:String){
	  
	  val collect = db.right.get.getCollection(coll)
	  
	  collect.insert(dbObj)
	}
	
	def find(queryObj:DBObject, coll:String, limit:Int):ListBuffer[Object] = {
	  val collect = db.right.get.getCollection(coll)
	  val cursor = collect.find(queryObj).limit(limit).sort(MongoDBObject("date" -> -1))
	  
	  val dataList = ListBuffer[Object]()
	  while(cursor.hasNext()){
	    dataList += cursor.next()
	  }
	  dataList
	}
	
	def findOne(queryObject:BasicDBObject, coll:String):Object = {
	  val collect = db.right.get.getCollection(coll)
		collect.findOne(queryObject)
	}
	
	def findOne(coll:String):Object = {
	  val collect = db.right.get.getCollection(coll)
	  collect.findOne()
	}
	
	def findOneSimple(field:String, value:String, coll:String):Object = {
	  
	  var queryObject = new BasicDBObject(field, value)
	  findOne(queryObject, coll)
	}
	
	
	def findOneSimpleAndDelete(field:String, value:String, coll:String) = {
	  
	  var queryObject = new BasicDBObject(field, value)
	  
	  findAndDelete(queryObject, coll)
	}
	
	
	def findAll(coll:String):ListBuffer[Object] = {
	  val collect = db.right.get.getCollection(coll)
	  val cursor = collect.find
	  
	  val objectList = ListBuffer[Object]() 
	  
	  while(cursor.hasNext()){
	    objectList += cursor.next()
	  }
	  
	  objectList
	}
}