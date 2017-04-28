package com.samplecode.twitterfriends.classifier.cosine

import com.samplecode.twitterfriends.beans.UserProfile
import com.samplecode.twitterfriends.classifier.{ClassificationResult, Classifier}
import com.samplecode.twitterfriends.classifier.doc.{Corpus, Document}
import com.samplecode.twitterfriends.util.text.Sanitizer

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
  * Classifier implementation that uses cosine similarity and tf-idf
  * Created by vdonets on 4/8/2017.
  */
private[cosine] class CosineSimilarityClassifier(private implicit val sanitizer: Sanitizer) extends Classifier {

  /**
    * Calculates TF-IDF for one document (set of user's posts) given a corpus of all relevant friends posts
    *
    * @param userPosts posts of a user to calculate TD-IDF for
    * @param corpus    total corpus with posts from all relevant friends
    */
  private def tfidf(userPosts: Document, corpus: Corpus): (Map[String, Double], Seq[(String, Double)]) = {
    val idfs = new mutable.HashMap[String, Double]
    return (userPosts.toWordCount.map(word => {
      val idf = (word._2.toDouble / userPosts.wordCount.toDouble) *
        corpus.inverseDocumentFrequency(word._1)
      idfs.put(word._1, idf)
      (word._1, idf)
    }), {
      val sorted = idfs.toArray
      scala.util.Sorting.stableSort(
        sorted,
        (e1: (String, Double), e2: (String, Double)) => {
          e1._2 > e2._2
        }
      )
      sorted.take(5)
    })
  }

  /**
    * Calculates cosine similarity between two vector representations.
    *
    * @param vector1
    * @param vector2
    */
  private def cosineSimilarity(vector1: Map[String, Double],
                               vector2: Map[String, Double]): Double = {
    val n = dotProduct(vector1, vector2)
    val d = Math.sqrt(dotProduct(vector1, vector1)) *
      Math.sqrt(dotProduct(vector2, vector2))
    return n / d
  }

  /**
    * Multiplies two vectors and return a scalar result. Only elements
    * present in both maps are multiplied, since if an element is missing
    * in either map one of the multiples will be 0.
    *
    * @param vector1
    * @param vector2
    * @return
    */
  private def dotProduct(vector1: Map[String, Double],
                         vector2: Map[String, Double]): Double = {
    var n = 0.0
    vector1.foreach(e => {
      if (vector2.contains(e._1))
        n += (e._2 * vector2(e._1))
    })
    return n
  }

  /**
    * Calculates similarity of friends to a given user.
    *
    * @param userPosts posts of a user to compare friends against
    * @param friends   sorted friends with metadata and base line important words
    */
  private[twitterfriends] def sort(userPosts: Document,
                                   friends: Iterable[(UserProfile, Document)])
  : (Seq[ClassificationResult], Seq[(String, Double)]) = {
    val corpus = new Corpus
    corpus.update(userPosts)
    friends.foreach(f => corpus.update(f._2))
    val userTfIdf = tfidf(userPosts, corpus)
    val result = friends.map(friend => {
      val tuple = tfidf(friend._2, corpus)
      ClassificationResult(friend._1,
        cosineSimilarity(userTfIdf._1, tuple._1),
        tuple._2
      )
    }).toArray

    scala.util.Sorting.stableSort(
      result,
      (e1: ClassificationResult, e2: ClassificationResult) => {
        e1.score > e2.score
      }
    )

    return (result, userTfIdf._2)
  }
}
