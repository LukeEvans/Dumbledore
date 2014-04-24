package com.reactor.store

import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._
import com.mongodb.BasicDBObject
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.MongoURI
import com.novus.salat.grater
import com.novus.salat.global._
import com.reactor.dumbledore.prime.services.twitter.TwitterCacheSet
import com.mongodb.DBCursor


class MongoDB {
	val uri = MongoURI("mongodb://levans002:dakota1@ds031887.mongolab.com:31887/winston-db")
	val db = uri.connectDB
	
	
	/** Find and remove one instance of the DBObject
	 */
	def findAndDelete(dbObj:DBObject, coll:String){
	  
	  val collect = db.right.get.getCollection(coll)
	  
	  collect.remove(dbObj)
	}
	
	
	/** Insert DBObject
	 */
	def insert(dbObj:DBObject, coll:String){
	  
	  val collect = db.right.get.getCollection(coll)
	  
	  collect.insert(dbObj)
	}
	
	
	/** Find Objects matching query DBObject with a limit number of objects
	 */
	def find(queryObj:DBObject, coll:String, limit:Int):ListBuffer[Object] = {
	  val collect = db.right.get.getCollection(coll)
	  val cursor = collect.find(queryObj).limit(limit).sort(MongoDBObject("date" -> -1))
	  
	  val list = collect.find(queryObj).limit(limit).sort(MongoDBObject("date" -> -1)).toArray()
	  
	  val dataList = ListBuffer[Object]()
	  
	  for(item <- list){
	    dataList += item
	  }
	  
	  dataList
	}
	
	
	def findFirst(coll:String, limit:Option[Int] = None):DBCursor = {
	  
	  val collect = db.right.get.getCollection(coll)
	  
	  limit match{
	    case Some(lim) => return collect.find().limit(lim)
	    case None => return collect.find()
	  }
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