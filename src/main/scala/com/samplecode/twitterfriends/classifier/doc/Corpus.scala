package com.samplecode.twitterfriends.classifier.doc

import com.samplecode.twitterfriends.util.text.Sanitizer

import scala.collection.mutable

/**
  * Holds all relevant words for a classification/ordering
  * task.
  * Created by vdonets on 4/8/2017.
  */
private[classifier] class Corpus {

  /**
    * Holds word mapped to number of times it appears in the corpus
    */
  private val words = new mutable.HashMap[String, Int]()
  private var docNum: Int = 0

  private[classifier] def update(document: Document)(implicit sanitizer: Sanitizer) = {
    document.toWordCount.foreach(e => {
      if (this.words.contains(e._1))
        this.words(e._1) =
          this.words(e._1) + 1
      else
        this.words(e._1) = 1
    })
    docNum += 1
  }

  /**
    * Calculates the idf of a given word with respect to all documents
    * this Corpus accounts for
    *
    * @param word the word to calculate idf for
    * @return
    */
  private[classifier] def inverseDocumentFrequency(word: String): Double = {
    if (words.contains(word))
      return Math.log(docNum.toDouble / words(word).toDouble)
    else
      throw new IllegalArgumentException
  }
}
