package com.samplecode.twitterfriends.util.logging

import org.slf4j.LoggerFactory

/**
  * Created by vdonets on 4/8/2017.
  */
trait LazyLogging {

  protected lazy val logger = LoggerFactory.getLogger(this.getClass)
}
