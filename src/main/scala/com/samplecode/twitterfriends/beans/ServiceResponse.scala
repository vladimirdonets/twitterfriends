package com.samplecode.twitterfriends.beans

import com.samplecode.twitterfriends.classifier.ClassificationResult

/**
  * Created by vdonets on 4/8/2017.
  */
case class ServiceResponse private(userProfile: UserProfile,
                                   importantWords: Seq[(String, Double)],
                                   friends: Seq[ClassificationResult],
                                   message: String)

object ServiceResponse {

  def apply(tuple: (UserProfile, Seq[ClassificationResult], Seq[(String, Double)]), message: String): ServiceResponse = {
    if (tuple != null)
      return ServiceResponse(tuple._1, tuple._3, tuple._2, message)
    else
      return ServiceResponse(null, null, null, message)
  }
}
