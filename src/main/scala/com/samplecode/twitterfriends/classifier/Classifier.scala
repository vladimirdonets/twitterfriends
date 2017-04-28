package com.samplecode.twitterfriends.classifier

import com.samplecode.twitterfriends.beans.UserProfile
import com.samplecode.twitterfriends.classifier.doc.Document

/**
  * Orders friend profiles based on their similarity to a given user
  * calculated in some manner
  * Created by vdonets on 4/8/2017.
  */
private[twitterfriends] trait Classifier {

  /**
    * Calculates similarity of friends to a given user.
    *
    * @param userPosts posts of a user to compare friends against
    * @param friends   friend profiles and their posts to sort
    */
  private[twitterfriends] def sort(userPosts: Document, friends: Iterable[(UserProfile, Document)]):
  (Seq[ClassificationResult], Seq[(String, Double)])

}
