package com.samplecode.twitterfriends.datasource.twitter

import com.samplecode.twitterfriends.classifier.doc.Document
import com.samplecode.twitterfriends.datasource.twitter.response.Tweet
import com.samplecode.twitterfriends.util.text.Sanitizer

import scala.collection.mutable

/**
  * Created by vdonets on 4/8/2017.
  */
private[twitter] case class TwitterTimeline(tweets: Iterable[Tweet]) extends Document {


  private var wordBag: WordBag = null

  private def buildBag(sanitizer: Sanitizer): WordBag = {
    val result = new mutable.HashMap[String, Int]
    var wordCount = 0
    tweets.foreach(tweet => {
      tweet.text.split(" ").foreach(word => {
        val sanitized = sanitizer.sanitize(word).toLowerCase
        if (sanitized.length != 0) {
          wordCount += 1
          if (result.contains(sanitized)) {
            result(sanitized) = result(sanitized) + 1
          } else {
            result(sanitized) = 1
          }
        }
      })
    })
    return WordBag(wordCount, result.toMap)
  }

  override private[twitterfriends] def toWordCount()(implicit sanitizer: Sanitizer): Map[String, Int] = {
    if (wordBag == null) {
      wordBag = buildBag(sanitizer)
    }
    return wordBag.words
  }

  /**
    * Number of words in this document
    *
    * @return
    */
  override private[twitterfriends] def wordCount()(implicit sanitizer: Sanitizer): Int = {
    if (wordBag == null)
      wordBag = buildBag(sanitizer)
    return wordBag.wordCount
  }

  private case class WordBag(wordCount: Int, words: Map[String, Int])

}
