package com.samplecode.twitterfriends.classifier

import com.samplecode.twitterfriends.beans.UserProfile

/**
  * A single entry in a sorted seq of friend profiles with the profile info
  * and meta data why the user that particular place
  *
  * @param userProfile the user that was classifier/sorted
  * @param score       score the user profile got
  * @param baseOn      based on what words, etc
  */
case class ClassificationResult(userProfile: UserProfile,
                                score: Double,
                                baseOn: Iterable[(String, Double)]) {

}
