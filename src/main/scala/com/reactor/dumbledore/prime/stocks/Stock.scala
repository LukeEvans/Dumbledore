package com.reactor.dumbledore.prime.stocks

import com.fasterxml.jackson.databind.JsonNode

case class Stock(company:String, symbol:String) {
	def this(node:JsonNode) = this(node.get("company").asText(), node.get("symbol").asText())
}