package com.samplecode.twitterfriends.classifier.doc

import com.samplecode.twitterfriends.util.text.Sanitizer

/**
  * A set of words we want to use to sort friends
  * in a classifier
  * Created by vdonets on 4/8/2017.
  */
private[twitterfriends] trait Document {



  /**
    * Collects a count of words in this document
    *
    * @return map of word to number of times it occurs
    */
  private[twitterfriends] def toWordCount()(implicit sanitizer: Sanitizer): Map[String, Int]

  /**
    * Number of words in this document
    *
    * @return
    */
  private[twitterfriends] def wordCount()(implicit sanitizer: Sanitizer): Int
}
