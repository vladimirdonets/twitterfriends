package com.samplecode.twitterfriends.util.text

import org.springframework.boot.context.properties.ConfigurationProperties

/**
  * Created by vdonets on 4/10/2017.
  */
@ConfigurationProperties(prefix = "sanitizer")
private[text] class SanitizerConfig {

  private[text] var punctuation: String = ""

  def setPunctuation(chars: String) = punctuation = chars


}
