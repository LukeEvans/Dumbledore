package com.reactor.store

import scala.collection.mutable.ListBuffer
import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.mongodb.casbah.MongoURI
import com.mongodb.casbah.query.dsl.QueryExpressionObject
import com.mongodb.casbah.Imports._


class MongoDB {
	val uri = MongoURI("mongodb://levans002:dakota1@ds031887.mongolab.com:31887/winston-db")
	val db = uri.connectDB
	
	def find(queryObj:DBObject, coll:String, limit:Int):ListBuffer[Object] = {
	  val collect = db.right.get.getCollection(coll)
	  val cursor = collect.find(queryObj).limit(limit)  
	  
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