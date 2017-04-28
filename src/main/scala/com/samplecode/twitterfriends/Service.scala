package com.samplecode.twitterfriends

import com.samplecode.twitterfriends.beans.UserProfile
import com.samplecode.twitterfriends.classifier.{ClassificationResult, Classifier}
import com.samplecode.twitterfriends.datasource.Datasource
import com.samplecode.twitterfriends.classifier.doc.Document
import com.samplecode.twitterfriends.exception.InaccessibleProfileException
import com.samplecode.twitterfriends.util.logging.LazyLogging

/**
  * Created by vdonets on 4/8/2017.
  */
private[twitterfriends] class Service(private val ds: Datasource,
                                      private val classifier: Classifier) extends LazyLogging {
  /**
    * Looks up a user by screen name, find friends, recent posts in underlying
    * social network, and sorts friends based on similarity of posts to user
    *
    * @param screenName name of user
    * @return userProfile, Iterable(friendProfile, closenessMeasure)
    */
  private[twitterfriends] def relevantFriends(screenName: String, numPosts: Int):
  (UserProfile, Seq[ClassificationResult], Seq[(String, Double)]) = {
    val user = ds.findUser(screenName)
    if (user == null)
      throw new InaccessibleProfileException("User [" + screenName + "] profile does not exist or is private")
    val t = ds.searchPosts(Iterable(user), numPosts)
    val posts =
      if (t.nonEmpty)
        t.head._2
      else throw new InaccessibleProfileException("User [" + screenName + "] does not have any posts")
    //find friends and their posts
    val friends = ds.searchPosts(
      //from user accounts
      ds.findUsers(
        //from friend ids
        ds.findFriendIds(
          //of the user
          user)), numPosts)
    val result = classifier.sort(
      posts,
      friends)
    logger.info("result = " + result._1.toString)
    logger.info("based on = " + result._2.toString)
    return (user, result._1, result._2)
  }
}
