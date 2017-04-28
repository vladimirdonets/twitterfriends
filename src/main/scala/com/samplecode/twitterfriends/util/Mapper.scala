package com.samplecode.twitterfriends.util

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper

/**
  * Scala-capable object mapper
  * Created by vdonets on 4/8/2017.
  */
class Mapper extends ObjectMapper with ScalaObjectMapper{

  this.registerModule(DefaultScalaModule)
  this.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

}
