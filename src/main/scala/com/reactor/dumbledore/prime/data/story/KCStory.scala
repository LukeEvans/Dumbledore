package com.reactor.dumbledore.prime.data.story

import java.util.ArrayList
import com.reactor.dumbledore.prime.entities.Entity

class KCStory {
  	var id:String = null
	var story_type:String = null
	var entities:ArrayList[Entity] = null
	var speech:String = null
	var valid:Boolean = true
	var source_category:String = null
	var headline:String = null
	var source_id:String = null
	var source_name:String = null
	var author:String = null
	var summary:String = null
	var full_text:String = null
	var pubdate:String = null
	var date:Long = 0
	var link:String = null
	var source_icon_link:String = null
	var source_twitter_handle:String = null
	var image_links:ArrayList[String] = null
	var ceiling_Topic:String = null
	var related_topics:ArrayList[String] = null
	var main_topics:ArrayList[String] = null
}