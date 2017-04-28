package com.samplecode.twitterfriends.util.text

import com.samplecode.twitterfriends.util.logging.LazyLogging

/**
  * Created by vdonets on 4/8/2017.
  */
private[twitterfriends] class Sanitizer(private val config: SanitizerConfig) extends LazyLogging {

  private val punctuation: Set[Char] = Set(config.punctuation.toCharArray: _*)
  logger.info("punctuation = " + punctuation)

  private[twitterfriends] def sanitize(word: String): String = {
    new String(word.toCharArray.filter(c => !punctuation.contains(c)))
  }

}
